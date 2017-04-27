import libs.StdDraw;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

public class GameObject {
    private int id;

    private Vector r; // position
    private Vector v; // velocity
    private double mass; // mass

    private double radius;
    private Color color;

    //Physics
    private Shape shape;
    private double restCoeff; // Coefficient of restitution

    static final double SIZE = 3E-14;

    private Collection<Integer> colliders;

    public GameObject(int id, Vector r, Vector v, double mass, double radius) {
        this.id = id;
        this.r = r;
        this.v = v;
        this.mass = mass;
        this.radius = Math.sqrt(mass / Math.PI) * Math.sqrt(radius);

        Random random = new Random();
        this.color = new Color(random.nextFloat(),
                random.nextFloat(),
                random.nextFloat());

        this.colliders = new HashSet<>();
    }

    public void move(Vector f, double dt) {
        Vector a = f.times(1/mass);
        v = v.plus(a.times(dt));
        r = r.plus(v.times(dt));
    }

    public Vector forceFrom(GameObject that) {
        Vector delta = that.r.minus(this.r);
        double dist = delta.magnitude();

        double f = (StellarCrush.G * this.mass * that.mass) / ((dist + StellarCrush.softE) * (dist+ StellarCrush.softE));
        return delta.direction().times(f);
    }

    public void draw() {
        StdDraw.setPenColor(color);
        StdDraw.filledCircle(r.cartesian(0), r.cartesian(1), SIZE * radius* StellarCrush.scale);
    }

    static Vector collisionResult(double cr, //coefficient of restitution
                                  double ma, double mb, //weight
                                  Vector ua, Vector ub) // Velocity
    {
        double f = (ma - mb) /(ma + mb);
        Vector result = ub.minus(ua).times(mb * 0.95).plus(ua.times(ma).plus(ub.times(mb)));
        return ua.times(f);
    }

    /*
    //Collisions elastic
    public void applyCollide(GameObject object)
    {
        double m1 = this.getMass();
        double m2 = object.getMass();
        double m1m2 = m1+m2;

        this.v = this.v.times(
                (m1 - m2) / m1m2
        ).plus(object.v.times(
                2.0 * m2  / m1m2
        ));

        object.v = this.v.times(
                2.0 * m1  / m1m2
        ).plus(object.v.times(
                (m2 - m1) / m1m2
        ));
   }
   */

    //Nonelastic collisions
    public void applyCollide(GameObject object)
    {
        Vector ua = this.v;
        Vector ub = object.v;
        double ma = this.getMass();
        double mb = this.getMass();

        double mamb = ma + mb;

        double Cr = 0.3;

        this.v = ub.minus(ua).times(Cr * mb).plus(ua.times(ma)).plus(ub.times(mb)).times(1.0/mamb);

        object.v = ua.minus(ub).times(Cr * ma).plus(ua.times(ma)).plus(ub.times(mb)).times(1.0/mamb);

    }

    public boolean collideWith(GameObject object)
    {
        Vector position = object.getPosition();
        //Fast check distance
        if(VectorUtil.distanceMinusTo(position, r, (object.getRadius() + radius) * SIZE * StellarCrush.scale))
        {
           /* //Still collide return false for velocity increase;
            if(colliders.contains(object.getId()))
                return false;
            else
            {
                //Never collided so compute velocity
                colliders.add(object.getId());
                return true;
            }*/
            return true;
        }
        //No longer colliding so remove and stop all
        //colliders.remove(object.getId());

        return false;
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

    public void setMass(double mass) {
        this.mass = mass;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public double getRestCoeff() {
        return restCoeff;
    }

    public void setRestCoeff(double restCoeff) {
        this.restCoeff = restCoeff;
    }
}
