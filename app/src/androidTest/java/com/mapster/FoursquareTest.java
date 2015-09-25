package com.mapster;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.api.ApiRequest;
import com.mapster.api.foursquare.Foursquare;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by Harriet on 7/27/2015. Warning/possible flaw: assumes that the format for URLs stays
 * constant (allows only for reordering of query parameters)
 */
@RunWith(MockitoJUnitRunner.class)
public class FoursquareTest  {

    private static final String MOCK_API_KEY = "MockApiKey";
    private static final String MOCK_SECRET_KEY = "MockSecretKey";

    @Mock
    Context _mockContext; // Required to get API keys

    @Test
    public void testConstructExploreUrl() {
        String expectedUrl = "";

//        Context _mockContext = mock(Context.class);

        when(_mockContext.getString(R.string.FOURSQUARE_CLIENT_ID)).thenReturn(MOCK_API_KEY);
        when(_mockContext.getString(R.string.FOURSQUARE_CLIENT_SECRET)).thenReturn(MOCK_SECRET_KEY);

        LatLng mockLatLng = new LatLng(10.0, 10.0);

        Foursquare testApi = new Foursquare(_mockContext);
        ApiRequest testRequest = testApi.exploreNearbyVenuesRequest(mockLatLng, 3000, 15);
        String actualUrl = testRequest.getUrl();

        // TODO Allow for reordering of parameters
        assertThat(actualUrl, is(equalTo(expectedUrl)));
    }

}
