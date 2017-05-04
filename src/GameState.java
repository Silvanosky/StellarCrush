

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

    private final Draw StdDraw = StellarCrush.getDraw();

    GameState(PlayerObject player, double radius) {
        this.player = player;
        double ratio = (double) StdDraw.getWidth() / StdDraw.getHeight();
        StdDraw.setXscale(-radius, +radius);
        StdDraw.setYscale(-radius / ratio, +radius / ratio);

        StdDraw.changeWindowTitle("StellarCrush");

        this.objects = new LinkedList<>();

        for (int i = 0; i < 150; i++)
        {
           addGameObject(GameObjectLibrary.createAsteroidCircle(number++));
        }
        addGameObject(player);
        lastSpawnedAsteroid = System.currentTimeMillis();

        while(checkIntegrity() != 0); //Gameplay arrangement before start

    }

    private synchronized void addGameObject(GameObject object)
    {
        this.objects.add(object);
    }

    private synchronized void removeGameObject(GameObject object)
    {
        this.objects.remove(object);
    }

    private int checkIntegrity()
    {
        ExecutorService taskExecutor = Executors.newWorkStealingPool();
        Map<GameObject, Collection<GameObject>> collisions = calculateCollisions(taskExecutor);
        taskExecutor.shutdown();
        try{
            taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ignored) {
            ignored.printStackTrace();
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
                GameObjectLibrary.mergeAsteroid(a, b);
                removeGameObject(b);
            }
        }
        return i;
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

        for(GameObject object : this.objects)
        {
            object.move(f.get(object), delay);
        }

        //Physics
        for(Map.Entry<GameObject, Collection<GameObject>> entry : collisions.entrySet())
        {
            GameObject object = entry.getKey();
            Collection<GameObject> collides = entry.getValue();

            for(GameObject object1 : collides)
            {
                collisions.get(object1).remove(object);

                object.applyCollide(object1);
                if(object instanceof PlayerObject)
                {
                    player.eat(object1);
                    removeGameObject(object1);
                }else if(object1 instanceof PlayerObject)
                {
                    player.eat(object);
                    removeGameObject(object);
                }else {
                    processCollisions(object, object1);
                }
            }
        }
        //Check in bounds
        /*for(GameObject object : objects)
            checkPosition(object);*/
        checkPosition(player);

        processGamePlay();
    }

    //Process merge or split depending on environment
    private void processCollisions(GameObject object, GameObject object1)
    {
        if(VectorUtil.distanceMinusTo(object.getVelocity(), object1.getVelocity(), MERGE_SPEED_MAX)
                || VectorUtil.distanceMinusTo(object.getLocation(), object1.getLocation(),
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

    //Check position of an object and bounce him if out
    private void checkPosition(GameObject object)
    {
        Vector position = object.getLocation();
        Vector velocity = object.getVelocity();
        double xmin = StellarCrush.getDraw().getXmin();
        double xmax = StellarCrush.getDraw().getXmax();
        double ymin = StellarCrush.getDraw().getYmin();
        double ymax = StellarCrush.getDraw().getYmax();

        if(position.cartesian(0) - object.getRealRadius() < xmin)
        {
            double[] vec = {velocity.cartesian(0) * -1.0 , velocity.cartesian(1)};
            object.setVelocity(new Vector(vec));
        }

        if(position.cartesian(0) + object.getRealRadius() > xmax)
        {
            double[] vec = {velocity.cartesian(0) * -1.0 , velocity.cartesian(1)};
            object.setVelocity(new Vector(vec));
        }


        if(position.cartesian(1) - object.getRealRadius() < ymin)
        {
            double[] vec = {velocity.cartesian(0), velocity.cartesian(1) * -1.0};
            object.setVelocity(new Vector(vec));
        }

        if(position.cartesian(1) + object.getRealRadius() > ymax)
        {
            double[] vec = {velocity.cartesian(0), velocity.cartesian(1) * -1.0};
            object.setVelocity(new Vector(vec));
        }
    }

    //Gameplay
    private void processGamePlay()
    {
        if(System.currentTimeMillis() - lastSpawnedAsteroid > TIME_PER_SPAWN)
        {
            if(objects.size() < MAX_ASTEROID_NUMBER) //We don't want to crash the game
            {
                addGameObject((Math.random() > 0.05) ? GameObjectLibrary.createAsteroidCircle(number++) : GameObjectLibrary.createBulletAsteroid(number++));
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
            object.draw(StellarCrush.getDraw());

        new Thread(() -> player.getCam().render(objects)).start();
    }

    public PlayerObject getPlayer() {
        return player;
    }

    public void close() {
        player.close();
    }
}
