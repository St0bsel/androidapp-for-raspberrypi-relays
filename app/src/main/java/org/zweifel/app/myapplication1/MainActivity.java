package org.zweifel.app.myapplication1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.R.drawable.presence_offline;

public class MainActivity extends AppCompatActivity {


    //function which returns text from url
    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                 while(data != -1){
                     char current = (char) data;
                     result += current;
                     data = reader.read();
                 }
                return result;
            }
            catch (Exception e){
                e.printStackTrace();
                return "Faild";
            }
        }
    }

    //function to get relaydata and show it in listview
    public String getinfo(){

        DownloadTask task = new DownloadTask();
        String result = null;

        try {
            String serverhost = functionSharedPreferences("get", "serverhost", "0");
            result = task.execute(serverhost + "php/getarray.php").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        //process json data recived from url
        try {
            JSONArray recivedData = new JSONArray(result);
            JSONArray recivedData1 = recivedData.getJSONArray(0);

            final ListView list = (ListView) findViewById(R.id.list1);
            ArrayList<String> items = new ArrayList<String>();

            for(int i=0; i < recivedData1.length() ; i++) {
                JSONArray json_data = recivedData1.getJSONArray(i);
                String name=json_data.getString(0);
                int status = json_data.getInt(2);
                String statustxt = "";
                if (status == 1){
                    statustxt = "on";
                } else {
                    statustxt = "off";
                }
                items.add(name + " is " + statustxt);
                Log.i("INFO", name + " is " + statustxt);
            }

            //insert into list view
            ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
            list.setAdapter(mArrayAdapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    //Parameters Array for post request
                    String[][] postParameters = {
                            {"change", String.valueOf(i)},
                            /*{"name", ""},
                            {"gpiopin", ""},
                            {"relay", ""},*/
                            {"method", "0"}
                    };

                    //generate String out of array
                    String postParam = "";
                    for (int j = 0; j < postParameters.length; j++){
                        postParam += postParameters[j][0] + "=" + postParameters[j][1] + "&";
                    }

                    try {
                        //send post request to change relay status and rebuild listview
                        getrequest request = new getrequest();
                        String result = null;
                        result = request.execute(postParam).get();
                        clearList();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    //function which sends a post request to a url
    public class getrequest extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... postParameters) {
            try {
                String serverhost = functionSharedPreferences("get", "serverhost", "0");
                String result ="";
                URL url = new URL(serverhost + "php/change.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                //String postParameter = "change=0&method=0";
                String postParameter = postParameters[0];
                connection.setFixedLengthStreamingMode(postParameter.getBytes().length);
                PrintWriter out = new PrintWriter(connection.getOutputStream());
                out.print(postParameter);
                out.close();
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while(data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "false";
        }
    }

    public String functionSharedPreferences(String method, String name, String value){
        SharedPreferences sharedPreferences = this.getSharedPreferences("org.zweifel.app.myapplication1", Context.MODE_PRIVATE);
        if (method == "get"){
            String result = sharedPreferences.getString(name,"");
            return result;
        }
        else if (method == "set"){
            sharedPreferences.edit().putString(name, value).apply();
            return "true";
        }
        else {
            return "false";
        }

    }

    public void clearList(){
        ListView list = (ListView) findViewById(R.id.list1);
        list.setAdapter(null);
        getinfo();
    }

    public void openClientSettings(View view){
        Intent intent = new Intent(this, ClientSettingsActivity.class);
        startActivity(intent);
    }

    public void openServerSettings(View view){
        Intent intent = new Intent(this, ServerSettingsActivity.class);
        startActivity(intent);
    }

    public void refreshList(View view){
        clearList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //if the connection to the server fails
        if (getinfo() == "Faild"){
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Connection error")
                    .setMessage("The app has no connection to the server, please go to the Client Settings and change the server")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.i("alert", "alert accepted");
                        }
                    })
                    .show();
        }
        else {

        }

    }
}
