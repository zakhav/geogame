package com.locify.locifymobile;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
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

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by vitaliy on 15.03.2016.
 */
public class LocifyClient {
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String TAG = "LocifyClient";
    public static final String LOCIFY_SERVER_BASE_URL = "http://192.168.1.3:9000";
    public static final String CONTENT_TYPE_FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_JSON = "application/json";

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
//        client = new AsyncHttpClient();
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return LOCIFY_SERVER_BASE_URL + relativeUrl;
    }

    public static interface HttpClientResponseHandler {
        void onSuccess(int statusCode, Header[] headers, byte[] responseBody);
        void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error);
    }

    private static class HttpRequestTask extends AsyncTask<Void, Void, HttpResponseData> {
        private String requestUrl;
        private String httpMethod;
        private String contentType;
        private byte requestBody[];
        private HttpClientResponseHandler listener;

        public HttpRequestTask(String requestUrl, String httpMethod, String contentType, byte requestBody[], HttpClientResponseHandler listener) {
            this.requestUrl = requestUrl;
            this.httpMethod = httpMethod;
            this.contentType = contentType;
            this.requestBody = requestBody;
            this.listener = listener;
        }

        @Override
        protected HttpResponseData doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(requestUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod(httpMethod);
                if(contentType != null) {
                    urlConnection.setRequestProperty("Content-Type", contentType);
                }
                if(requestBody != null && requestBody.length > 0) {
                    urlConnection.setDoOutput(true);
                    urlConnection.setChunkedStreamingMode(0);
                    OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    out.write(requestBody);
                    out.close();
                }

                urlConnection.connect();
                int code = urlConnection.getResponseCode();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                IOUtils.copy(urlConnection.getInputStream(), bos);
                byte responseBody[] = bos.toByteArray();

                List<Header> headers = new ArrayList<Header>();
                Map<String, List<String>> map = urlConnection.getHeaderFields();

                for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                    String headerName = entry.getKey();
                    String headerValue = "";
                    List<String> headerValueList = entry.getValue();
                    if(!headerValueList.isEmpty()) {
                        headerValue = headerValueList.get(0);
                    }
                    if(headerName != null) {
                        headers.add(new BasicHeader(headerName, headerValue));
                    }
                }
                return new HttpResponseData(code, responseBody, headers.toArray(new Header[headers.size()]), null);
            } catch (IOException ioe) {
                return new HttpResponseData(-1, null, null, ioe);
            } catch (Exception ex) {
                return new HttpResponseData(-1, null, null, ex);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(HttpResponseData response) {
            if(listener != null) {
                if(response.statusCode >= HttpURLConnection.HTTP_OK
                        && response.statusCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                    listener.onSuccess(response.statusCode, response.headers, response.responseBody);
                } else {
                    listener.onFailure(response.statusCode, response.headers, response.responseBody, response.error);
                }
            }
        }
    }

    private static class HttpResponseData {
        public int statusCode;
        public byte[] responseBody;
        public Header[] headers;
        public Throwable error;

        public HttpResponseData(int statusCode, byte[] responseBody, Header[] headers, Throwable error) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
            this.headers = headers;
            this.error = error;
        }
    }

    public void login(Context context, final LoginListener loginListener, String userMail, String password) {
        HttpClientResponseHandler loginResponseHandler = new HttpClientResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                // check if we have not been redirected to login page
                boolean redirectToLogin = false;
                for(Header header: headers) {
                    if(header.getName().equals("Set-Cookie")) {
                        redirectToLogin = true;
                        break;
                    }
                }
                if(!redirectToLogin) {
                    loginListener.loginSucceded();
                } else {
                    loginListener.loginFailed(401);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(TAG, "Login failed. HTTP error: " + statusCode);
                loginListener.loginFailed(statusCode);
            }
        };

        List<NameValuePair> formData = new ArrayList<NameValuePair>();
        formData.add(new BasicNameValuePair("email", userMail));
        formData.add(new BasicNameValuePair("password", password));

        try {
            byte requestBody[] = getQuery(formData).getBytes(DEFAULT_ENCODING);
            new HttpRequestTask(getAbsoluteUrl("/login"), "POST", CONTENT_TYPE_FORM_URL_ENCODED, requestBody, loginResponseHandler).execute();
        } catch (UnsupportedEncodingException uee) {
            Log.e(TAG, "Login - Unsupported encoding");
        }
    }

    public void logout(Context context) {
        HttpClientResponseHandler logoutResponseHandler = new HttpClientResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d(TAG, "Logout suceeeded. HTTP status: " + statusCode);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(TAG, "Logout failed. HTTP error: " + statusCode);
            }
        };
        new HttpRequestTask(getAbsoluteUrl("/logout"), "GET", null, null, logoutResponseHandler).execute();
    }

    public void retrieveItemList(Context context, final RetriveItemsListener listener, SearchResultBuffer searchBuffer) {
        HttpClientResponseHandler itemListResponseHandler = new HttpClientResponseHandler() {
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
        };
        String searchQueryParams = searchBuffer.createRequestQuery();
        new HttpRequestTask(getAbsoluteUrl("/geocaching/search" + "?" + searchQueryParams), "GET", null, null, itemListResponseHandler).execute();
    }

    public void retrieveItemLogs(Context context, final RetriveItemLogsListener listener, String itemCode) {
        HttpClientResponseHandler itemLogsResponseHandler = new HttpClientResponseHandler() {
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
        };
        new HttpRequestTask(getAbsoluteUrl("/geocaching/logs/" + itemCode), "GET", null, null, itemLogsResponseHandler).execute();
    }

    public void sendCurrentLocation(Context context, Location location) {
        HttpClientResponseHandler sendLocationResponseHandler = new HttpClientResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d(TAG, "Send current location - success");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(TAG, "Send current location - failed. Status code: " + statusCode);
            }
        };
        try {
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("lat", location.getLatitude());
            jsonParams.put("lng", location.getLongitude());
            byte requestBody[] = jsonParams.toString().getBytes(DEFAULT_ENCODING);
            new HttpRequestTask(getAbsoluteUrl("/geocaching/location"), "POST", CONTENT_TYPE_JSON, requestBody, sendLocationResponseHandler).execute();
        } catch (UnsupportedEncodingException uee) {
            Log.e(TAG, "Login - Unsupported encoding");
        } catch (JSONException je) {
            Log.e(TAG, "Send current location - JSONException");
        }
    }

    public void sendResetInstructions(Context context, final String email, final ResetPasswordListener listener) {
        HttpClientResponseHandler resetInstructionsResponseHandler = new HttpClientResponseHandler() {
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
        };
        try {
            List<NameValuePair> formData = new ArrayList<NameValuePair>();
            formData.add(new BasicNameValuePair("email", email));
            byte requestBody[] = getQuery(formData).getBytes(DEFAULT_ENCODING);
            new HttpRequestTask(getAbsoluteUrl("/login/password/forgot"), "POST", CONTENT_TYPE_FORM_URL_ENCODED, requestBody, resetInstructionsResponseHandler).execute();
        } catch (UnsupportedEncodingException uee) {
            Log.e(TAG, "Login - Unsupported encoding");
        }
    }

    public void signUp(Context context, final String name, final String email, final String password, final SignupListener listener) {
        HttpClientResponseHandler signUpResponseHandler = new HttpClientResponseHandler() {
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
        };
        try {
            List<NameValuePair> formData = new ArrayList<NameValuePair>();
            formData.add(new BasicNameValuePair("name", name));
            formData.add(new BasicNameValuePair("email", email));
            formData.add(new BasicNameValuePair("password", password));
            formData.add(new BasicNameValuePair("repeatPassword", password));
            byte requestBody[] = getQuery(formData).getBytes(DEFAULT_ENCODING);
            new HttpRequestTask(getAbsoluteUrl("/login/password/forgot"), "POST", CONTENT_TYPE_FORM_URL_ENCODED, requestBody, signUpResponseHandler).execute();
        } catch (UnsupportedEncodingException uee) {
            Log.e(TAG, "SignUp - Unsupported encoding");
        }
    }

    private static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), DEFAULT_ENCODING));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), DEFAULT_ENCODING));
        }

        return result.toString();
    }
}
