package com.example.minnie.alpha2_app1;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    // Declare widgets
    Button sendBtn, receiveBtn, clearBtn;
    TextView sendTv, receiveTv;
    Toolbar toolbar;

    String responseStr = "";
    String TAG = "Alpha2_App1";

    // Send JSON string to:
    //String fullURL = "http://www.155.41.125.181/post.php";
    //String serverIP = "155.41.125.181"; String serverPage = "server_v1.php";
    String serverIP = "www.httpbin.org"; final String serverPage = "post";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Buttons
        sendBtn = (Button)findViewById(R.id.send_btn);
        receiveBtn = (Button)findViewById(R.id.receive_btn);
        clearBtn = (Button)findViewById(R.id.clear_btn);
        sendBtn.setOnClickListener(this);
        receiveBtn.setOnClickListener(this);
        clearBtn.setOnClickListener(this);

        // TextViews
        sendTv = (TextView)findViewById(R.id.send_textView);
        receiveTv = (TextView)findViewById(R.id.receive_textView);

        // Toolbar
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setLogo(R.drawable.alpha2_app1_logo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu (i.e. adds items to the tool bar)
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                Toast.makeText(getApplicationContext(), "Nothing to see here...", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Assign tasks for buttons
    public void onClick(View v) {
        if (v == sendBtn) {
            // Check for network connection
            String fullURL = "http://" + serverIP + "/" + serverPage;
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nwInfo = connMgr.getActiveNetworkInfo();
            Log.d(TAG, fullURL);
            // If network is present, start AsyncTask to connect to given URL
            if ((nwInfo != null) && (nwInfo.isConnected())) {
                new StartAsyncTask().execute(fullURL);
            } else {
                sendTv.setText("ERROR No network connection detected.");
            }
        }

        if (v == receiveBtn) {

        }

        if (v == clearBtn) {
            // Clear out text views
            sendTv.setText("");
            receiveTv.setText("");
        }
    }

    protected class StartAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // Params come from the execute() call: params[0] is the url
            try {
                connectToURL(urls[0]);
                return responseStr;
            } catch (IOException e) {
                return responseStr = "Error: could not connect to URL. Please check URL";
            }
        }

        protected void onPostExecute(String responseStr) {
            /*if (responseStr != null) {
                sendTv.setText("Sent the following JSON string to URL");
            }*/

            sendTv.setText(responseStr);
        }

        private String connectToURL(String url) throws IOException {
            try {
                URL myURL = new URL(url);
                HttpURLConnection conn = (HttpURLConnection)myURL.openConnection();

                conn.setReadTimeout(10 * 1000); // milliseconds
                conn.setConnectTimeout(10 * 1000); // milliseconds
                conn.setDoOutput(true);
                //conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                //conn.setRequestProperty("Accept", "application/json");
                conn.connect();

                // Create JSON object
                JSONObject sendJsonOb = new JSONObject();
                sendJsonOb.put("key0", "value0");
                sendJsonOb.put("key1", "value1");
                sendJsonOb.put("key2", "value2");
                Log.d(TAG, "sendJsonOb's content: " + sendJsonOb.toString());

                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                osw.write(sendJsonOb.toString());
                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);
                osw.flush();
                osw.close();

                // TODO Change this later (temp fix to avoid IOException FileNotFoundException)
                if (responseCode > 0) {
                    responseStr = "The following JSON string was sent to " + url + ": " + sendJsonOb.toString();
                } else {
                    responseStr = "Error: JSON string was not sent.";
                }
                return responseStr;

                  /*
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                Log.d(TAG, "URL in HttpPost(url): " + url);

                // Create JSON object
                JSONObject sendThis = new JSONObject();
                sendThis.put("key", "value");

                // Set HTTP parameters
                StringEntity se = new StringEntity(sendThis.toString());
                httpPost.setEntity(se);
                httpPost.setHeader("Content-type", "application/json");

                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity entity = httpResponse.getEntity();

                if (entity != null) {
                    InputStream response = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response));
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                    responseStr = content.toString();
                    response.close();
                }
                */
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return responseStr = "ERROR MalformedURLException caught: " + e;
            } catch (IOException e) {
                e.printStackTrace();
                return responseStr = "ERROR IOException caught: " + e;
            } catch (JSONException e) {
                e.printStackTrace();
                return responseStr = "ERROR JSONException caught: " + e;
            }
        }
    }
}
