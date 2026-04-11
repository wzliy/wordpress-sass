package com.wpss.wordpresssass.report.infrastructure.mapper;

import com.wpss.wordpresssass.report.infrastructure.dataobject.SiteOrderSummaryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderReportMapper {

    @Select("""
            <script>
            SELECT o.site_id AS site_id,
                   COALESCE(s.name, CONCAT('Site #', o.site_id)) AS site_name,
                   COUNT(1) AS total_orders,
                   SUM(CASE WHEN o.payment_status = 'PAID' THEN 1 ELSE 0 END) AS paid_orders,
                   SUM(CASE WHEN o.shipping_status IN ('SHIPPED', 'DELIVERED') THEN 1 ELSE 0 END) AS shipped_orders,
                   COALESCE(SUM(CASE WHEN o.payment_status = 'PAID' THEN o.total_amount ELSE 0 END), 0) AS total_revenue
            FROM orders o
            LEFT JOIN site s
              ON s.tenant_id = o.tenant_id AND s.id = o.site_id
            WHERE o.tenant_id = #{tenantId}
            <if test="siteId != null">
                AND o.site_id = #{siteId}
            </if>
            <if test="dateFrom != null">
                AND o.created_at <![CDATA[>=]]> #{dateFrom}
            </if>
            <if test="dateTo != null">
                AND o.created_at <![CDATA[<=]]> #{dateTo}
            </if>
            GROUP BY o.site_id, s.name
            ORDER BY total_revenue DESC, total_orders DESC, o.site_id DESC
            </script>
            """)
    List<SiteOrderSummaryDO> summarizeBySite(@Param("tenantId") Long tenantId,
                                             @Param("siteId") Long siteId,
                                             @Param("dateFrom") LocalDateTime dateFrom,
                                             @Param("dateTo") LocalDateTime dateTo);
}
