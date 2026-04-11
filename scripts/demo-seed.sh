#!/usr/bin/env bash
set -euo pipefail

APP_URL="${APP_URL:-http://127.0.0.1:8080}"
ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin123}"
STORE_HOST="${STORE_HOST:-demo-shop.local}"
STORE_BASE_URL="${STORE_BASE_URL:-http://${STORE_HOST}:8080}"
SITE_NAME="${SITE_NAME:-Demo Shop}"
WP_USERNAME="${WP_USERNAME:-admin}"
WP_APP_PASSWORD="${WP_APP_PASSWORD:-secret}"

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing required command: $1" >&2
    exit 1
  }
}

extract_token() {
  python3 -c '
import json
import sys

payload = json.load(sys.stdin)
token = payload.get("data", {}).get("token")
if not token:
    raise SystemExit("Missing data.token in login response")
print(token)
'
}

extract_data_id() {
  python3 -c '
import json
import sys

payload = json.load(sys.stdin)
identifier = payload.get("data", {}).get("id")
if identifier is None:
    raise SystemExit("Missing data.id in API response")
print(identifier)
'
}

find_site_id_by_base_url() {
  local target_base_url="$1"
  python3 -c '
import json
import sys

target = sys.argv[1]
payload = json.load(sys.stdin)
for item in payload.get("data", []):
    if item.get("baseUrl") == target:
        print(item["id"])
        break
' "$target_base_url"
}

find_category_id_by_slug() {
  local target_slug="$1"
  python3 -c '
import json
import sys

target = sys.argv[1]
payload = json.load(sys.stdin)
for item in payload.get("data", []):
    if item.get("slug") == target:
        print(item["id"])
        break
' "$target_slug"
}

find_product_id_by_sku() {
  local target_sku="$1"
  python3 -c '
import json
import sys

target = sys.argv[1]
payload = json.load(sys.stdin)
for item in payload.get("data", []):
    if item.get("sku") == target:
        print(item["id"])
        break
' "$target_sku"
}

extract_location() {
  awk 'BEGIN { IGNORECASE = 1 } /^Location:/ { value = $2; sub(/\r$/, "", value); print value; exit }'
}

json_compact() {
  python3 -c '
import json
import sys

payload = json.load(sys.stdin)
print(json.dumps(payload, ensure_ascii=False))
'
}

api_get() {
  local path="$1"
  curl -fsS \
    -H "Authorization: Bearer ${TOKEN}" \
    "${APP_URL}${path}"
}

api_post_json() {
  local path="$1"
  local body="$2"
  curl -fsS \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -X POST \
    -d "${body}" \
    "${APP_URL}${path}"
}

api_put_json() {
  local path="$1"
  local body="$2"
  curl -fsS \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -X PUT \
    -d "${body}" \
    "${APP_URL}${path}"
}

storefront_post_form_with_location() {
  local path="$1"
  shift
  curl -fsS -o /dev/null -D - \
    -H "Host: ${STORE_HOST}" \
    -b "${COOKIE_JAR}" \
    -c "${COOKIE_JAR}" \
    -X POST \
    "${APP_URL}${path}" \
    "$@"
}

ensure_health() {
  curl -fsS "${APP_URL}/actuator/health" >/dev/null
}

login() {
  curl -fsS \
    -H "Content-Type: application/json" \
    -X POST \
    -d "{\"username\":\"${ADMIN_USERNAME}\",\"password\":\"${ADMIN_PASSWORD}\"}" \
    "${APP_URL}/auth/login" | extract_token
}

ensure_site() {
  local existing_id
  existing_id="$(api_get "/api/admin/sites" | find_site_id_by_base_url "${STORE_BASE_URL}" || true)"
  if [[ -n "${existing_id}" ]]; then
    printf '%s\n' "${existing_id}"
    return
  fi

  api_post_json "/api/admin/sites" \
    "{\"name\":\"${SITE_NAME}\",\"baseUrl\":\"${STORE_BASE_URL}\",\"wpUsername\":\"${WP_USERNAME}\",\"appPassword\":\"${WP_APP_PASSWORD}\"}" \
    | extract_data_id
}

ensure_category() {
  local name="$1"
  local slug="$2"
  local existing_id
  existing_id="$(api_get "/api/admin/categories" | find_category_id_by_slug "${slug}" || true)"
  if [[ -n "${existing_id}" ]]; then
    printf '%s\n' "${existing_id}"
    return
  fi

  api_post_json "/api/admin/categories" \
    "{\"name\":\"${name}\",\"slug\":\"${slug}\"}" \
    | extract_data_id
}

ensure_product() {
  local sku="$1"
  local title="$2"
  local category_id="$3"
  local price="$4"
  local compare_price="$5"
  local cover_image="$6"
  local description_html="$7"
  local sizes_json="$8"
  local gallery_json="$9"
  local existing_id
  existing_id="$(api_get "/api/admin/products" | find_product_id_by_sku "${sku}" || true)"
  if [[ -n "${existing_id}" ]]; then
    printf '%s\n' "${existing_id}"
    return
  fi

  api_post_json "/api/admin/products" \
    "{
      \"sku\":\"${sku}\",
      \"title\":\"${title}\",
      \"categoryId\":${category_id},
      \"coverImage\":\"${cover_image}\",
      \"galleryImages\":${gallery_json},
      \"descriptionHtml\":\"${description_html}\",
      \"sizes\":${sizes_json},
      \"price\":${price},
      \"compareAtPrice\":${compare_price},
      \"status\":\"ACTIVE\"
    }" | extract_data_id
}

publish_product() {
  local product_id="$1"
  local site_id="$2"
  api_post_json "/api/admin/products/${product_id}/publishes/${site_id}/publish" "{}" >/dev/null
}

create_order() {
  local product_id="$1"
  local quantity="$2"
  local first_name="$3"
  local last_name="$4"
  local email="$5"
  local header_output
  local location

  storefront_post_form_with_location "/cart/items" \
    --data-urlencode "productId=${product_id}" \
    --data-urlencode "quantity=${quantity}" >/dev/null

  header_output="$(storefront_post_form_with_location "/checkout" \
    --data-urlencode "firstName=${first_name}" \
    --data-urlencode "lastName=${last_name}" \
    --data-urlencode "email=${email}" \
    --data-urlencode "phone=+15550007777" \
    --data-urlencode "country=US" \
    --data-urlencode "state=California" \
    --data-urlencode "city=San Francisco" \
    --data-urlencode "postalCode=94105" \
    --data-urlencode "addressLine1=Mission Street 88")"

  location="$(printf '%s' "${header_output}" | extract_location)"
  if [[ -z "${location}" ]]; then
    echo "Failed to capture checkout redirect location" >&2
    exit 1
  fi

  location="${location#"/order/"}"
  location="${location%"/success"}"
  printf '%s\n' "${location}"
}

pay_order() {
  local order_no="$1"
  local payment_location
  local payment_no

  payment_location="$(storefront_post_form_with_location "/payments/orders/${order_no}/initiate" \
    --data-urlencode "providerCode=MOCK" | extract_location)"
  if [[ -z "${payment_location}" ]]; then
    echo "Failed to capture mock payment redirect location" >&2
    exit 1
  fi
  payment_no="${payment_location##*/}"

  storefront_post_form_with_location "/payments/mock/${payment_no}/callback" >/dev/null
}

ship_order() {
  local order_no="$1"
  local tracking_no="$2"

  api_put_json "/api/supply/shipments/${order_no}" \
    "{
      \"procurementStatus\":\"ORDERED\",
      \"shipmentStatus\":\"SHIPPED\",
      \"trackingNo\":\"${tracking_no}\",
      \"carrier\":\"UPS\",
      \"failureReason\":\"\"
    }" >/dev/null
}

print_report_summary() {
  local report_payload
  report_payload="$(api_get "/api/admin/reports")"
  printf '%s' "${report_payload}" | python3 -c '
import json
import sys

payload = json.load(sys.stdin).get("data", {})
summary = {
    "totalOrders": payload.get("totalOrders"),
    "paidOrders": payload.get("paidOrders"),
    "shippedOrders": payload.get("shippedOrders"),
    "totalRevenue": payload.get("totalRevenue"),
}
print(json.dumps(summary, ensure_ascii=False))
'
}

main() {
  require_cmd curl
  require_cmd python3

  ensure_health
  TOKEN="$(login)"
  COOKIE_JAR="$(mktemp)"
  trap 'rm -f "${COOKIE_JAR}"' EXIT

  echo "Seeding demo data against ${APP_URL} for store host ${STORE_HOST}"

  site_id="$(ensure_site)"
  bags_category_id="$(ensure_category "Travel Bags" "travel-bags")"
  wellness_category_id="$(ensure_category "Wellness" "wellness")"
  fitness_category_id="$(ensure_category "Home Fitness" "home-fitness")"

  backpack_product_id="$(ensure_product \
    "DEMO-BAG-001" \
    "Transit Backpack" \
    "${bags_category_id}" \
    "79.90" \
    "119.90" \
    "https://cdn.example.com/demo/transit-backpack.jpg" \
    "<p>Carry-on friendly backpack for paid-traffic landing pages and daily commuting.</p>" \
    "[\"Standard\"]" \
    "[\"https://cdn.example.com/demo/transit-backpack-1.jpg\",\"https://cdn.example.com/demo/transit-backpack-2.jpg\"]")"
  vitamins_product_id="$(ensure_product \
    "DEMO-WELL-001" \
    "Daily Balance Gummies" \
    "${wellness_category_id}" \
    "29.90" \
    "49.90" \
    "https://cdn.example.com/demo/daily-balance.jpg" \
    "<p>Vitamin gummy set for a simple subscription-style storefront demo.</p>" \
    "[\"30 Count\"]" \
    "[\"https://cdn.example.com/demo/daily-balance-1.jpg\"]")"
  mat_product_id="$(ensure_product \
    "DEMO-FIT-001" \
    "Flex Training Mat" \
    "${fitness_category_id}" \
    "39.90" \
    "59.90" \
    "https://cdn.example.com/demo/flex-mat.jpg" \
    "<p>Foldable training mat for a fast-moving fitness single-product page.</p>" \
    "[\"One Size\"]" \
    "[\"https://cdn.example.com/demo/flex-mat-1.jpg\"]")"

  publish_product "${backpack_product_id}" "${site_id}"
  publish_product "${vitamins_product_id}" "${site_id}"
  publish_product "${mat_product_id}" "${site_id}"

  paid_order_no="$(create_order "${backpack_product_id}" "1" "Demo" "Paid" "buyer-paid@example.com")"
  pay_order "${paid_order_no}"
  ship_order "${paid_order_no}" "DEMO-TRACK-001"

  pending_order_no="$(create_order "${mat_product_id}" "1" "Demo" "Pending" "buyer-pending@example.com")"

  echo "Seed completed."
  echo "siteId=${site_id}"
  echo "categoryIds={travel-bags:${bags_category_id}, wellness:${wellness_category_id}, home-fitness:${fitness_category_id}}"
  echo "productIds={backpack:${backpack_product_id}, gummies:${vitamins_product_id}, mat:${mat_product_id}}"
  echo "paidOrderNo=${paid_order_no}"
  echo "pendingOrderNo=${pending_order_no}"
  echo "reportSummary=$(print_report_summary)"
}

main "$@"
