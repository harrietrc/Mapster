package com.mapster.infowindow.listeners;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

/**
 * Created by Harriet on 7/29/2015.
 */
public class WebsiteButtonListener implements View.OnClickListener {

    private Context _context;
    private String _url;

    public WebsiteButtonListener(Context context, String url) {
        _context = context;
        _url = url;
    }

    @Override
    public void onClick(View websiteButton) {
        Intent webIntent = new Intent(Intent.ACTION_VIEW);
        webIntent.setData(Uri.parse(_url)); // Assume it starts with http://
        _context.startActivity(webIntent);
    }
}
