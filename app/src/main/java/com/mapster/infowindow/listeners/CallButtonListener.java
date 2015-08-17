package com.mapster.infowindow.listeners;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

/**
 * Created by Harriet on 7/29/2015.
 */
public class CallButtonListener implements View.OnClickListener {

    private Context _context;
    private String _phoneNumber;

    public CallButtonListener(Context context, String phoneNumber) {
        _context = context;
        _phoneNumber = phoneNumber;
    }

    @Override
    public void onClick(View callButton) {
        // Dials the phone number (doesn't call it)
        Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
        phoneIntent.setData(Uri.parse("tel:" + _phoneNumber));
        _context.startActivity(phoneIntent);
    }
}
