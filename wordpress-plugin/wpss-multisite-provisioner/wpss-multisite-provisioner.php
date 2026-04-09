<?php
/**
 * Plugin Name: WPSS Multisite Provisioner
 * Description: Exposes a network admin REST endpoint for provisioning WordPress Multisite subsites from WP SaaS.
 * Version: 0.1.0
 * Author: WP SaaS
 * Network: true
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( 'WPSS_Multisite_Provisioner' ) ) {
	final class WPSS_Multisite_Provisioner {
		const REST_NAMESPACE = 'wpss/v1';
		const REST_ROUTE     = '/sites';

		public static function bootstrap() {
			add_action( 'rest_api_init', array( __CLASS__, 'register_routes' ) );
		}

		public static function activate() {
			if ( is_multisite() ) {
				return;
			}

			deactivate_plugins( plugin_basename( __FILE__ ) );
			wp_die( esc_html__( 'WPSS Multisite Provisioner requires WordPress Multisite.', 'wpss-multisite-provisioner' ) );
		}

		public static function register_routes() {
			register_rest_route(
				self::REST_NAMESPACE,
				self::REST_ROUTE,
				array(
					'methods'             => WP_REST_Server::CREATABLE,
					'callback'            => array( __CLASS__, 'handle_create_site' ),
					'permission_callback' => array( __CLASS__, 'can_manage_network' ),
				)
			);
		}

		public static function can_manage_network() {
			if ( ! is_multisite() ) {
				return self::error( 'wpss_multisite_required', 'This endpoint requires WordPress Multisite.', 400 );
			}

			if ( ! current_user_can( 'manage_network' ) ) {
				return self::error( 'rest_forbidden', 'You are not allowed to provision sites.', rest_authorization_required_code() );
			}

			return true;
		}

		public static function handle_create_site( WP_REST_Request $request ) {
			if ( ! function_exists( 'wpmu_create_blog' ) || ! function_exists( 'add_user_to_blog' ) ) {
				require_once ABSPATH . 'wp-admin/includes/ms.php';
			}

			$title       = sanitize_text_field( (string) $request->get_param( 'title' ) );
			$slug        = sanitize_title( (string) $request->get_param( 'slug' ) );
			$admin_email = sanitize_email( (string) $request->get_param( 'adminEmail' ) );
			$tenant_id   = absint( $request->get_param( 'tenantId' ) );

			if ( '' === $title ) {
				return self::error( 'wpss_invalid_title', 'The title field is required.', 400 );
			}

			if ( '' === $slug ) {
				return self::error( 'wpss_invalid_slug', 'The slug field is required.', 400 );
			}

			if ( ! is_email( $admin_email ) ) {
				return self::error( 'wpss_invalid_admin_email', 'The adminEmail field must be a valid email address.', 400 );
			}

			if ( 0 === $tenant_id ) {
				return self::error( 'wpss_invalid_tenant_id', 'The tenantId field is required.', 400 );
			}

			$network = get_network( get_current_network_id() );
			if ( ! $network ) {
				return self::error( 'wpss_network_not_found', 'The current multisite network could not be resolved.', 500 );
			}

			$site_address = self::build_site_address( $network, $slug );
			if ( is_wp_error( $site_address ) ) {
				return $site_address;
			}

			if ( function_exists( 'domain_exists' ) && domain_exists( $site_address['domain'], $site_address['path'], (int) $network->id ) ) {
				return self::error( 'wpss_site_exists', 'A site with the same slug already exists in this network.', 409 );
			}

			$user = self::find_or_create_admin_user( $admin_email, $tenant_id, $slug, $title );
			if ( is_wp_error( $user ) ) {
				return $user;
			}

			$blog_id = wpmu_create_blog(
				$site_address['domain'],
				$site_address['path'],
				$title,
				(int) $user->ID,
				array( 'public' => 1 ),
				(int) $network->id
			);

			if ( is_wp_error( $blog_id ) ) {
				return self::error( 'wpss_create_site_failed', $blog_id->get_error_message(), 500 );
			}

			$add_user_result = add_user_to_blog( (int) $blog_id, (int) $user->ID, 'administrator' );
			if ( is_wp_error( $add_user_result ) ) {
				return self::error( 'wpss_assign_admin_failed', $add_user_result->get_error_message(), 500 );
			}

			if ( function_exists( 'update_blog_option' ) ) {
				update_blog_option( (int) $blog_id, 'wpss_tenant_id', $tenant_id );
			}

			$app_password = self::create_application_password( (int) $user->ID, $tenant_id, (int) $blog_id );
			if ( is_wp_error( $app_password ) ) {
				return $app_password;
			}

			$base_url = untrailingslashit( get_home_url( (int) $blog_id ) );
			$response = array(
				'blogId'      => (int) $blog_id,
				'tenantId'    => $tenant_id,
				'baseUrl'     => $base_url,
				'domain'      => $base_url,
				'adminUrl'    => untrailingslashit( get_admin_url( (int) $blog_id ) ),
				'wpUsername'  => $user->user_login,
				'appPassword' => $app_password,
				'message'     => 'Multisite provision completed',
			);

			return new WP_REST_Response( $response, 201 );
		}

		private static function build_site_address( WP_Network $network, $slug ) {
			$network_path = '/' === $network->path ? '/' : trailingslashit( $network->path );

			if ( is_subdomain_install() ) {
				$domain = $slug . '.' . $network->domain;
				$path   = $network_path;
			} else {
				$domain = $network->domain;
				$path   = trailingslashit( ltrim( $network_path . $slug, '/' ) );
				$path   = '/' . ltrim( $path, '/' );
			}

			if ( '' === $domain || '' === $path ) {
				return self::error( 'wpss_invalid_network', 'The multisite network domain or path is invalid.', 500 );
			}

			return array(
				'domain' => $domain,
				'path'   => $path,
			);
		}

		private static function find_or_create_admin_user( $admin_email, $tenant_id, $slug, $title ) {
			$existing_user = get_user_by( 'email', $admin_email );
			if ( $existing_user instanceof WP_User ) {
				return $existing_user;
			}

			$username = self::allocate_username( $admin_email, $tenant_id, $slug );
			$user_id  = wp_insert_user(
				array(
					'user_login'   => $username,
					'user_pass'    => wp_generate_password( 32, true, true ),
					'user_email'   => $admin_email,
					'display_name' => $title . ' Admin',
					'nickname'     => $title . ' Admin',
				)
			);

			if ( is_wp_error( $user_id ) ) {
				return self::error( 'wpss_create_user_failed', $user_id->get_error_message(), 500 );
			}

			return get_user_by( 'id', (int) $user_id );
		}

		private static function allocate_username( $admin_email, $tenant_id, $slug ) {
			$email_prefix = strstr( $admin_email, '@', true );
			$seed         = sanitize_user( 'tenant_' . $tenant_id . '_' . $email_prefix . '_' . $slug, true );
			$base         = substr( $seed, 0, 48 );

			if ( '' === $base ) {
				$base = 'tenant_' . $tenant_id;
			}

			$candidate = $base;
			$suffix    = 1;
			while ( username_exists( $candidate ) ) {
				$trimmed   = substr( $base, 0, max( 1, 48 - strlen( (string) $suffix ) ) );
				$candidate = $trimmed . $suffix;
				$suffix++;
			}

			return $candidate;
		}

		private static function create_application_password( $user_id, $tenant_id, $blog_id ) {
			if ( ! class_exists( 'WP_Application_Passwords' ) ) {
				return self::error( 'wpss_application_passwords_unavailable', 'Application passwords are not available in this WordPress installation.', 500 );
			}

			if ( function_exists( 'wp_is_application_passwords_available_for_user' ) ) {
				$user = get_user_by( 'id', $user_id );
				if ( $user instanceof WP_User && ! wp_is_application_passwords_available_for_user( $user ) ) {
					return self::error( 'wpss_application_passwords_disabled', 'Application passwords are disabled for the target administrator. HTTPS is typically required.', 400 );
				}
			}

			$result = WP_Application_Passwords::create_new_application_password(
				$user_id,
				array(
					'name'   => sprintf( 'WP SaaS Tenant %d Site %d', $tenant_id, $blog_id ),
					'app_id' => wp_generate_uuid4(),
				)
			);

			if ( is_wp_error( $result ) ) {
				return self::error( 'wpss_create_application_password_failed', $result->get_error_message(), 500 );
			}

			return $result[0];
		}

		private static function error( $code, $message, $status ) {
			return new WP_Error(
				$code,
				$message,
				array(
					'status' => $status,
				)
			);
		}
	}

	register_activation_hook( __FILE__, array( 'WPSS_Multisite_Provisioner', 'activate' ) );
	WPSS_Multisite_Provisioner::bootstrap();
}
