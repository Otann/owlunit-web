package com.manymonkeys.moviesstory.service;

import com.google.gson.Gson;
import com.manymonkeys.moviesstory.model.FacebookMovies;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public class FacebookIntegrationServiceImpl implements FacebookIntegrationService {

    public static final String UTF_8 = "UTF-8";

    public final static String FB_GRAPH_URL = "https://graph.facebook.com/";

    public static final String OAUTH_URL = "https://www.facebook.com/dialog/oauth?" +
            "client_id=%s" +
            "&redirect_uri=%s" +
            "&scope=publish_stream,manage_pages,offline_access,read_stream";

    public static final String ACCESS_TOKEN_URL = "https://graph.facebook.com/oauth/access_token?client_id=%s" +
            "&redirect_uri=%s" +
            "&client_secret=%s" +
            "&code=%s";

    private String appId;

    private String appSecret;

    private String redirectUri;

    public FacebookIntegrationServiceImpl(String appId, String appSecret, String redirectUri) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.redirectUri = redirectUri;
    }

    public String constructApplicationAuthenticaionUrl() throws UnsupportedEncodingException {
        return String.format(OAUTH_URL, appId, URLEncoder.encode(redirectUri, UTF_8));
    }

    public String retrieveAccessToken(String code) throws Exception {
        String url = String.format(ACCESS_TOKEN_URL, appId, redirectUri, appSecret, code);

        URL tokenEndpoint = new URL(url);

        URLConnection connection = tokenEndpoint.openConnection();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String line;

        StringBuffer buff = new StringBuffer();

        while ((line = in.readLine()) != null) {
            buff.append(line);
        }

        in.close();

        String response = buff.toString();

        if (response.contains("error")) {
            //Todo throw a "nice" type-specific exception here
            throw new Exception(response.toString());
        } else {
            /* fyi: response should be like "access_token=blablabla&expires=blalba" */
            return buff.toString().split("&")[0].split("=")[1];
        }
    }

    public FacebookMovies retrieveFacebookMovies(String userAccessToken) throws IOException {
        return FacebookMovies.deserialize(
                retriveJsonStringRequest(
                        String.format("https://graph.facebook.com/me/movies?access_token=%s", userAccessToken)
                )
        );
    }

    /*----------------------------------------\
    |  P R I V A T E  S T A T I C  S T U F F  |
    \========================================*/

    private static String retriveJsonStringRequest(String requestUrl) throws IOException {
        StringBuilder result = new StringBuilder();

        HttpURLConnection urlConnection = (HttpURLConnection) (new URL(requestUrl).openConnection());

        if (urlConnection.getResponseCode() != 200) {
            //Todo Throw a "nice" checked exception here
            throw new IllegalStateException("Failed to perform a request to facebook graph api");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        reader.close();

        return result.toString();
    }

}
