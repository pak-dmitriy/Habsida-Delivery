package com.delivery.habsida.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "store_delivery_areas")
public class StoreDeliveryArea extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "district_name", nullable = false)
    private String districtName;

    @Column(name = "delivery_fee", nullable = false)
    private BigDecimal deliveryFee;

    public String getDistrictName() {return districtName;}
    public void setDistrictName(String districtName) {this.districtName = districtName;}

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }
}
