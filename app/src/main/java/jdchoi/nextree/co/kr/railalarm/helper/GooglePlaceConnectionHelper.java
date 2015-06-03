package jdchoi.nextree.co.kr.railalarm.helper;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2015-06-03.
 */
public class GooglePlaceConnectionHelper extends AsyncTask<String, String, String> {
    private static final String API_KEY = "AIzaSyD9YGLaYTb-AXDQMsPKMrMa3TXKaNaYPEg";
    private Location location;

    public GooglePlaceConnectionHelper(Location location) {
        this.location = location;
    }

    @Override
    protected String doInBackground(String... urls) {
        String reference = urls[0];
        // params comes from the execute() call: params[0] is the url.
        try {
            return downloadUrl(reference);
        } catch (IOException e) {
            return "Unable to retrieve web page. URL may be invalid.";
        }
    }

    private String downloadUrl(String searchUrl) throws IOException {
        String types ="subway_station";
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(searchUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestProperty("key", API_KEY);
            conn.setRequestProperty("location", location.getLatitude() + "," + location.getLongitude());
            conn.setRequestProperty("radius", "50"); // in meters
            conn.setRequestProperty("sensor", "false");
            conn.setRequestProperty("types", types);

            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("DEBUG_TAG", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }
}
