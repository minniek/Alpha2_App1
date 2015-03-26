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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MainActivity extends ActionBarActivity {
    // Declare widgets
    Button sendBtn, receiveBtn, clearBtn;
    TextView sendTv, receiveTv;
    Toolbar toolbar;

    boolean isSend; // flag to determine Send or Receive execution

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Send - create POJO, serialize it to JSON Object, and send POST request
        sendBtn = (Button)findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isSend = true;
                String urlSend = "http://www.httpbin.org/post"; // A test server that accepts post requests

                // Check for network connection
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo nwInfo = connMgr.getActiveNetworkInfo();
                // If network is present, start AsyncTask to connect to given URL
                if ((nwInfo != null) && (nwInfo.isConnected())) {
                    new StartAsyncTask().execute(urlSend);
                } else {
                    sendTv.setText("ERROR No network connection detected.");
                }
        }});

        // Receive - get JSON string from server, deserialize it to POJO
        receiveBtn = (Button)findViewById(R.id.receive_btn);
        receiveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isSend = false;
                // Receive JSON string from:
                String urlReceive = "http://jsonip.com/"; // Returns public IP of device

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
        });

        clearBtn = (Button)findViewById(R.id.clear_btn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendTv.setText("");
                receiveTv.setText("");
            }
        });

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

    protected class StartAsyncTask extends AsyncTask<String, Void, String> {
        String TAG = "Alpha2_App1"; // For Log.d
        String responseStr = "";

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
            if (isSend) {
                sendTv.setText("Successfully sent JSON string to \"http://www.httpbin.org/post\"" + "\n\n" + "The following" +
                        " is the response from the server: " + responseStr);
            } else if (!isSend) {
                receiveTv.setText("Successfully received the following JSON string from \"http://jsonip.com/\"" + "\n\n"
                        + responseStr);
            }
        }

        private String connectToURL(String url) throws IOException {
            URL myURL = new URL(url);
            HttpURLConnection conn =  (HttpURLConnection) myURL.openConnection();
            conn.setReadTimeout(10 * 1000); // milliseconds
            conn.setConnectTimeout(10 * 1000); // milliseconds
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();

            // If Send button is pressed, create data object, convert it to JSON string, and send it
            if (isSend) {
                Data dataObj = new Data();
                Gson gson = new Gson();
                String dataJsonStr = gson.toJson(dataObj); // Serialize data object to string
                Log.d(TAG, "dataJsonStr: " + dataJsonStr);

                // Send JSON string to server
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(dataJsonStr.getBytes());
            }

            // Retrieve response from server
            InputStream is = conn.getInputStream();
            Log.d(TAG, is.toString());
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            responseStr = content.toString();

            // If Receive is pressed, deserialize "responseStr" into JsonIP object
            if (!isSend) {
                Gson gson = new Gson();
                JsonIP jsonIp = gson.fromJson(responseStr, JsonIP.class); // Deserialize
                Log.d(TAG, jsonIp.toString()); // Temp sink
            }

            return responseStr;
        }
    }

    /**************************************** POJOs ****************************************/
    // Object to send
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

    // Object to receive
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