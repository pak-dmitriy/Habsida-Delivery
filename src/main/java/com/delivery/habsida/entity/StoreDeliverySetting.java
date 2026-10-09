package com.delivery.habsida.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "store_delivery_settings")
public class StoreDeliverySetting extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "delivery_enabled", nullable = false)
    private Boolean deliveryEnabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false)
    private DeliveryType deliveryType;

    @Column(name = "min_order_price", nullable = false)
    private BigDecimal minOrderPrice;

    @Column(name = "free_delivery_threshold", nullable = false)
    private BigDecimal freeDeliveryThreshold;

    @Column(name = "max_distance", nullable = false)
    private BigDecimal maxDistance;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_geography", nullable = false)
    private DeliveryGeography deliveryGeography;

    public Boolean getDeliveryEnabled() {
        return deliveryEnabled;
    }

    public void setDeliveryEnabled(Boolean deliveryEnabled) {
        this.deliveryEnabled = deliveryEnabled;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    public BigDecimal getMinOrderPrice() {
        return minOrderPrice;
    }

    public void setMinOrderPrice(BigDecimal minOrderPrice) {
        this.minOrderPrice = minOrderPrice;
    }

    public BigDecimal getFreeDeliveryThreshold() {
        return freeDeliveryThreshold;
    }

    public void setFreeDeliveryThreshold(BigDecimal freeDeliveryThreshold) {
        this.freeDeliveryThreshold = freeDeliveryThreshold;
    }

    public BigDecimal getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(BigDecimal maxDistance) {
        this.maxDistance = maxDistance;
    }

    public DeliveryGeography getDeliveryGeography() {
        return deliveryGeography;
    }

    public void setDeliveryGeography(DeliveryGeography deliveryGeography) {
        this.deliveryGeography = deliveryGeography;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }
}
