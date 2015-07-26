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
    public String getRequest(ApiRequest request) {
        String url = request.constructUrl();

        HttpConnection conn = new HttpConnection();
        String response = null;
        try {
            response = conn.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

}
