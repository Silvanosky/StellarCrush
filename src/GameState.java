

import libs.Draw;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GameState {
    // Class representing the game state and implementing main game loop update step.t

    private static final double SPLIT_SPEED_MIN = 11000;
    private static final double MERGE_SPEED_MAX = 0.013;
    private static final double MAX_ASTEROID_NUMBER = 500;

    private static final long TIME_PER_SPAWN = 1000;

    private final Collection<GameObject> objects;
    private final PlayerObject player;

    private int number = 1;

    private long lastSpawnedAsteroid;

    /**
     * Create an game instance
     * @param player The game player instance
     * @param radius The radius of this game
     */
    GameState(PlayerObject player, double radius) {
        this.player = player;
        Draw draw = StellarCrush.getDraw();
        double ratio = (double) draw.getWidth() / draw.getHeight();
        draw.setXscale(-radius, +radius);
        draw.setYscale(-radius / ratio, +radius / ratio);

        draw.changeWindowTitle("StellarCrush");

        this.objects = new HashSet<>();

        for (int i = 0; i < 150; i++)
        {
           addGameObject(GameObjectLibrary.createAsteroidCircle(number++));
        }
        addGameObject(player);
        lastSpawnedAsteroid = System.currentTimeMillis();

        while(checkIntegrity() != 0); //Gameplay arrangement before start

    }

    /**
     * Add an object to the game world
     * @param object The object to add
     */
    private synchronized void addGameObject(GameObject object)
    {
        this.objects.add(object);
    }

    /**
     * Remove and object from the game world
     * @param object The object to remove
     */
    private synchronized void removeGameObject(GameObject object)
    {
        this.objects.remove(object);
    }

    /**
     * Utility method which check if some objects collides and merge then in that case.
     * @return The number of object that was merged during this check.
     */
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

    /**
     * The main game loop method. This method update the game for the desired delay.
     * @param delay The delay to add to the game
     */
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
                //collisions.get(object1).remove(object);

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
        checkPosition(player);

        processGamePlay();
    }

    /**
     * Utility method called just after a collision between 2 objects.
     * This method determine depending on the collision if we need to split or merge the object or do nothing.
     * @param object The first object colliding.
     * @param object1 The second object colliding.
     */
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

    /**
     * Method that check if an object is in the border and if not revert the velocity to make it come back.
     * Used to keep the player in the playing window.
     * @param object The object to do the check.
     */
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

    /**
     * Method called at the end of each frame to apply gameplay feature like asteroid spawn.
     */
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

    /**
     * Compute all the force occurring on the objects and return it.
     * The compute is multi-threaded using the current pool given in parameter.
     *
     * @param taskExecutor The pool of thread to compute the result.
     * @return Map of GameObject and Vector corresponding to the force for each object.
     */
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

    /**
     * Compute all the force occurring on the objects and return it.
     * The compute is multi-threaded using the current pool given in parameter.
     *
     * @param taskExecutor The pool of thread to compute the result.
     * @return Map of GameObject and Vector corresponding to the objects colliding.
     */
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
                    if(object1.getId() <= object.getId()
                            || !object.collideWith(object1))
                        return;

                    map.get(object).add(object1);
                    //map.get(object1).add(object);
                });
            }
        }
        return map;
    }

    /**
     * Method used to draw all the object on the main window.
     * This method also refresh the First person view.
     */
    synchronized void draw() {
        for (GameObject object : this.objects)
            object.draw(StellarCrush.getDraw());

        new Thread(() -> player.getCam().render(objects)).start();
    }

    /**
     * Method to cleanup before deletion
     */
    public void close()
    {
        player.close();
    }

    /**
     * Get the player of the game.
     * @return Instance of PlayerObject.
     */
    public PlayerObject getPlayer() {
        return player;
    }
}
