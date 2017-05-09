import java.util.Random;

public class GameObjectLibrary {
// Class for defining various game objects, and putting them together to create content
// for the game world.  Default assumption is objects face in the direction of their velocity, and are spherical.

    // UNIVERSE CONSTANTS - TUNED BY HAND FOR RANDOM GENERATION
    private static final double ASTEROID_RADIUS = 0.5; // Location of asteroid belt for random initialization
    private static final double ASTEROID_WIDTH = 0.2; // Width of asteroid belt
    public static final double ASTEROID_MIN_MASS = 1E22;
    private static final double ASTEROID_MAX_MASS = 1E24;
    private static final double PLAYER_MASS = 1E26;

    private static final double SQUARE_OF_TWO = Math.sqrt(2.0);

    private static final Random random = new Random(0);//seed = 0 for test

    private GameObjectLibrary()
    {

    }

    public static PlayerObject createPlayerObject()
    {
        return new PlayerObject(new Vector(2), new Vector(2), PLAYER_MASS, 0.05);
    }

    public static GameObject createAsteroidCircle(int id)
    {
        double mass = ASTEROID_MIN_MASS + random.nextDouble() * (ASTEROID_MAX_MASS - ASTEROID_MIN_MASS);
        double radius = 2.0 + random.nextDouble() * 1.0;

        double pos = random.nextDouble() * Math.PI * 2;
        double distance = (ASTEROID_RADIUS * StellarCrush.scale)+ random.nextDouble() * ASTEROID_WIDTH * StellarCrush.scale ;
        double[] position = {Math.cos(pos)*distance, Math.sin(pos)*distance};

        Vector r = new Vector(position);
        Vector v = new Vector(new double[]{Math.sin(pos), -Math.cos(pos)}).times(2200);
        return new GameObject(id, r, v, mass, radius);
    }

    public static GameObject createBulletAsteroid(int id)
    {
        double mass = ASTEROID_MIN_MASS + random.nextDouble() * (ASTEROID_MAX_MASS - ASTEROID_MIN_MASS);
        mass *= 2;
        double radius = 1.5 + random.nextDouble() * 0.5;

        double pos = random.nextDouble() * Math.PI * 2;
        double distance = StellarCrush.scale;
        double[] position = {Math.cos(pos)*distance, Math.sin(pos)*distance};

        Vector r = new Vector(position);
        Vector v = r.direction().times(-50000);
        return new GameObject(id, r, v, mass, radius);
    }

    public static GameObject createAsteroidRandom(int id)
    {
        double mass = ASTEROID_MIN_MASS + random.nextDouble() * (ASTEROID_MAX_MASS - ASTEROID_MIN_MASS);
        double radius = 1.0 + random.nextDouble() * 0.5;

        double[] position = {(-1.0 * StellarCrush.scale) + random.nextDouble() * StellarCrush.scale * 2.0,
                (-1.0 * StellarCrush.scale) + random.nextDouble() * StellarCrush.scale * 2.0};

        Vector r = new Vector(position);
        Vector v = new Vector(new double[]{-1 + random.nextDouble() * 2.0, -1 + random.nextDouble() * 2.0}).times(5000);
        return new GameObject(id, r, v, mass, radius);
    }

    public static GameObject splitAsteroid(int id, GameObject gameObject)
    {
        gameObject.setMass(gameObject.getMass()/2.0);
        gameObject.setRadius(gameObject.getRadius()/SQUARE_OF_TWO);
        gameObject.updatePoint();

        double angle = random.nextDouble() * Math.PI * 2.0;
        double radius = gameObject.getRadius();
        Vector vector = new Vector(new double[]{
                Math.cos(angle) * (radius*2.0) * GameObject.SIZE * StellarCrush.scale,
                Math.sin(angle) * (radius*2.0) * GameObject.SIZE * StellarCrush.scale});

        GameObject cloned = new GameObject(id,
                gameObject.getLocation().plus(vector),
                gameObject.getVelocity(),
                gameObject.getMass(),
                42.0);
        cloned.setRadius(gameObject.getRadius());
        cloned.updatePoint();
        cloned.setColor(gameObject.getColor());

        return cloned;
    }

    public static void mergeAsteroid(GameObject a, GameObject b)
    {
        a.setMass(a.getMass() + b.getMass());
        a.setRadius(Math.sqrt(a.getRadius() * a.getRadius() + b.getRadius() * b.getRadius()));
        a.setVelocity(a.getVelocity().plus(b.getVelocity()).times(0.5));

        Vector position = (a.getRadius() > b.getRadius())?a.getLocation():b.getLocation();
        a.setLocation(position);
        a.updatePoint();
    }

    public static Random getRandom() {
        return random;
    }
}
