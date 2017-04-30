

import libs.Draw;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GameState {
    // Class representing the game state and implementing main game loop update step.t

    static final double SPLIT_SPEED_MIN = 11000;
    static final double MERGE_SPEED_MAX = 0.013;
    static final double MAX_ASTEROID_NUMBER = 500;

    static final long TIME_PER_SPAWN = 1 * 1000;

    private final Collection<GameObject> objects;
    private final PlayerObject player;

    private int number = 1;

    private long lastSpawnedAsteroid;

    private Draw StdDraw = StellarCrush.getDraw();

    public GameState(PlayerObject player, double radius) {
        this.player = player;
        double ratio = (double) StdDraw.getWidth() / StdDraw.getHeight();
        StdDraw.setXscale(-radius, +radius);
        StdDraw.setYscale(-radius / ratio, +radius / ratio);

        StdDraw.changeWindowTitle("StellarCrush");

        this.objects = new LinkedList<>();

        /*addGameObject(new GameObject(number++,
                new Vector(new double[]{ radius/2.0, radius/2.0 }),
                new Vector(new double[]{ -15300, -15000}),
                1e24,
                1)
        );

        addGameObject(new GameObject(number++,
                new Vector(new double[]{ 0.0, -radius/2.0 }),
                new Vector(new double[]{ 0.0, 18000}),
                1e24,
                1)
        );*/


        for (int i = 0; i < 150; i++)
        {
           addGameObject(GameObjectLibrary.createAsteroidCircle(number++));
        }
        addGameObject(player);
        lastSpawnedAsteroid = System.currentTimeMillis();

        while(checkIntegrity() != 0) //Gameplay arrangement before start
        {

        }
    }

    private int checkIntegrity()
    {
        ExecutorService taskExecutor = Executors.newFixedThreadPool(4);
        Map<GameObject, Collection<GameObject>> collisions = calculateCollisions(taskExecutor);
        taskExecutor.shutdown();
        try{
            taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int i = 0;
        for(Map.Entry<GameObject, Collection<GameObject>> entry : collisions.entrySet())
        {
            GameObject a = entry.getKey();
            Collection<GameObject> collides = entry.getValue();
            for(GameObject b : collides)
            {
                i++;
                collisions.get(b).remove(a);

                a.applyCollide(b);

                //Avoid merge of player
                if(a instanceof PlayerObject)
                {
                    removeGameObject(b);
                    continue;
                }
                if(b instanceof PlayerObject)
                {
                    removeGameObject(a);
                    continue;
                }
                GameObjectLibrary.mergeAsteroid(a, b);
                removeGameObject(b);
            }
        }
        return i;
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

        ExecutorService taskExecutor = Executors.newWorkStealingPool();
        //Compute
        Map<GameObject, Vector> f = calculateForces(taskExecutor);
        Map<GameObject, Collection<GameObject>> collisions = calculateCollisions(taskExecutor);
        taskExecutor.shutdown();
        try{
            taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for(Iterator<GameObject> ite = objects.iterator(); ite.hasNext(); )
        {
            GameObject object = ite.next();

            object.move(f.get(object), delay);
        }

        //Physics
        for(Map.Entry<GameObject, Collection<GameObject>> entry : collisions.entrySet())
        {
            GameObject object = entry.getKey();
            Collection<GameObject> collides = entry.getValue();
            if(object instanceof PlayerObject)
            {
                for (GameObject object1 : collides)
                {
                    player.applyCollide(object1);
                    removeGameObject(object1);
                }
            }else {
                for(GameObject object1 : collides)
                {
                    collisions.get(object1).remove(object);

                    object.applyCollide(object1);

                    //Avoid merge of player
                    if(object1 instanceof PlayerObject)
                    {
                        removeGameObject(object);
                    }else {
                        if(VectorUtil.distanceMinusTo(object.getVelocity(), object1.getVelocity(), MERGE_SPEED_MAX)
                                || VectorUtil.distanceMinusTo(object.getPosition(), object1.getPosition(),
                                (object.getRadius() + object1.getRadius()) * GameObject.SIZE * StellarCrush.scale/2.0))
                        {
                            GameObjectLibrary.mergeAsteroid(object, object1);
                            removeGameObject(object1);
                        }
                        if(VectorUtil.distanceMaxTo(object.getVelocity(), object1.getVelocity(), SPLIT_SPEED_MIN)) {

                            GameObject toSplit = object1;

                            if(object.getRadius() > object1.getRadius())
                                toSplit = object;
                            addGameObject(GameObjectLibrary.splitAsteroid(number++, toSplit));
                        }
                    }


                }
            }
        }
        //System.out.println("Player speed: " + player.getVelocity().magnitude());

        //objects.removeIf(object -> !(object instanceof PlayerObject) && player.collideWith(object));
        //Check in bounds
        /*for(GameObject object : objects)
            checkPosition(object);*/
        checkPosition(player);

        processGamePlay();
    }

    public void checkPosition(GameObject object)
    {
        Vector position = object.getPosition();
        Vector velocity = object.getVelocity();
        double xmin = StellarCrush.getDraw().getXmin();
        double xmax = StellarCrush.getDraw().getXmax();
        double ymin = StellarCrush.getDraw().getYmin();
        double ymax = StellarCrush.getDraw().getYmax();

        if(position.cartesian(0) - object.getRealRadius() < xmin)
        {
            double[] vec = {velocity.cartesian(0) * -1.0 , velocity.cartesian(1)};
            object.setVelocity(new Vector(vec));

           /* double[] pos = {Math.abs(velocity.cartesian(1) - StdDraw.getXmin()), 0.0};
            object.setPosition(object.getPosition().plus(new Vector(pos)));*/
        }

        if(position.cartesian(0) + object.getRealRadius() > xmax)
        {
            double[] vec = {velocity.cartesian(0) * -1.0 , velocity.cartesian(1)};
            object.setVelocity(new Vector(vec));

            /*double[] pos = {-Math.abs(velocity.cartesian(1) - StdDraw.getXmax()), 0.0};
            object.setPosition(object.getPosition().plus(new Vector(pos)));*/
        }


        if(position.cartesian(1) - object.getRealRadius() < ymin)
        {
            double[] vec = {velocity.cartesian(0), velocity.cartesian(1) * -1.0};
            object.setVelocity(new Vector(vec));

           /* double[] pos = {0.0, Math.abs(velocity.cartesian(1) - StdDraw.getYmin())};
            object.setPosition(object.getPosition().plus(new Vector(pos)));*/
        }

        if(position.cartesian(1) + object.getRealRadius() > ymax)
        {
            double[] vec = {velocity.cartesian(0), velocity.cartesian(1) * -1.0};
            object.setVelocity(new Vector(vec));

            /*double[] pos = {0.0, -Math.abs(velocity.cartesian(1) - StdDraw.getYmax())};
            object.setPosition(object.getPosition().plus(new Vector(pos)));*/
        }
    }

    public void processGamePlay()
    {
        if(System.currentTimeMillis() - lastSpawnedAsteroid > TIME_PER_SPAWN)
        {
            if(objects.size() < MAX_ASTEROID_NUMBER) //We don't want to crash the game
            {
                addGameObject((Math.random() > 0.1) ? GameObjectLibrary.createAsteroidCircle(number++) : GameObjectLibrary.createBulltAsteroid(number++));
            }
            lastSpawnedAsteroid = System.currentTimeMillis();
        }
    }

    private Map<GameObject,Vector> calculateForces(ExecutorService taskExecutor) {

        Map<GameObject, Vector> map = new ConcurrentHashMap<>();

        for (GameObject object : this.objects) {
            map.put(object, new Vector(new double[2]));
        }

        for (GameObject i : this.objects) {
            for (GameObject j : this.objects) {
                taskExecutor.execute(() -> {
                    if (i.getId() != j.getId()) {
                        map.put(i, map.get(i).plus(i.forceFrom(j)));
                    }
                });
            }
        }

        return map;
    }


    private Map<GameObject, Collection<GameObject>> calculateCollisions(ExecutorService taskExecutor)
    {
        Map<GameObject, Collection<GameObject>> map = new ConcurrentHashMap<>();

        for (GameObject object : objects)
        {
            map.put(object, new HashSet<>());
        }

        for (GameObject object : this.objects)
        {
            for(GameObject object1 : objects)
            {
                taskExecutor.execute(() -> {
                    if(object1.getId() == object.getId()
                            || !object.collideWith(object1))
                        return;

                    map.get(object).add(object1);
                    map.get(object1).add(object);
                });
            }
        }
        return map;
    }

    synchronized void draw() {
        for (GameObject object : this.objects)
            object.draw();

        new Thread(() -> player.getCam().render(objects)).start();
    }

}
