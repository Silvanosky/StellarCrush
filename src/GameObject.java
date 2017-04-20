import libs.StdDraw;

import java.awt.*;
import java.util.Random;

public class GameObject {
    private int id;

    protected Vector r; // position
    protected Vector v; // velocity
    private double mass; // mass

    private double radius;
    private Color color;

    public GameObject(int id, Vector r, Vector v, double mass, double radius) {
        this.id = id;
        this.r = r;
        this.v = v;
        this.mass = mass;
        this.radius = radius;
        Random random = new Random();
        this.color = new Color(random.nextFloat(),
                random.nextFloat(),
                random.nextFloat());
    }

    public void move(Vector f, double dt) {
        Vector a = f.times(1/mass);
        v = v.plus(a.times(dt));
        r = r.plus(v.times(dt));
    }

    public Vector forceFrom(GameObject that) {
        Vector delta = that.r.minus(this.r);
        double dist = delta.magnitude();

        if(dist < StellarCrush.softE)
            dist = StellarCrush.softE;

        double f = (StellarCrush.G * this.mass * that.mass) / (dist * dist);
        return delta.direction().times(f);
    }

    public void draw() {
        StdDraw.setPenRadius(0.05 * radius);
        StdDraw.setPenColor(color);
        //libs.StdDraw.setPenRadius(1.0);
        libs.StdDraw.point(r.cartesian(0), r.cartesian(1));
    }

    public int getId() {
        return id;
    }

    public Vector getPosition()
    {
        return r;
    }

    public void setPosition(Vector r)
    {
        this.r = r;
    }

    public Vector getVelocity()
    {
        return v;
    }

    public void setVelocity(Vector v)
    {
        this.v = v;
    }

    public double getMass() {
        return mass;
    }

    public double getRadius() {
        return radius;
    }

    public Color getColor() {
        return color;
    }
}
