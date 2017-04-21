import libs.StdDraw;

import java.util.*;

public class GameState {
    // Class representing the game state and implementing main game loop update step.

    private Collection<GameObject> objects;
    private final PlayerObject player;

    public GameState(PlayerObject player, double radius) {
        this.player = player;
        StdDraw.setXscale(-radius, +radius);
        libs.StdDraw.setYscale(-radius, +radius);

        StdDraw.changeWindowTitle("StellarCrush");

        objects = new LinkedList<>();

        for (int i = 0; i < 10; i++)
        {
            objects.add(GameObjectLibrary.createAsteroid(i));
        }
        objects.add(player);
    }

    void update(int delay) {
        //Compute
        Map<GameObject, Vector> f = calculateForces(); //TODO async

        for(GameObject object : objects) {

            object.move(f.get(object), delay);
            Vector position = object.getPosition();
            Vector velocity = object.getVelocity();

            if(Math.abs(position.cartesian(0)) > StellarCrush.scale)
            {
                double[] vec = {velocity.cartesian(0) * -1, velocity.cartesian(1) };
                object.setVelocity(new Vector(vec));
            }

            if(Math.abs(position.cartesian(1)) > StellarCrush.scale)
            {
                double[] vec = {velocity.cartesian(0), velocity.cartesian(1) *-1};
                object.setVelocity(new Vector(vec));
            }
        }
        //Draw
        player.getCam().render(objects);//TODO async
    }

    private Map<GameObject,Vector> calculateForces() {
        Map<GameObject, Vector> map = new HashMap<>();

        for (GameObject object : objects) {
            map.put(object, new Vector(new double[2]));
        }

        for (GameObject i : objects)
            for (GameObject j : objects)
                if (i.getId() != j.getId())
                    map.put(i, map.get(i).plus(i.forceFrom(j)));

        return map;
    }

    void draw() {
        for (GameObject object : objects)
            object.draw();
        //Input
        player.processCommand(0);
    }

}
