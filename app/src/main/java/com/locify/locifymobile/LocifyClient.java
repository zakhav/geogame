package com.locify.locifymobile;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.locify.locifymobile.com.locify.locifymobile.model.GeoResult;
import com.locify.locifymobile.com.locify.locifymobile.model.LogItem;
import com.locify.locifymobile.com.locify.locifymobile.model.SearchResultBuffer;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.auth.AuthScope;
import cz.msebera.android.httpclient.auth.UsernamePasswordCredentials;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by vitaliy on 15.03.2016.
 */
public class LocifyClient {
    private static final String TAG = "LocifyClient";
//    private static final String DATE_FORMAT = "yyyy-mm-dd'T'hh:mm:ss";
    public static final String LOCIFY_SERVER_BASE_URL = "http://10.0.2.2:9000";
    public static final String CONTENT_TYPE_FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_JSON = "application/json";
    private AsyncHttpClient client;

    JsonSerializer<Date> dateSerializer = new JsonSerializer<Date>() {
        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext
                context) {
            return src == null ? null : new JsonPrimitive(src.getTime());
        }
    };

    JsonDeserializer<Date> dateDeserializer = new JsonDeserializer<Date>() {
        @Override
        public Date deserialize(JsonElement json, Type typeOfT,
                                JsonDeserializationContext context) throws JsonParseException {
            return json == null ? null : new Date(json.getAsLong());
        }
    };

    private static LocifyClient instance;

    public static LocifyClient getInstance() {
        if(instance == null) {
            instance = new LocifyClient();
        }
        return instance;
    }

    private LocifyClient() {
        client = new AsyncHttpClient();
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return LOCIFY_SERVER_BASE_URL + relativeUrl;
    }

    public void login(Context context, final LoginListener loginListener, String userMail, String password) {
        List<NameValuePair> formData = new ArrayList<NameValuePair>();
        formData.add(new BasicNameValuePair("email", userMail));
        formData.add(new BasicNameValuePair("password", password));

        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formData);

            final ProgressDialog prgDialog = new ProgressDialog(context, R.style.AppTheme);
            prgDialog.setIndeterminate(true);
            prgDialog.setMessage("Authenticating...");
            prgDialog.show();

//            setCredentials(client, getAbsoluteUrl("/login"), userMail, password);

            client.post(context, getAbsoluteUrl("/login"), entity, CONTENT_TYPE_FORM_URL_ENCODED, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    // Hide Progress Dialog
                    prgDialog.hide();
                    loginListener.loginSucceded();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    // Hide Progress Dialog
                    prgDialog.hide();
                    Log.d(TAG, "Login failed. HTTP error: " + statusCode);
                    loginListener.loginFailed(statusCode);
                }
            });
        } catch (UnsupportedEncodingException uee) {
            Log.e(TAG, "Login - Unsupported encoding");
        }
    }

    public void logout(Context context) {
        client.get(context, getAbsoluteUrl("/logout"), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d(TAG, "Logout suceeeded. HTTP status: " + statusCode);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(TAG, "Logout failed. HTTP error: " + statusCode);
            }
        });
    }

    private void setCredentials(AsyncHttpClient client, String URL, String userMail, String password) {
        Uri parsed = Uri.parse(URL);
        client.clearCredentialsProvider();
        client.setCredentials(
                new AuthScope(parsed.getHost(), parsed.getPort() == -1 ? 80 : parsed.getPort()),
                new UsernamePasswordCredentials(userMail, password)
        );
    }

    public void retrieveItemList(Context context, final RetriveItemsListener listener, SearchResultBuffer searchBuffer) {
        client.get(getAbsoluteUrl("/geocaching/search"), searchBuffer.createRequestParams(),
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        InputStream contentStream = new ByteArrayInputStream(responseBody);
                        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, dateSerializer)
                                .registerTypeAdapter(Date.class, dateDeserializer).create();
                        Reader reader = new InputStreamReader(contentStream);
                        GeoResult response = gson.fromJson(reader, GeoResult.class);
                        listener.itemsRetrieved(response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        listener.requestFailed(statusCode);
                    }
                });
    }

    public void retrieveItemLogs(Context context, final RetriveItemLogsListener listener, String itemCode) {
        client.get(getAbsoluteUrl("/geocaching/logs/" + itemCode), null,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        InputStream contentStream = new ByteArrayInputStream(responseBody);
                        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, dateSerializer)
                                .registerTypeAdapter(Date.class, dateDeserializer).create();
                        Reader reader = new InputStreamReader(contentStream);
                        Type listType = new TypeToken<ArrayList<LogItem>>(){}.getType();
                        List<LogItem> response = gson.fromJson(reader, listType);
                        listener.itemLogsRetrieved(response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        listener.requestFailed(statusCode);
                    }
                });
    }

    public void sendCurrentLocation(Context context, Location location) {
        try {
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("lat", location.getLatitude());
            jsonParams.put("lng", location.getLongitude());
            StringEntity entity = new StringEntity(jsonParams.toString());
            client.post(context, getAbsoluteUrl("/geocaching/location"), entity, CONTENT_TYPE_JSON,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.d(TAG, "Send current location - success");
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.e(TAG, "Send current location - failed. Status code: " + statusCode);
                    }
                });
        } catch (UnsupportedEncodingException uee) {
            Log.e(TAG, "Send current location - Unsupported encoding");
        } catch (JSONException je) {
            Log.e(TAG, "Send current location - JSONException");
        }
    }

    public void sendResetInstructions(Context context, final String email, final ResetPasswordListener listener) {
        try {
            List<NameValuePair> formData = new ArrayList<NameValuePair>();
            formData.add(new BasicNameValuePair("email", email));

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formData);
            client.post(context, getAbsoluteUrl("/login/password/forgot"), entity, CONTENT_TYPE_FORM_URL_ENCODED,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.d(TAG, "Send reset password instructions - success");
                        listener.resetRequestSucceded(email);
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.e(TAG, "Send reset password instructions - failed. Status code: " + statusCode);
                        listener.resetRequestFailed(statusCode);
                    }
                });
        } catch (UnsupportedEncodingException uee) {
            Log.e(TAG, "Send reset password instructions - Unsupported encoding");
        }
    }

    public void signUp(Context context, final String name, final String email, final String password, final SignupListener listener) {
        try {
            List<NameValuePair> formData = new ArrayList<NameValuePair>();
            formData.add(new BasicNameValuePair("name", name));
            formData.add(new BasicNameValuePair("email", email));
            formData.add(new BasicNameValuePair("password", password));
            formData.add(new BasicNameValuePair("repeatPassword", password));

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formData);
            client.post(context, getAbsoluteUrl("/signup"), entity, CONTENT_TYPE_FORM_URL_ENCODED,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.d(TAG, "SignUp - success");
                        listener.signupSucceded(name, email, password);
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.e(TAG, "SignUp - failed. Status code: " + statusCode);
                        listener.signupFailed(statusCode);
                    }
                });
        } catch (UnsupportedEncodingException uee) {
            Log.e(TAG, "SignUp - Unsupported encoding");
        }
    }
}
