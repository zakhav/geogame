package com.locify.locifymobile.com.locify.locifymobile.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Created by vitaliy on 16.03.2016.
 */
public class GeoData {
    public String code;
    public Map<String, String> names;
    public Location location;
    public String type;
    public String status;
    public String url;
    public int founds;
//    @SerializedName("notfounds")
    public int notFounds;
    public double difficulty;
//    @SerializedName("last_modified")
    public Date lastModified;
//    @SerializedName("date_created")
    public Date dateCreated;
//    @SerializedName("date_hidden")
    public Date dateHidden;
    public ItemOwner owner;
    public Map<String, String> descriptions;
    public Map<String, String> hints;
    public Map<String, String> hints2;
    public String state;
    public String country;

    public GeoData() {
    }

    public String getName() {
        return names.get(Locale.ENGLISH.getLanguage());
    }

    public String getDescription() {
        return descriptions.get(Locale.ENGLISH.getLanguage());
    }

    public String getHint() {
        return hints.get(Locale.ENGLISH.getLanguage());
    }
}
