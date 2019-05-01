package com.example.user.spacecraftoperationsapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.text.SimpleDateFormat;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class Rover extends AppCompatActivity {
    //Global variables
    private TextView tvOutputMsg;
    private TextView tvOutputBatteryI;
    private TextView tvOutputBatteryV;
    private TextView tvOutputMotorLF;
    private TextView tvOutputMotorLR;
    private TextView tvOutputMotorRF;
    private TextView tvOutputMotorRR;
    private WebSocket ws;
    private SharedPreferences subscriptions;
    private SharedPreferences settingsPref;
    private ArrayList<Odometry> dpList;
    private Double imu_acc_y;
    private int counter = 0;

    private void displayToast(final String msg)
    {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setTime(int resourceId)
    {
        final TextView tv = findViewById(resourceId);
        final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Date currentTime = Calendar.getInstance().getTime();
                        String formattedDate = dateFormat.format(currentTime);
                        tv.setText("GMT: " + formattedDate);
                    }
                });
            }
        }, 0, 1000);//1000 is a Refreshing Time (1second)
    }

    //ID is the key of the JSON object. Value is the value of the JSON object. Min and max are the warning range. Textview is used for setting colors.
    private void checkValue(TextView myTV, String id, double value, double min, double max)
    {
        //If the value is more than or less than the range, then notify the user and set a color.
        if (value <= min || value >= max)
        {
            //If the user enable warnings, then toast messages will be displayed to him.
            if (settingsPref.getBoolean("warning", true))
            {
                displayToast("Warning: " + id + " has an irregular value.");
            }
            myTV.setTextColor(Color.RED);
        }
        else
        {
            //If nightmode is on, then the text will be grey, else it will be white.
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            {
                myTV.setTextColor(Color.WHITE);
            }
            else
            {
                myTV.setTextColor(Color.GRAY);
            }
        }
    }

    //Takes the resouce id of a textview. Label and value of the json object.
    private void displayValue(TextView textview, String label, String value)
    {
        String formattedValue = label + ": " + value;
        textview.setText(formattedValue);
    }

    private final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            System.out.println("A websocket connection has been opened.");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            try {
                JSONObject jObject = new JSONObject(text);
                String id = jObject.getString("id");
                String stringValue = jObject.getString("value"); //String is used for displaying.
                double doubleValue = jObject.getDouble("value"); //Double is used to check data value.
                switch (id)
                {
                    case "ack.msg":
                        displayValue(tvOutputMsg, "Acknowledgement", stringValue);
                        break;
                    case "power_telemetry.battery_I":
                        displayValue(tvOutputBatteryI,"Battery I", stringValue);
                        checkValue(tvOutputBatteryI, id, doubleValue, 0.25, 2.75);
                        break;
                    case "power_telemetry.battery_V":
                        displayValue(tvOutputBatteryV,"Battery V", stringValue);
                        checkValue(tvOutputBatteryV, id, doubleValue, 4.75, 5.25);
                        break;
                    case "power_telemetry.motor_rf_I":
                        displayValue(tvOutputMotorRF, "Motor RF", stringValue);
                        checkValue(tvOutputMotorRF, id, doubleValue, 0.25, 2.75);
                        break;
                    case "power_telemetry.motor_rr_I":
                        displayValue(tvOutputMotorRR, "Motor RR", stringValue);
                        checkValue(tvOutputMotorRR, id, doubleValue, 0.25, 2.75);
                        break;
                    case "power_telemetry.motor_lf_I":
                        displayValue(tvOutputMotorLF, "Motor LF", stringValue);
                        checkValue(tvOutputMotorLF, id, doubleValue, 0.25, 2.75);
                        break;
                    case "power_telemetry.motor_lr_I":
                        displayValue(tvOutputMotorLR, "Motor LR", stringValue);
                        checkValue(tvOutputMotorLR, id, doubleValue, 0.25, 2.75);
                        break;
                    case "odometry.imu_acc_y":
                        imu_acc_y = jObject.getDouble("value"); //Unlike the above, this value will be displayed in a graph.
                        break;
                    default: System.out.println("Error: Unknown message received.");
                }

                if (imu_acc_y != null)
                {
                    System.out.println("imu_acc_y = " + imu_acc_y);
                    counter++;
                    dpList.add(new Odometry(counter, imu_acc_y));
                    imu_acc_y = null;
                    if (dpList.size() > 0) {
                        updateGraph(R.id.graph);
                    }
                }
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
                    case R.id.action_settings:
                        Intent settings = new Intent(getApplicationContext(),Settings.class);
                        startActivity(settings);
                        break;
                }
                return true;
            }
        });
    }

    private void checkSubscriptions(int myToggleButton)
    {
        if (subscriptions.getBoolean(Integer.toString(myToggleButton), false))
        {
            ws.send("s " + getResources().getResourceEntryName(myToggleButton));
        }
    }

    private void updateGraph(int myGraph)
    {
        GraphView graph = findViewById(myGraph);
        System.out.println("dpList size = " + dpList.size());
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        for (int i = 0; i < dpList.size(); i++)
        {
            int x = dpList.get(i).getX(); System.out.println("dpList.get(i).getX() = " + dpList.get(i).getX());
            double y = dpList.get(i).getY(); System.out.println("dpList.get(i).getY() = " + dpList.get(i).getY());
            series.appendData(new DataPoint(x,y),true, 1000); System.out.println("Appended data: x = " + x + " and y = " + y);
            if (x >= 160)
            {
                dpList.clear(); System.out.println("dpList is cleared. Size is now " + dpList.size());
                graph.removeAllSeries();
                series.resetData(new DataPoint[] {new DataPoint(0,y)});
                counter = 0;
                System.out.println("The graph as been cleared.");
            }
        }
        graph.addSeries(series);
        //series.resetData(new DataPoint[]);
        System.out.println("The graph has been updated.");
    }

    private void setGraphProperties(int myGraph)
    {
        //Code related to graph.
        GraphView graph = findViewById(myGraph);

        //set manual x bounds
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMaxY(100);
        graph.getViewport().setMinY(-100);

        //set manual y bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMaxX(160);
        graph.getViewport().setMinX(0);

        //If nightmode is on, then the graph will be colored white in order to be visible.
        if (AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES)
        {
            graph.setBackgroundColor(Color.WHITE);
        }
    }

    private void nightModeActivate()
    {
        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES)
        {
            setTheme(R.style.darkTheme);
        }
        else setTheme(R.style.AppTheme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        nightModeActivate(); //Function by Abdullah Aljallaf to init night mode.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rover);
        initBottomNav(); //Code by Butti to initialize the bottom navigation.

        //Establishing a websocket connection
        Request request = new Request.Builder().url("ws://192.168.1.148:3739/").build(); //The IP of the server.
        EchoWebSocketListener listener = new EchoWebSocketListener();
        OkHttpClient client = new OkHttpClient();
        ws = client.newWebSocket(request, listener);

        //Gets all textviews
        tvOutputMsg = findViewById(R.id.tvOutputMsg);
        tvOutputBatteryI = findViewById(R.id.tvOutputBatteryI);
        tvOutputBatteryV = findViewById(R.id.tvOutputBatteryV);
        tvOutputMotorLF = findViewById(R.id.tvOutputMotorLF);
        tvOutputMotorLR = findViewById(R.id.tvOutputMotorLR);
        tvOutputMotorRF = findViewById(R.id.tvOutputMotorRF);
        tvOutputMotorRR = findViewById(R.id.tvOutputMotorRR);

        //Check the keys the user is subscribed to.
        subscriptions = getSharedPreferences("subscriptions", Context.MODE_PRIVATE);
        checkSubscriptions(R.id.ack_msg);
        checkSubscriptions(R.id.power_telemetry_battery_I);
        checkSubscriptions(R.id.power_telemetry_battery_V);
        checkSubscriptions(R.id.power_telemetry_motor_lf_I);
        checkSubscriptions(R.id.power_telemetry_motor_lr_I);
        checkSubscriptions(R.id.power_telemetry_motor_rf_I);
        checkSubscriptions(R.id.power_telemetry_motor_rr_I);

        //Display the time at the top left corner of the screen.
        setTime(R.id.tvTime);

        //Graph related code.
        dpList = new ArrayList<>();
        setGraphProperties(R.id.graph);
        ws.send("s odometry.imu_acc_y"); //The app subscribes to this key by default.

        //Get the user's preference from settings, such as warning.
        settingsPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
    }
}