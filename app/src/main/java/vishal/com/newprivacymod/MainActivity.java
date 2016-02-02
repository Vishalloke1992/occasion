package vishal.com.newprivacymod;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class MainActivity extends AppCompatActivity {

    ProgressDialog prgDialog;
    String encodedString;
    Spinner spinner;
    RequestParams params = new RequestParams();
    String imgPath, fileName;
    private String selectedImagePath;
    Bitmap bitmap;
    private static int RESULT_LOAD_IMG = 1;
    private static final int SELECT_PICTURE = 1;

    int gr_id[]=new int[50];
    ArrayList<String> list = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = (Spinner)findViewById(R.id.spinner);

        new DownloadSpinnerContent().execute();
        String result = null;
        StringBuilder sb=null;
        //http post

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        spinner.setAdapter(adapter);

        prgDialog = new ProgressDialog(this);
        // Set Cancelable as False
        prgDialog.setCancelable(false);
    }

    public void loadImagefromGallery(View view) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
        {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
        }
        else
        {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
        }


    }

    // When Image is selected from Gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && data.getData() != null && resultCode == RESULT_OK) {
            ParcelFileDescriptor pfd;
            try {
                ImageView image=(ImageView)findViewById(R.id.imgView);
                pfd = getContentResolver().openFileDescriptor(data.getData(), "r");
                FileDescriptor fd = pfd.getFileDescriptor();
                Bitmap img = BitmapFactory.decodeFileDescriptor(fd);
                pfd.close();
                image.setImageBitmap(img);  //image represent ImageVIew to display picked image

                if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                    int flags = data.getFlags()&(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    Uri u = data.getData();
                    getContentResolver().takePersistableUriPermission(u,flags);
                    String id = u.getLastPathSegment().split(":")[1];
                    final String[] imageColumns = {MediaStore.Images.Media.DATA};
                    final String imageOrderBy = null;
                    Uri u1 =Uri.EMPTY;
                    String state = Environment.getExternalStorageState();
                    if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
                        u1 = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
                    else
                        u1 = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    Cursor c = managedQuery(u1, imageColumns, MediaStore.Images.Media._ID + "=" + id, null, imageOrderBy);
                    if (c.moveToFirst()) {
                        imgPath = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));  //imgPath represents string variable to hold the path of image
                    }
                } else {
                    Uri imgUri = data.getData();
                    Cursor c1 = getContentResolver().query(imgUri, null, null, null, null);
                    if (c1 == null) {
                        imgPath = imgUri.getPath();  //imgPath represents string variable to hold the path of image
                    } else {
                        c1.moveToFirst();
                        int idx = c1.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                        imgPath = c1.getString(idx);  //imgPath represents string variable to hold the path of image
                        c1.close();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }catch(Exception ea)
            {}
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
        // When Upload button is clicked
        public void uploadImage(View v) {
            // When Image is selected from Gallery
            if (imgPath != null && !imgPath.isEmpty()) {
                prgDialog.setMessage("Converting Image to Binary Data");
                prgDialog.show();
                // Convert image to String using Base64
                encodeImagetoString();
                // When Image is not selected from Gallery
            } else {
                Toast.makeText(
                        getApplicationContext(),
                        "You must select image from gallery before you try to upload",
                        Toast.LENGTH_LONG).show();
            }
        }

        // AsyncTask - To convert Image to String
        public void encodeImagetoString() {
            new AsyncTask<Void, Void, String>() {

                protected void onPreExecute() {

                };

                @Override
                protected String doInBackground(Void... params) {
                    BitmapFactory.Options options = null;
                    options = new BitmapFactory.Options();
                    options.inSampleSize = 3;
                    bitmap = BitmapFactory.decodeFile(imgPath,
                            options);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    // Must compress the Image to reduce image size to make upload easy
                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                    byte[] byte_arr = stream.toByteArray();
                    // Encode Image to String
                    encodedString = Base64.encodeToString(byte_arr, 0);
                    return "";
                }

                @Override
                protected void onPostExecute(String msg) {
                    prgDialog.setMessage("Calling Upload");
                    // Put converted Image string into Async Http Post param
                    params.put("image", encodedString);
                    params.put("groupid",gr_id[spinner.getSelectedItemPosition()]);
                    params.put("userid", 1);//userid will be the user who has logged in from the application
                    // Trigger Image upload
                    triggerImageUpload();
                }
            }.execute(null, null, null);
        }

        public void triggerImageUpload() {
            makeHTTPCall();
        }

    // Make Http call to upload Image to Php server
    public void makeHTTPCall() {
        prgDialog.setMessage("Invoking Php");
        AsyncHttpClient client = new AsyncHttpClient();
        // Don't forget to change the IP address to your LAN address. Port no as well.
        client.post("http://192.168.0.7/occasions/upload_image.php",
                params, new AsyncHttpResponseHandler() {
                    // When the response returned by REST has Http
                    // response code '200'


                    @Override
                    public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {

                        // Hide Progress Dialog
                        prgDialog.hide();
                        Toast.makeText(getApplicationContext(), "hello"+bytes.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                    // When the response returned by REST has Http
                    // response code other than '200' such as '404',
                    // '500' or '403' etc

                    @Override
                    public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {

                        // Hide Progress Dialog
                        prgDialog.hide();
                        // When Http response code is '404'
                        if (statusCode == 404) {
                            Toast.makeText(getApplicationContext(),
                                    "Requested resource not found",
                                    Toast.LENGTH_LONG).show();
                        }
                        // When Http response code is '500'
                        else if (statusCode == 500) {
                            Toast.makeText(getApplicationContext(),
                                    "Something went wrong at server end",
                                    Toast.LENGTH_LONG).show();
                        }
                        // When Http response code other than 404, 500
                        else {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Error Occured n Most Common Error: n1. Device not connected to Internetn2. Web App is not deployed in App servern3. App server is not runningn HTTP Status code : "
                                            + statusCode, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }


                });
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // Dismiss the progress bar when application is closed
        if (prgDialog != null) {
            prgDialog.dismiss();
        }
    }

    private class DownloadSpinnerContent extends AsyncTask{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            String gr_name;

            try{
               JSONArray jArray = new JSONArray(o.toString());
                JSONObject json_data=null;

                for(int i=0;i<jArray.length();i++){
                    json_data = jArray.getJSONObject(i);
                    gr_id[i]=json_data.getInt("groupid");
                    gr_name=json_data.getString("groupname");

                    list.add(gr_name);
                    adapter.notifyDataSetChanged();
                }

            }catch(JSONException e1){
                Toast.makeText(getBaseContext(), "No Food Found", Toast.LENGTH_LONG).show();
            }



        }

        @Override
        protected Object doInBackground(Object[] objects) {
            InputStream is = null;
            String result=null;
            try{
                HttpClient httpclient = new DefaultHttpClient();


                HttpPost httppost = new HttpPost("http://192.168.0.7/occasions/getgroupdetails.php");
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("userid", "1"));//here the user id will be sent to server to get the groups he is in for spinner filling
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            }catch(Exception e){
                Log.e("log_tag", "Error in http connection" + e.toString());
                Toast.makeText(MainActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
            }



            //convert response to string
            try{
                StringBuilder sb=null;
                BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
                sb = new StringBuilder();
                sb.append(reader.readLine() + "\n");
                String line="0";

                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                is.close();
                result=sb.toString();
                return result;
            }catch(Exception e){
                Log.e("log_tag", "Error converting result " + e.toString());
            }
            return result;
        }
    }
}

