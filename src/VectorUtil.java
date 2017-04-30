import libs.RootMath;

public class VectorUtil {
    // Class containing additional utility functions for working with vectors.

    public static final Vector TWO_D_ZERO = new Vector(new double[]{0, 0});

    static Vector rotate(Vector v, double ang) {
        // Rotate v by ang radians - two dimensions only.
        return null;
    }

    static Vector direction(Vector v) {
        // Returns direction of v, but sets angle to Math.PI/2 when v is the zero vector
		// Used to avoid exception in Vector.java
        return null;
    }

    static Vector fastDir(Vector v)
    {
        return v.times(RootMath.invSqrtApprox(RootMath.sqrt((float) v.dot(v))));
    }

    //Check the distance between two vector without calling sqrt which have heavy complexity
    static boolean distanceMinusTo(Vector a, Vector b, double distance)
    {
        Vector minus = a.minus(b);
        return minus.dot(minus) < distance * distance;
    }

    //Check the distance between two vector without calling sqrt which have heavy complexity
    static boolean distanceMaxTo(Vector a, Vector b, double distance)
    {
        Vector minus = a.minus(b);
        return minus.dot(minus) > distance * distance;
    }

}
