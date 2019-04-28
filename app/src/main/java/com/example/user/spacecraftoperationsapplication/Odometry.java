package com.example.user.spacecraftoperationsapplication;

public class Odometry {
    //Attributes
    Double x;
    Double y;

    public Odometry() {}
    public Odometry(Double x, Double y)
    {
        this.x = x;
        this.y = y;
    }

    //Methods
    public Double getX() {
        return x;
    }
    public void setX(Double x) {
        this.x = x;
    }
    public Double getY() {
        return y;
    }
    public void setY(Double y) {
        this.y = y;
    }
}
