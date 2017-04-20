import java.util.Random;

public class GameObjectLibrary {
// Class for defining various game objects, and putting them together to create content
// for the game world.  Default assumption is objects face in the direction of their velocity, and are spherical.

    // UNIVERSE CONSTANTS - TUNED BY HAND FOR RANDOM GENERATION
    private static final double ASTEROID_RADIUS = 0.5; // Location of asteroid belt for random initialization
    private static final double ASTEROID_WIDTH = 0.2; // Width of asteroid belt
    private static final double ASTEROID_MIN_MASS = 1E24;
    private static final double ASTEROID_MAX_MASS = 1E26;
    private static final double PLAYER_MASS = 1E25;

    private static final Random random = new Random(0);

    public static PlayerObject createPlayerObject()
    {
        return new PlayerObject(new Vector(2), new Vector(2), PLAYER_MASS);
    }

    public static GameObject createAsteroid(int id)
    {
        double radius = random.nextDouble();
        double mass = ASTEROID_MIN_MASS + radius * (ASTEROID_MAX_MASS - ASTEROID_MIN_MASS);
        /*double pos = random.nextDouble() * Math.PI * 2;
        double distance = (ASTEROID_RADIUS * StellarCrush.scale)+ random.nextDouble() * ASTEROID_WIDTH ;
        double[] position = { Math.cos(pos)*distance, Math.sin(pos)*distance };*/

        double[] position = {(-1.0 * StellarCrush.scale) + random.nextDouble() * StellarCrush.scale * 2.0,
                (-1.0 * StellarCrush.scale) + random.nextDouble() * StellarCrush.scale * 2.0};

        /*double rx = StdIn.readDouble();
        double ry = StdIn.readDouble();
        double vx = StdIn.readDouble();
        double vy = StdIn.readDouble();
        double mass = StdIn.readDouble();
        double[] position = { rx, ry };
        double[] velocity = { vx, vy };*/

        //System.out.println("Coord: " + position[0] + " " + position[1]);
        Vector r = new Vector(position);
        Vector v = new Vector(2);
        return new GameObject(id, r, v, mass, radius);
    }
}