package com.example.user.spacecraftoperationsapplication;

import java.time.LocalTime;

public class Odometry {
    //Attributes
    LocalTime time;
    Double y;

    public Odometry(LocalTime time, Double y)
    {
        this.time = time;
        this.y = y;
    }

    //Methods
    public LocalTime getTime() {
        return time;
    }
    public void setTime(LocalTime time) {
        this.time = time;
    }

    public Double getY() {
        return y;
    }
    public void setY(Double y) {
        this.y = y;
    }
}
