package com.example.user.spacecraftoperationsapplication;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class Rover extends AppCompatActivity {

    private TextView tvOutputMsg;
    private TextView tvOutputBatteryI;
    private TextView tvOutputBatteryV;
    private TextView tvOutputMotorLF;
    private TextView tvOutputMotorLR;
    private TextView tvOutputMotorRF;
    private TextView tvOutputMotorRR;
    private WebSocket ws;

    private void displayToast(final String msg)
    {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //ID is the key of the JSON object. Value is the value of the JSON object. Min and max are the warning range. Textview is used for setting colors.
    private void checkValue(TextView myTV, String id, double value, double min, double max)
    {
        //If the value is more than or less than the range, then notify the user and set a color.
        if (value <= min || value >= max)
        {
            displayToast("Warning: " + id + " has an irregular value.");
            myTV.setTextColor(Color.RED);
        }
        else
        {
            myTV.setTextColor(Color.GRAY);
        }
    }

    private final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            //webSocket.send("s ack.msg");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            try {
                JSONObject jObject = new JSONObject(text);
                String id = jObject.getString("id");
                switch (id)
                {
                    case "ack.msg":
                        tvOutputMsg.setText(jObject.getString("value"));
                        break;
                    case "power_telemetry.battery_I":
                        tvOutputBatteryI.setText(jObject.getString("value"));
                        checkValue(tvOutputBatteryI, id, jObject.getDouble("value"), 0.25, 2.75);
                        break;
                    case "power_telemetry.battery_V":
                        tvOutputBatteryV.setText(jObject.getString("value"));
                        checkValue(tvOutputBatteryV, id, jObject.getDouble("value"), 4.75, 5.25);
                        break;
                    case "power_telemetry.motor_rf_I":
                        tvOutputMotorRF.setText(jObject.getString("value"));
                        checkValue(tvOutputMotorRF, id, jObject.getDouble("value"), 0.25, 2.75);
                        break;
                    case "power_telemetry.motor_rr_I":
                        tvOutputMotorRR.setText(jObject.getString("value"));
                        checkValue(tvOutputMotorRR, id, jObject.getDouble("value"), 0.25, 2.75);
                        break;
                    case "power_telemetry.motor_lf_I":
                        tvOutputMotorLF.setText(jObject.getString("value"));
                        checkValue(tvOutputMotorLF, id, jObject.getDouble("value"), 0.25, 2.75);
                        break;
                    case "power_telemetry.motor_lr_I":
                        tvOutputMotorLR.setText(jObject.getString("value"));
                        checkValue(tvOutputMotorLR, id, jObject.getDouble("value"), 0.25, 2.75);
                        break;
                    default: System.out.println("Error: Unknown message received.");
                }
                /*if (jObject.getDouble("value") <= 0.25 || jObject.getDouble("value") >= 2.75)
                {
                    displayToast("Warning: " + jObject.getString("id") + " has an irregular value.");
                }*/
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            System.out.println("Closing : " + code + " / " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            System.out.println(t.getMessage());
        }
    }

    private void initBottomNav()
    {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.action_rover);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.action_home:
                        Intent home = new Intent(getApplicationContext(),HomeActivity.class);

                        startActivity(home);
                        break;
                    case R.id.action_rover:
                        Intent rover = new Intent(getApplicationContext(),Rover.class);

                        startActivity(rover);
                        break;
                    case R.id.action_settings:
                        Intent settings = new Intent(getApplicationContext(),Settings.class);

                        startActivity(settings);
                        break;
                }
                return true;
            }
        });
    }

    //This function takes the resource id and subscription key as parameters. It adds a listener to the toggle buttons.
    private void initToggleButtons(int myToggleButton, final String key)
    {
        ToggleButton toggle = findViewById(myToggleButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ws.send("s " + key); //Subscribe to the selected key.
                } else {
                    ws.send("u " + key); //Unsubscribe from the selected key.
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rover);
        initBottomNav(); //Code by Butti to initialize the bottom navigation.

        //Establishing a websocket connection
        Request request = new Request.Builder().url("ws://192.168.1.140:3739/").build(); //The IP of the server.
        EchoWebSocketListener listener = new EchoWebSocketListener();
        OkHttpClient client = new OkHttpClient();
        ws = client.newWebSocket(request, listener);

        //Reads all textview outputs
        tvOutputMsg = findViewById(R.id.tvOutputMsg);
        tvOutputBatteryI = findViewById(R.id.tvOutputBatteryI);
        tvOutputBatteryV = findViewById(R.id.tvOutputBatteryV);
        tvOutputMotorLF = findViewById(R.id.tvOutputMotorLF);
        tvOutputMotorLR = findViewById(R.id.tvOutputMotorLR);
        tvOutputMotorRF = findViewById(R.id.tvOutputMotorRF);
        tvOutputMotorRR = findViewById(R.id.tvOutputMotorRR);

        //Initliazes the toggle buttons by adding a listener, and setting corresponding key.
        initToggleButtons(R.id.btnMsg, "ack.msg");
        initToggleButtons(R.id.btnBatteryI, "power_telemetry.battery_I");
        initToggleButtons(R.id.btnBatteryV, "power_telemetry.battery_V");
        initToggleButtons(R.id.btnMotorLF, "power_telemetry.motor_lf_I");
        initToggleButtons(R.id.btnMotorLR, "power_telemetry.motor_lr_I");
        initToggleButtons(R.id.btnMotorRF, "power_telemetry.motor_rf_I");
        initToggleButtons(R.id.btnMotorRR, "power_telemetry.motor_rr_I");
    }
}