package org.opencv.samples.facedetect;


public class ExpMovingAverage {
    public double alpha; // [0;1] less = more stable, more = less stable
    public double oldValue;
    public boolean unset;
    
    public ExpMovingAverage() {
        this.alpha = 0.2;
        unset = true;
    }

    public void clear() {
        unset = true;
    }

    public void add(double value) {
        if (unset == true) {
            oldValue = value;
            unset = false;
        }
        double newValue = oldValue + alpha * (value - oldValue);
        oldValue = newValue;
    }

    public double get() {
        return oldValue;
    }
}



