package com.locify.locifymobile.com.locify.locifymobile.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by vitaliy on 16.03.2016.
 */
public class LogData {
    public ItemOwner user;
    @SerializedName("cache_code")
    public String cacheCode;
    public Date date;
    public String type;
    @SerializedName("was_recommended")
    public boolean wasRecommended;
    public String comment;

    public LogData() {
    }
}
