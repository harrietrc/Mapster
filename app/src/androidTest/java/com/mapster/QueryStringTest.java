package com.mapster;

import android.test.AndroidTestCase;

import com.mapster.webutils.QueryString;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Harriet on 7/26/2015. Pretty basic.
 */
public class QueryStringTest extends AndroidTestCase {

    public void testMapToQueryString() {
        String expectedQueryString = "?a=v1&b=v2&c=v3";

        Map<String, String> testFields = new HashMap<>();
        testFields.put("a", "v1");
        testFields.put("b", "v2");
        testFields.put("c", "v3");

        QueryString qs = new QueryString(testFields);
        String actualQueryString = qs.toString();

        assertThat(actualQueryString, is(equalTo(expectedQueryString)));
    }
}
