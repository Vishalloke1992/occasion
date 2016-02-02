package vishal.com.newprivacymod;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FamilyPage extends Activity {

    String name, url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String url = "http://192.168.0.7/occasions/getupdate.php";
        new GetUpdate().execute(url);
       /* try {
            a = new JSONArray(s.toString());

            int length = a.length();
            for (int i = 0; i < length; i++) {
                //Information information=new Information();
                JSONObject jsonObject = a.getJSONObject(i);
                name = jsonObject.getString("userid");
                url = jsonObject.getString("imageurl");
                // data.add(information);

                //setImages(Integer.parseInt(name));

                Toast.makeText(FamilyPage.this, name + " http://192.168.0.7/occasions/" + url, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }*/


        //new UpdatePage().execute();

        /*SharedPreferences.Editor editor = getSharedPreferences("occassions", MODE_PRIVATE).edit();
        editor.putBoolean("autodownload", true);
        editor.commit();

        SharedPreferences prefs = getSharedPreferences("occassions", MODE_PRIVATE);
        String restoredText = prefs.getString("text", null);
        if (restoredText != null) {
            boolean autodw=prefs.getBoolean("autodownload",true);
        }*/
    }


    private class GetUpdate extends AsyncTask<String, Void, Integer>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }



        @Override
        protected Integer doInBackground(String... params) {
            InputStream inputStream = null;
            Integer result = 0;
            HttpURLConnection urlConnection = null;

            try {
                /* forming th java.net.URL object */
                URL url = new URL(params[0]);

                urlConnection = (HttpURLConnection) url.openConnection();

                /* for Get request */
                urlConnection.setRequestMethod("GET");

                int statusCode = urlConnection.getResponseCode();

                /* 200 represents HTTP OK */
                if (statusCode ==  200) {

                    BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        response.append(line);
                    }

                  //  parseResult(response.toString());
                    result = 1; // Successful
                }else{
                    result = 0; //"Failed to fetch data!";
                }

            } catch (Exception e) {
               // Log.d(TAG, e.getLocalizedMessage());
            }

            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            try {
                JSONArray a = new JSONArray(result.toString());

                int length = a.length();
                for (int i = 0; i < length; i++) {
                    //Information information=new Information();
                    JSONObject jsonObject = a.getJSONObject(i);
                    name = jsonObject.getString("userid");
                    url = jsonObject.getString("imageurl");
                    // data.add(information);

                    //setImages(Integer.parseInt(name));

                    Toast.makeText(FamilyPage.this, name + " http://192.168.0.7/occasions/" + url, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
