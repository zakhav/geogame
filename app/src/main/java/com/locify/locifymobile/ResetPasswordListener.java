package com.locify.locifymobile;

/**
 * Created by vitaliy on 15.03.2016.
 */
public interface ResetPasswordListener {
    public void resetRequestSucceded(String email);
    public void resetRequestFailed(int statusCode);
}
