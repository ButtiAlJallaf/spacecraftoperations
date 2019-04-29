package com.example.user.spacecraftoperationsapplication;

public class Odometry {
    //Attributes
    int x;
    Double y;

    public Odometry(int x, Double y)
    {
        this.x = x;
        this.y = y;
    }

    //Methods
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }
    public void setY(Double y) {
        this.y = y;
    }
}
