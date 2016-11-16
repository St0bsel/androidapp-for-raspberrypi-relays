package org.zweifel.app.myapplication1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ClientSettingsActivity extends AppCompatActivity {

    public void clientSettingsBack(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void clientSettingsSave(View view){
        EditText serverhostField = (EditText) findViewById(R.id.clientSettingsServerhost);
        String result = functionSharedPreferences("set", "serverhost", serverhostField.getText().toString());
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_settings);
        TextView serverhostText = (TextView) findViewById(R.id.clientSettingsText3);
        String serverhost = functionSharedPreferences("get","serverhost","");
        serverhostText.setText(serverhost);
    }
}
