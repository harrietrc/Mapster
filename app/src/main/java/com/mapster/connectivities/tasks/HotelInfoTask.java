package com.mapster.connectivities.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.mapster.expedia.ExpediaRequest;

import java.security.NoSuchAlgorithmException;

/**
 * Created by Harriet on 5/24/2015.
 */
public class HotelInfoTask extends AsyncTask<Integer, Void, String> {

    private Context _context;

    public HotelInfoTask(Context context) {
        _context = context;
    }

    @Override
    protected String doInBackground(Integer... hotelIds) {
        int hotelId = hotelIds[0];
        ExpediaRequest exp = new ExpediaRequest(_context);

        String response = null;

        try {
            response = exp.request(hotelId);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return response;
    }
}