package com.mapster.api;

import com.mapster.connectivities.HttpConnection;

import java.io.IOException;

/**
 * Created by Harriet on 7/26/2015.
 */
public abstract class Api {

    /**
     * Makes a GET request to a given API and returns the response as a string.
     */
    protected ApiRequest getRequest(ApiRequest request) {
        String url = request.getUrl();

        HttpConnection conn = new HttpConnection();
        try {
            String response = conn.readUrl(url);
            request.setResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return request;
    }

}
