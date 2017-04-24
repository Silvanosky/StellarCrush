import libs.StdDraw;

import java.util.*;

public class GameState {
    // Class representing the game state and implementing main game loop update step.

    static final double SPLIT_SPEED_MIN = 10000;
    static final double MERGE_SPEED_MAX = 500;

    private final Collection<GameObject> objects;
    private final PlayerObject player;

    private int number = 1;

    public GameState(PlayerObject player, double radius) {
        this.player = player;
        StdDraw.setXscale(-radius, +radius);
        StdDraw.setYscale(-radius, +radius);

        StdDraw.changeWindowTitle("StellarCrush");

        this.objects = new LinkedList<>();

       /* addGameObject(new GameObject(number++,
                new Vector(new double[]{ radius/2.0, radius/2.0 }),
                new Vector(new double[]{ -15000, -15000}),
                1e24,
                1,
                0.5)
        );

        addGameObject(new GameObject(number++,
                new Vector(new double[]{ 0.0, -radius/2.0 }),
                new Vector(new double[]{ 0.0, 18000}),
                1e24,
                1,
                0.5)
        );*/


        for (int i = 0; i < 20; i++)
        {
           addGameObject(GameObjectLibrary.createAsteroidRandom(number++));
        }
        addGameObject(player);
    }

    public synchronized void addGameObject(GameObject object)
    {
        this.objects.add(object);
    }

    public synchronized void removeGameObject(GameObject object)
    {
        this.objects.remove(object);
    }

    synchronized void update(int delay) {
        //Input
        this.player.processCommand(delay);

        //Compute
        Map<GameObject, Vector> f = calculateForces();
        for(Iterator<GameObject> ite = objects.iterator(); ite.hasNext(); )
        {
            GameObject object = ite.next();

            object.move(f.get(object), delay);

            Vector position = object.getPosition();
            Vector velocity = object.getVelocity();

            if(Math.abs(position.cartesian(0)) > StellarCrush.scale)
            {
                double[] vec = {position.cartesian(0) * -1.0 , position.cartesian(1)};
                object.setPosition(new Vector(vec));
            }

            if(Math.abs(position.cartesian(1)) > StellarCrush.scale)
            {
                double[] vec = {position.cartesian(0), position.cartesian(1) * -1.0};
                object.setPosition(new Vector(vec));
            }
        }

        //Physics
        Map<GameObject, GameObject> collisions = calculateCollisions();
        for(Map.Entry<GameObject, GameObject> entry : collisions.entrySet())
        {
            GameObject object = entry.getKey();
            GameObject object1 = entry.getValue();
            object.applyCollide(object1);

            //Avoid merge of player
            if(object instanceof PlayerObject)
            {
                removeGameObject(object1);
                continue;
            }
            if(object1 instanceof PlayerObject)
            {
                removeGameObject(object);
                continue;
            }

            if(object.getVelocity().distanceTo(object1.getVelocity()) < MERGE_SPEED_MAX)
            {
                GameObjectLibrary.mergeAsteroid(object, object1);
                removeGameObject(object1);
            }

            if(object.getVelocity().magnitude() > SPLIT_SPEED_MIN) {
                addGameObject(GameObjectLibrary.splitAsteroid(number++, object));
            }
        }

        //System.out.println("Player speed: " + player.getVelocity().magnitude());

        objects.removeIf(object -> !(object instanceof PlayerObject) && player.collideWith(object));
    }

    private Map<GameObject,Vector> calculateForces() {
        Map<GameObject, Vector> map = new HashMap<>();

        for (GameObject object : this.objects) {
            map.put(object, new Vector(new double[2]));
        }

        for (GameObject i : this.objects) {
            for (GameObject j : this.objects) {
                if (i.getId() != j.getId()) {
                    map.put(i, map.get(i).plus(i.forceFrom(j)));
                }
            }
        }

        return map;
    }


    private Map<GameObject, GameObject> calculateCollisions()
    {
        Map<GameObject, GameObject> map = new HashMap<>();
        for (GameObject object : this.objects)
        {

            for(GameObject object1 : this.objects)
            {
                if(object1.getId() == object.getId()
                        || !object.collideWith(object1))
                    continue;

                if(!map.containsKey(object) && !map.containsKey(object1))
                    map.put(object, object1);
            }
        }
        return map;
    }

    synchronized void draw() {
        for (GameObject object : this.objects)
            object.draw();

        player.getCam().render(objects);
    }

}
