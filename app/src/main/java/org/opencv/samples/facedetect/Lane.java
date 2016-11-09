package org.opencv.samples.facedetect;

import org.opencv.core.Point;


public class Lane {
    public Point p0;
    public Point p1;
    public int votes;
    public boolean visited;
    public boolean found;
    public double angle;
    public double k=0.0;
    public double b=0.0;

    public Lane() {}

    public Lane(Point a, Point b, double angle, double kl, double bl) {
        p0 = a;
        p1 = b;
        this.angle = angle;
        votes = 0;
        visited = false;
        found = false;
        k = kl;
        this.b = bl;
    }
}



