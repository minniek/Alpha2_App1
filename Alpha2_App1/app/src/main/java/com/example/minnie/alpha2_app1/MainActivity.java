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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    Boolean isSend; // flag to determine GET or POST request execution

    // Send JSON string to:
    String urlSend = "http://www.httpbin.org/post"; // A test server that accepts post requests

    // Receive JSON string from:
    String urlReceive = "http://jsonip.com/"; // Returns public IP of device

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Buttons
        sendBtn = (Button)findViewById(R.id.send_btn); sendBtn.setOnClickListener(this);
        receiveBtn = (Button)findViewById(R.id.receive_btn); receiveBtn.setOnClickListener(this);
        clearBtn = (Button)findViewById(R.id.clear_btn); clearBtn.setOnClickListener(this);

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
            // Set flag to execute POST request in AsyncTask
            isSend = true;

            // Check for network connection
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nwInfo = connMgr.getActiveNetworkInfo();
            // If network is present, start AsyncTask to connect to given URL
            if ((nwInfo != null) && (nwInfo.isConnected())) {
                new StartAsyncTask().execute(urlSend);
            } else {
                sendTv.setText("ERROR No network connection detected.");
            }
        }

        if (v == receiveBtn) {
            // Set flag to execute GET request in AsyncTask
            isSend = false;

            // Check for network connection
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nwInfo = connMgr.getActiveNetworkInfo();
            // If network is present, start AsyncTask to connect to given URL
            if ((nwInfo != null) && (nwInfo.isConnected())) {
                new StartAsyncTask().execute(urlReceive);
            } else {
                receiveTv.setText("ERROR No network connection detected.");
            }
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
            if (isSend == true) {
                sendTv.setText("Successfully sent JSON string to " + urlSend + "\n\n" + "The following" +
                        " is the response from the server: " + responseStr);
            } else if (isSend == false) {
                Gson gson = new Gson();
                JsonIP jsonIp = gson.fromJson(responseStr, JsonIP.class);
                receiveTv.setText("Successfully received JSON string from " + urlReceive + "\n\n" + "The following" +
                        " is the response from the server: " + jsonIp);
            }
        }

        private String connectToURL(String url) throws IOException {
            // Execute the following if "Send" button is pressed
            if (isSend == true) {
                try {
                    URL myURL = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) myURL.openConnection();

                    conn.setReadTimeout(10 * 1000); // milliseconds
                    conn.setConnectTimeout(10 * 1000); // milliseconds
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.connect();

                    // Create Data POJO and convert to JSON string
                    Data data = new Data();
                    Gson gson = new Gson();
                    String dataGsonStr = gson.toJson(data);

                    // Send JSON string to server
                    DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                    dos.write(dataGsonStr.getBytes());
                    int responseCode = conn.getResponseCode();
                    Log.d(TAG, "Response code: " + responseCode);

                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                    responseStr = content.toString();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    responseStr = "ERROR MalformedURLException caught: " + e;
                } catch (IOException e) {
                    e.printStackTrace();
                    responseStr = "ERROR IOException caught: " + e;
                }
            } else if (isSend == false) { // Execute the following if "Receive" button is pressed
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);

                HttpResponse httpResponse = httpClient.execute(httpGet);
                int responseCode = httpResponse.getStatusLine().getStatusCode();
                Log.d(TAG, "Response code: " + responseCode);
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
                }
            }
            return responseStr;
        }
    }

    // POJO for send
    public class Data {
        private int dataInt = 700;
        private String dataStr = "EC700 Alpha2";

        public int getDataInt() {
            return dataInt;
        }

        public String getDataStr() {
            return dataStr;
        }

        public void setDataInt(int dataInt) {
            this.dataInt = dataInt;
        }

        public void setDataStr(String dataStr) {
            this.dataStr = dataStr;
        }
    }

    // POJO for receive
    public class JsonIP {
        private String ip;
        private String about;
        private URL url;

        public String getIp() {
            return ip;
        }

        public String getAbout() {
            return about;
        }

        public URL getPro() {
            return url;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public void setAbout(String about) {
            this.about = about;
        }

        public void setPro(URL url) {
            this.url = url;
        }

        @Override
        public String toString() {
            return "JsonIp [ip=" + ip + ", about=" + about
                    + ", Pro!=" + url + "]";
        }
    }
}