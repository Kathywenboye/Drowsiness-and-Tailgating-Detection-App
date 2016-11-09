package org.opencv.samples.facedetect;


public class Status {
    public ExpMovingAverage b;
    public ExpMovingAverage k;
    public int lost;
    public boolean reset;
 

    public Status() {
        b = new ExpMovingAverage();
        k = new ExpMovingAverage();
        reset = true;
        lost = 0;
    }
}



