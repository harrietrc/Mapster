package com.mapster.connectivities.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.mapster.expedia.Expedia;

import java.security.NoSuchAlgorithmException;

/**
 * Created by Harriet on 5/24/2015.
 */
public class ExpediaHotelInfoTask extends AsyncTask<Integer, Void, String> {

    private Context _context;

    public ExpediaHotelInfoTask(Context context) {
        _context = context;
    }

    @Override
    protected String doInBackground(Integer... hotelIds) {
        int hotelId = hotelIds[0];
        Expedia exp = new Expedia(_context);

        // Make a request to the Expedia API in order to retrieve hotel info
        String response = exp.hotelInfoRequest(hotelId);

        return response;
    }
}