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
    private int counter = 0;
    private String myUrl;

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
    private void displayValue(final TextView textview, final String label, final String value)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //If the textview is invisible, then it will be visible.
                if (textview.getVisibility() == View.GONE)
                {
                    textview.setVisibility(View.VISIBLE);
                }
                //Display the values to the user.
                String formattedValue = label + ": \n" + value;
                textview.setText(formattedValue);
            }
        });
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
                String value = jObject.getString("value"); //String is used for displaying.
                switch (id)
                {
                    case "ack.msg":
                        displayValue(tvOutputMsg, "Acknowledgement", value);
                        break;
                    case "power_telemetry.battery_I":
                        displayValue(tvOutputBatteryI,"Battery I", value);
                        checkValue(tvOutputBatteryI, id, Double.parseDouble(value), 0.25, 2.75);
                        break;
                    case "power_telemetry.battery_V":
                        displayValue(tvOutputBatteryV,"Battery V", value);
                        checkValue(tvOutputBatteryV, id, Double.parseDouble(value), 4.75, 5.25);
                        break;
                    case "power_telemetry.motor_rf_I":
                        displayValue(tvOutputMotorRF, "Motor RF", value);
                        checkValue(tvOutputMotorRF, id, Double.parseDouble(value), 0.25, 2.75);
                        break;
                    case "power_telemetry.motor_rr_I":
                        displayValue(tvOutputMotorRR, "Motor RR", value);
                        checkValue(tvOutputMotorRR, id, Double.parseDouble(value), 0.25, 2.75);
                        break;
                    case "power_telemetry.motor_lf_I":
                        displayValue(tvOutputMotorLF, "Motor LF", value);
                        checkValue(tvOutputMotorLF, id, Double.parseDouble(value), 0.25, 2.75);
                        break;
                    case "power_telemetry.motor_lr_I":
                        displayValue(tvOutputMotorLR, "Motor LR", value);
                        checkValue(tvOutputMotorLR, id, Double.parseDouble(value), 0.25, 2.75);
                        break;
                    case "odometry.imu_acc_y": //Unlike the above, this value will be displayed in a graph.
                        double imu_acc_y = jObject.getDouble("value");  //Used as y.
                        counter++; //Used as x.
                        dpList.add(new Odometry(counter, imu_acc_y)); //This list stores object that hold x and y.
                        updateGraph(R.id.graph);
                        break;
                    default: System.out.println("Error: Unknown message received.");
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
            tvOutputMsg.setText("Could not connect to " + myUrl);
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
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        for (int i = 0; i < dpList.size(); i++)
        {
            int x = dpList.get(i).getX();
            double y = dpList.get(i).getY();
            series.appendData(new DataPoint(x,y),true, 1000);
            if (x >= 160) //Clears after adding 160 or more datapoints. This is done to avoid app crash.
            {
                dpList.clear();
                graph.removeAllSeries();
                series.resetData(new DataPoint[] {new DataPoint(0,y)});
                counter = 0;
            }
        }
        graph.addSeries(series);
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
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
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

        //Get the user's preference from settings, such as warning.
        settingsPref = getSharedPreferences("settings", Context.MODE_PRIVATE);

        //Get the IP and port saved in sp, in order to open a websocket.
        String ip = settingsPref.getString("ip", "192.168.0.2");
        String port = settingsPref.getString("port", "3739");
        myUrl = "ws://" + ip + ":" + port + "/";

        //Establishing a websocket connection
        Request request = new Request.Builder().url(myUrl).build(); //The IP of the server.
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
        dpList = new ArrayList<>(); //Init array list. This list will store Odometry objects, which will store x and y.
        setGraphProperties(R.id.graph); //Sets the x and y bounds of this graph.
        ws.send("s odometry.imu_acc_y"); //The app subscribes to this key by default.
    }
}