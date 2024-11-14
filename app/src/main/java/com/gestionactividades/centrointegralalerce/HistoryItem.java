package com.gestionactividades.centrointegralalerce;

import java.util.List;

public class HistoryItem {
    private String activityId;
    private String name;
    private String date;
    private String time;
    private boolean isCanceled;
    private String cancellationReason;
    private List<EventActivity.RescheduleInfo> reprogrammedReasons;

    public HistoryItem() {}

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setCanceled(boolean canceled) {
        isCanceled = canceled;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public List<EventActivity.RescheduleInfo> getReprogrammedReasons() {
        return reprogrammedReasons;
    }

    public void setReprogrammedReasons(List<EventActivity.RescheduleInfo> reprogrammedReasons) {
        this.reprogrammedReasons = reprogrammedReasons;
    }
}
