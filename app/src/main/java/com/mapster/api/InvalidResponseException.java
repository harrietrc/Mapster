package com.mapster.api;

/**
 * Created by Harriet on 7/27/2015.
 */
public class InvalidResponseException extends RuntimeException {

    public InvalidResponseException(String detailMessage) {
        super(detailMessage);
    }

}
