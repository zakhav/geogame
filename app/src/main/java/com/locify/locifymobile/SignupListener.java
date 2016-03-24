package com.locify.locifymobile;

/**
 * Created by vitaliy on 15.03.2016.
 */
public interface SignupListener {
    public void signupSucceded(String name, String email, String password);
    public void signupFailed(int statusCode);
}
