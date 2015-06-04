package jdchoi.nextree.co.kr.railalarm.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.text.Html;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import jdchoi.nextree.co.kr.railalarm.util.StreamUtil;

/**
 * Created by Administrator on 2015-06-03.
 */
public class NearStationSearchHelper extends AsyncTask<String, String, String> {
    //Google Place한글화 될때까지 주석처리함.
    //private static final String API_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    //private static final String API_KEY = "AIzaSyDktmASMI6kOZYGRdHjsDTD5bzsO2rmOhY";
    private static final String API_URL = "https://apis.daum.net/local/v1/search/category.json?";
    private static final String API_KEY = "685dfa45eaeb466d4851b3f84703d1c6";
    private Context context;
    private Location location;
    private ProgressDialog pDialog;

    public NearStationSearchHelper(Context context, Location location) {
        this.context = context;
        this.location = location;
    }

    /**
     * Before starting background thread Show Progress Dialog
     * */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage(Html.fromHtml("<b>Search</b><br/>Current Station..."));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected String doInBackground(String... args) {
        try {
            return downloadUrl(new StringBuilder().append(API_URL)
                    .append("apikey=").append(API_KEY)
                    .append("&code=SW8")
                    .append("&location=").append(location.getLatitude()).append(",").append(location.getLongitude())
                    .append("&radius=100").toString());

                    //Google Place한글화 될때까지 주석처리함.
                    //.append("key=").append(API_KEY)
                    //.append("&location=").append(location.getLatitude()).append(",").append(location.getLongitude())
                    //.append("&radius=500")
                    //.append("&sensor=false")
                    //.append("&types=subway_station").toString());
        } catch (IOException e) {
            return "Unable to retrieve web page. URL may be invalid.";
        }
    }

    private String downloadUrl(String searchUrl) throws IOException {
        InputStream is = null;
        String contentAsString = "";
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 50000;

        try {
            URL url = new URL(searchUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();

            int response = conn.getResponseCode();

            if(response == 200) {
                is = conn.getInputStream();
                // Convert the InputStream into a string
                contentAsString = readIt(is);

                if(!contentAsString.equals("")){
                    try {
                        JSONObject obj = new JSONObject(contentAsString);
                        obj = obj.getJSONObject("channel");
                        JSONArray jsonArray = obj.getJSONArray("item");
                        if(jsonArray != null && jsonArray.length() > 0) {
                            obj = jsonArray.getJSONObject(0);
                            String title = obj.getString("title");
                            contentAsString = title.split(" ")[0].split("역")[0];
                        }else{
                            contentAsString = "";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        } finally {
            if (is != null) {
                is.close();
            }
        }

        return contentAsString;
    }
    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
        byte[] bytes = StreamUtil.getBytes(stream);
        return new String(bytes);
    }

    /**
     * After completing background task Dismiss the progress dialog
     * and show the data in UI
     * Always use runOnUiThread(new Runnable()) to update UI from background
     * thread, otherwise you will get error
     * **/
    protected void onPostExecute(String file_url) {
        // dismiss the dialog after getting all products
        pDialog.dismiss();
    }

}
