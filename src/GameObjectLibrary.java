import java.util.Random;

public class GameObjectLibrary {
// Class for defining various game objects, and putting them together to create content
// for the game world.  Default assumption is objects face in the direction of their velocity, and are spherical.

    // UNIVERSE CONSTANTS - TUNED BY HAND FOR RANDOM GENERATION
    private static final double ASTEROID_RADIUS = 0.5; // Location of asteroid belt for random initialization
    private static final double ASTEROID_WIDTH = 0.2; // Width of asteroid belt
    private static final double ASTEROID_MIN_MASS = 1E24;
    private static final double ASTEROID_MAX_MASS = 1E25;
    private static final double PLAYER_MASS = 1E25;

    private static final double SQUARE_OF_TWO = Math.sqrt(2.0);

    private static final Random random = new Random(0);

    public static PlayerObject createPlayerObject()
    {
        double radius = 5.0 + random.nextDouble() * 5.0;
        return new PlayerObject(new Vector(2), new Vector(2), PLAYER_MASS, 0.5);
    }

    public static GameObject createAsteroidRandom(int id)
    {
        double mass = ASTEROID_MIN_MASS + random.nextDouble() * (ASTEROID_MAX_MASS - ASTEROID_MIN_MASS);
        double radius = 1.0 + random.nextDouble() * 0.5;

        double pos = random.nextDouble() * Math.PI * 2;
        double distance = (ASTEROID_RADIUS * StellarCrush.scale)+ random.nextDouble() * ASTEROID_WIDTH ;
        double[] position = {Math.cos(pos)*distance, Math.sin(pos)*distance};

       /* double[] position = {(-1.0 * StellarCrush.scale) + random.nextDouble() * StellarCrush.scale * 2.0,
                (-1.0 * StellarCrush.scale) + random.nextDouble() * StellarCrush.scale * 2.0};*/

        Vector r = new Vector(position);
        Vector v = new Vector(new double[]{- position[1]/ position[0], 1.0}).times(500);
        return new GameObject(id, r, v, mass, radius);
    }

    public static GameObject splitAsteroid(int id, GameObject gameObject)
    {
        Vector vector = new Vector(new double[]{
                gameObject.getRadius() * GameObject.SIZE * StellarCrush.scale,
                gameObject.getRadius() * GameObject.SIZE * StellarCrush.scale});

        GameObject cloned = new GameObject(id,
                gameObject.getPosition().plus(vector),
                gameObject.getVelocity(),
                gameObject.getMass()/2.0,
                gameObject.getRadius() /2.0);

        gameObject.setMass(gameObject.getMass()/2.0);
        gameObject.setRadius(gameObject.getRadius()/SQUARE_OF_TWO);

        cloned.setColor(gameObject.getColor());

        return cloned;
    }

    public static void mergeAsteroid(GameObject a, GameObject b)
    {
        a.setMass(a.getMass() + b.getMass() /2.0);
        a.setRadius( Math.max(a.getRadius(), b.getRadius()) + a.getRadius() + b.getRadius()/2.0);
        a.setVelocity(a.getVelocity().plus(b.getVelocity()));
    }
}
