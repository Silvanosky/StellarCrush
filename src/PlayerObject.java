import libs.Draw;
import libs.Entry;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class PlayerObject extends GameObject implements IViewPort {

    private static final Color DEFAULT_COLOR = Draw.WHITE;
    private static final Color DEFAULT_FACING_COLOR = Draw.BLACK;
    private static final double DEFAULT_FOV = Math.PI/2; // field of view of player's viewport
    private static final double FOV_INCREMENT = Math.PI/36; // rotation speed

    private Camera cam;

    private double yaw;

    private int score = 20;

    public PlayerObject(Vector r, Vector v, double mass, double radius) {
        super(-1, r, v, mass, radius);

        this.cam = new Camera(this, DEFAULT_FOV);
        this.yaw = 0;
    }

    private double clampYaw(double yaw)
    {
        double nyaw = yaw;
        if(yaw > Math.PI)
            nyaw += -Math.PI * 2.0;
        if(yaw < -Math.PI)
            nyaw += Math.PI * 2.0;
        return nyaw;
    }

    public void eat(GameObject object)
    {
        Vector position = object.getLocation();

        double deltaX = (position.cartesian(0) - getLocation().cartesian(0)) / StellarCrush.scale;
        double deltaY = (position.cartesian(1) - getLocation().cartesian(1)) / StellarCrush.scale;
        double angle = clampYaw(Math.atan2(deltaY, deltaX) - yaw);

        int point = object.getPoints();
        if(Math.abs(angle) > Math.PI/2.0)
            point *= -1;

        score += point;
        if(score >= 100) // TODO FINISH GAME
            score = 100;
    }

    //@Override
    void processCommand(int delay) {
        // Process keys applying to the player
		// Retrieve 
        if (cam != null) {
            // No commands if no draw canvas to retrieve them from!
            Draw dr = cam.getDraw();
            if (dr != null) {
                Vector direction = getFacingVector();
                if (StellarCrush.getListener().isUp())
                {
                    setVelocity(getVelocity().plus(direction.times(100.0)));
                }
                if (StellarCrush.getListener().isDown())
                {
                    setVelocity(getVelocity().plus(direction.times(-100.0)));
                }

                //Rotation
                if (StellarCrush.getListener().isRight())
                    yaw -= FOV_INCREMENT;
                if (StellarCrush.getListener().isLeft())
                    yaw += FOV_INCREMENT;

                yaw = clampYaw(yaw);// -Pi -> Pi
            }
        }
    }

    @Override
    public void draw(Draw dr)
    {
        //Circle for direction
        dr.setPenColor(Color.RED);
        double rayon = getRadius() * SIZE * StellarCrush.scale;

        double length = 0.018 * StellarCrush.scale;
        double width = Math.PI/12;

        Collection<Map.Entry<Double, Double>> points = new ArrayList<>();

        points.add(new Entry<>(
                getLocation().cartesian(0) + (Math.cos(yaw - width) * rayon),
                getLocation().cartesian(1) + (Math.sin(yaw - width) * rayon)
        ));
        points.add(new Entry<>(
                getLocation().cartesian(0) + (Math.cos(yaw + width) * rayon),
                getLocation().cartesian(1) + (Math.sin(yaw + width) * rayon)
        ));

        points.add(new Entry<>(
                getLocation().cartesian(0) + (Math.cos(yaw) * (rayon + length)),
                getLocation().cartesian(1) + (Math.sin(yaw) * (rayon + length))
        ));

        dr.filledPolygon(points);

        super.draw(dr);
    }

    public Camera getCam() {
        return cam;
    }

    @Override
    public Vector getFacingVector() {
        return new Vector(new double[]{Math.cos(yaw), Math.sin(yaw)});
    }

    @Override
    public double highlightLevel() {
        return 0;
    }

    @Override
    public double getYaw() {
        return yaw;
    }

    @Override
    public int getScore() {
        return score;
    }

    public void incrementScore(int value)
    {
        score += value;
    }
}
