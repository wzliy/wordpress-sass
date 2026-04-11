package com.wpss.wordpresssass.report.infrastructure.dataobject;

import java.math.BigDecimal;

public class SiteOrderSummaryDO {

    private Long siteId;
    private String siteName;
    private Integer totalOrders;
    private Integer paidOrders;
    private Integer shippedOrders;
    private BigDecimal totalRevenue;

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Integer getPaidOrders() {
        return paidOrders;
    }

    public void setPaidOrders(Integer paidOrders) {
        this.paidOrders = paidOrders;
    }

    public Integer getShippedOrders() {
        return shippedOrders;
    }

    public void setShippedOrders(Integer shippedOrders) {
        this.shippedOrders = shippedOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
