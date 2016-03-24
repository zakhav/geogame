package com.locify.locifymobile.com.locify.locifymobile.model;

/**
 * Created by vitaliy on 17.03.2016.
 */
public class Location {
    public double latitude;
    public double longitude;

    public Location() {
    }

    public Location(double latitude, double longitude) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return latitude + "," + longitude;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj instanceof Location) {
            Location other = (Location)obj;
            return (latitude == other.latitude) && (longitude == other.longitude);
        }
        return false;
    }

    public static Location parse(String value) {
        int separatorIndex = value.indexOf(',');
        if(separatorIndex == -1) {
            throw new IllegalArgumentException("Can't convert to Location: couldn't find separator");
        }
        String latitudeValue = value.substring(0, separatorIndex);
        String longitudeValue = value.substring(separatorIndex + 1);
        try {
            double latitude = Double.parseDouble(latitudeValue);
            double longitude = Double.parseDouble(longitudeValue);
            return new Location(latitude, longitude);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Can't parse Location: " + value);
        }
    }

    public android.location.Location getPoint() {
        android.location.Location targetLocation = new android.location.Location("");//provider name is unecessary
        targetLocation.setLatitude(latitude);//your coords of course
        targetLocation.setLongitude(longitude);
        return targetLocation;
//        return new Location(location.getLatitude(), location.getLongitude());
    }
}
