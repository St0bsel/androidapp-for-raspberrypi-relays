package org.zweifel.app.myapplication1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static org.zweifel.app.myapplication1.MainActivity.*;

public class ServerSettingsActivity extends AppCompatActivity {

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

    //function which sends a post request to a url
    public class getrequest extends AsyncTask<String, Void, String> {

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


    public void serverSettingsPresed(View view){
        final GridLayout gridSettings = (GridLayout) findViewById(R.id.gridSettings);
        gridSettings.setVisibility(View.VISIBLE);

        final EditText idInput = (EditText) findViewById(R.id.idInput);
        final EditText nameInput = (EditText) findViewById(R.id.nameInput);
        final EditText gpioInput = (EditText) findViewById(R.id.gpiopinInput);
        final EditText relayInput = (EditText) findViewById(R.id.relayInput);

        final TextView idText = (TextView) findViewById(R.id.idText);
        final TextView nameText = (TextView) findViewById(R.id.nameText);
        final TextView gpioText = (TextView) findViewById(R.id.gpiopinText);
        final TextView relayText = (TextView) findViewById(R.id.relayText);

        String method = "";
        //method 5 = change status from switch
        //method 4 = remove switch
        //method 3 = change name, gpiopin and relay from switches
        //method 2 = remove relay
        //method 1 = change name and gpiopin
        //method 0 = change gpiopin status


        switch (view.getId()) {
            case R.id.removeRelay:
                nameInput.setVisibility(View.GONE);
                nameText.setVisibility(View.GONE);
                gpioInput.setVisibility(View.GONE);
                gpioText.setVisibility(View.GONE);
                relayInput.setVisibility(View.GONE);
                relayText.setVisibility(View.GONE);
                method="2";
                break;
            case R.id.relaySettings:
                relayInput.setVisibility(View.GONE);
                relayText.setVisibility(View.GONE);
                method="1";
                break;
            case R.id.removeSwitch:
                nameInput.setVisibility(View.GONE);
                nameText.setVisibility(View.GONE);
                gpioInput.setVisibility(View.GONE);
                gpioText.setVisibility(View.GONE);
                relayInput.setVisibility(View.GONE);
                relayText.setVisibility(View.GONE);
                method="4";
                break;
            case R.id.switchSettings:
                method="3";
                break;
        }

        final String methodpost = method;

        Button submitSettings = (Button) findViewById(R.id.submitSettings);

        submitSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                relayInput.setVisibility(View.VISIBLE);
                relayText.setVisibility(View.VISIBLE);
                gridSettings.setVisibility(View.GONE);


                //Parameters Array for post request
                String[][] postParameters = {
                        {"change", idInput.getText().toString()},
                        {"name", nameInput.getText().toString()},
                        {"gpiopin", gpioInput.getText().toString()},
                        {"relay", relayInput.getText().toString()},
                        {"method", methodpost}
                };

                //generate String out of array
                String postParam = "";
                for (int j = 0; j < postParameters.length; j++){
                    postParam += postParameters[j][0] + "=" + postParameters[j][1] + "&";
                }

                try {
                    //send post request to change relay status and rebuild listview
                    getrequest request = new getrequest();
                    String result = "";
                    result = request.execute(postParam).get();
                    Log.i("result post", result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_settings);
    }
}
