import libs.Draw;

import java.awt.*;
import java.util.Random;


public class GameObject {
    private final int id;

    private Vector loc; // position
    private Vector velocity; // velocity
    private double mass; // mass

    private double radius;
    private Color color;

    //Point value of this object
    private int points;

    static final double SIZE = 3E-14;

    public GameObject(int id, Vector loc, Vector velocity, double mass, double radius) {
        this.id = id;
        this.loc = loc;
        this.velocity = velocity;
        this.mass = mass;
        this.radius = Math.sqrt((mass * radius) / Math.PI);

        updatePoint();

        Random random = GameObjectLibrary.getRandom();
        this.color = new Color(random.nextFloat(),
                random.nextFloat(),
                random.nextFloat());
    }

    /**
     * Method used to move an object.
     * @param f The vector force of the movement
     * @param dt The duration of this movement
     */
    public void move(Vector f, double dt) {
        Vector a = f.times(1/mass);
        this.velocity = this.velocity.plus(a.times(dt));
        this.loc = this.loc.plus(this.velocity.times(dt));
    }

    /**
     * Method wich compute the force between the current object and the specified object
     * @param that The object to compare to
     * @return The Vector force
     */
    public Vector forceFrom(GameObject that) {
        Vector delta = that.loc.minus(this.loc);
        double dist = delta.dot(delta) + StellarCrush.softE * StellarCrush.softE;

        double f = (StellarCrush.G * this.mass * that.mass) / dist;
        return VectorUtil.fastDir(velocity).times(f);
        //return delta.direction().times(f);
    }

    /**
     * Method to draw the object on a surface.
     * @param dr The surface to draw on
     */
    public void draw(Draw dr) {
        dr.setPenColor(this.color);
        double v = SIZE * this.radius * StellarCrush.scale;
        dr.filledCircle(this.loc.cartesian(0), this.loc.cartesian(1), v);
    }

    /**
     * Method wich compute and apply the collision force to the objects
     * @param object The object to collide with
     */
    public void applyCollide(GameObject object)
    {
        Vector ua = this.velocity;
        Vector ub = object.velocity;
        double ma = this.getMass();
        double mb = this.getMass();

        double mamb = ma + mb;

        double cr = 0.3;

        this.velocity = ub.minus(ua).times(cr * mb).plus(ua.times(ma)).plus(ub.times(mb)).times(1.0/mamb);

        object.velocity = ua.minus(ub).times(cr * ma).plus(ua.times(ma)).plus(ub.times(mb)).times(1.0/mamb);

    }

    /**
     * Method to check if the object collide with the current object
     * @param object The object to check the collision.
     * @return If collide or not
     */
    public boolean collideWith(GameObject object)
    {
        Vector position = object.getLocation();
        //Fast check distance
        return VectorUtil.distanceMinusTo(position, this.loc, (object.getRadius() + this.radius) * SIZE * StellarCrush.scale);
    }

    /**
     * Method to update the amount of point depending on the mass.
     */
    public void updatePoint()
    {
        //Math.log(GameObjectLibrary.ASTEROID_MIN_MASS) = 22
        new Thread(() -> points = (int) ((Math.log(mass) - Math.log(GameObjectLibrary.ASTEROID_MIN_MASS)))).start();
    }

    public int getId() {
        return id;
    }

    public Vector getLocation()
    {
        return loc;
    }

    public void setLocation(Vector loc)
    {
        this.loc = loc;
    }

    public Vector getVelocity()
    {
        return velocity;
    }

    public void setVelocity(Vector velocity)
    {
        this.velocity = velocity;
    }

    public double getMass() {
        return mass;
    }

    public double getRadius() {
        return radius;
    }

    public double getRealRadius()
    {
        return radius * SIZE * StellarCrush.scale;
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

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
