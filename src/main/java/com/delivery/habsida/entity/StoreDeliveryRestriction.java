package com.delivery.habsida.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "store_delivery_restrictions")
public class StoreDeliveryRestriction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "restricted_area", nullable = false, length = 100)
    private String restrictedArea;

    public String getRestrictedArea() {return restrictedArea;}

    public void setRestrictedArea(String restrictedArea) {this.restrictedArea = restrictedArea;}

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }
}
