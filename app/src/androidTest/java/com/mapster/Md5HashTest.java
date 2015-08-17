package com.mapster;

import android.test.AndroidTestCase;

import com.mapster.webutils.Md5Hash;

import java.security.NoSuchAlgorithmException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by Harriet on 7/25/2015. Test case for MD5 hashing (was a point of failure for requests
 * to Expedia in the past)
 */
public class Md5HashTest extends AndroidTestCase {

    public void testMd5HashExpedia() throws NoSuchAlgorithmException {
        String testApiKey = "oq1zosfuv5bqwylztbwqflx7cp";
        String testSecretKey = "7nYzd5yp";
        long testTimeStamp = 1437788118;
        String expectedHash = "3fb3a760999c8039a81babbb9dff20d8";

        Md5Hash testHash = new Md5Hash();
        String actualHash = testHash.calculateExpediaSignature(testApiKey, testSecretKey, testTimeStamp);

        assertThat(actualHash, is(equalTo(expectedHash)));
    }
}
