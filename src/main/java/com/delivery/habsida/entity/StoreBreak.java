package com.delivery.habsida.entity;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "store_breaks")
public class StoreBreak extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_hours_id", nullable = false)
    private StoreHour storeHour;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    public StoreHour getStoreHour() {
        return storeHour;
    }

    public void setStoreHour(StoreHour storeHour) {
        this.storeHour = storeHour;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
