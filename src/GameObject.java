public class GameObject {
    private int id;

    private Vector r; // position
    private Vector v; // velocity
    private double mass; // mass

    public GameObject(int id, Vector r, Vector v, double mass) {
        this.id = id;
        this.r = r;
        this.v = v;
        this.mass = mass;
    }

    public void move(Vector f, double dt) {
        Vector a = f.times(1/mass);
        v = v.plus(a.times(dt));
        r = r.plus(v.times(dt));
    }

    public Vector forceFrom(GameObject that) {
        double G = 6.67e-11;
        Vector delta = that.r.minus(this.r);
        double dist = delta.magnitude();
        double F = (G * this.mass * that.mass) / (dist * dist);
        return delta.direction().times(F);
    }

    public void draw() {
        StdDraw.setPenRadius(0.025);
        StdDraw.point(r.cartesian(0), r.cartesian(1));
    }

    public int getId() {
        return id;
    }
}
