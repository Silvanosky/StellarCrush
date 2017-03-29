public class VectorUtil {
    // Class containing additional utility functions for working with vectors.

    public static final Vector TWO_D_ZERO = new Vector(new double[]{0, 0});

    static Vector rotate(Vector v, double ang) {
        // Rotate v by ang radians - two dimensions only.
    }

    static Vector direction(Vector v) {
        // Returns direction of v, but sets angle to Math.PI/2 when v is the zero vector
		// Used to avoid exception in Vector.java
    }
}
