import java.util.*;

public class GameState {
    // Class representing the game state and implementing main game loop update step.

    private Collection<GameObject> objects;
    private final PlayerObject player;

    public GameState(PlayerObject player, double radius) {
        this.player = player;
        StdDraw.setCanvasSize();
        StdDraw.setXscale(-radius, +radius);
        StdDraw.setYscale(-radius, +radius);

        StdDraw.changeWindowTitle("StellarCrush");

        objects = new LinkedList<>();

        for (int i = 0; i < 2; i++)
        {
            objects.add(GameObjectLibrary.createAsteroid(i));
        }
    }

    void update(int delay) {
        Map<GameObject, Vector> f = calculateForces();

        for(GameObject object : objects)
            object.move(f.get(object), delay);
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
    }

}
