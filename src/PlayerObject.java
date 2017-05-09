import libs.Draw;
import libs.IEntry;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class PlayerObject extends GameObject implements IViewPort {

    private static final Color DEFAULT_COLOR = Draw.WHITE;
    private static final Color DEFAULT_FACING_COLOR = Draw.BLACK;
    private static final double DEFAULT_FOV = Math.PI/2.0; // field of view of player's viewport
    private static final double FOV_INCREMENT = Math.PI/36; // rotation speed

    private static final double USE_OF_FUEL_THRUST = 0.012;
    private static final double USE_OF_FUEL_ROTATION = 0.008;

    private Camera cam;

    private double yaw;
    private Vector facingVector;

    private double score = 20;

    /**
     * Create a player instance
     * @param r The position of the player
     * @param v The starting velocity of the player
     * @param mass The mass for the player
     * @param radius The radius
     */
    public PlayerObject(Vector r, Vector v, double mass, double radius) {
        super(-1, r, v, mass, radius);
        updateFacingVector();

        this.cam = new Camera(this, DEFAULT_FOV);
        this.yaw = 0;
    }

    /**
     * Method which clamp and angle to stay in the good rand [-PI, PI]
     * @param yaw The angle to clamp
     * @return The clamped angle
     */
    private double clampYaw(double yaw)
    {
        double nyaw = yaw;
        if(yaw > Math.PI)
            nyaw += -Math.PI * 2.0;
        if(yaw < -Math.PI)
            nyaw += Math.PI * 2.0;
        return nyaw;
    }

    /**
     * Method called when the player eat an object.
     * This method determine of the player gain or loose point depending on the situation.
     * @param object The object to be ate by the player.
     */
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

        checkScore();

    }

    /**
     * Method to update the saved facing vector corresponding to angle.
     * This avoid to recompute the sin and cos of the angle each time we need it.
     * Also we update it any time we change the yaw.
     */
    private void updateFacingVector()
    {
        this.facingVector = new Vector(new double[]{Math.cos(yaw), Math.sin(yaw)});
    }

    /**
     * Method that check if the game is over depending on the current score.
     */
    public void checkScore()
    {
        //Game over
        if(score >= 100) {
            score = 100;
            StellarCrush.setState(2);
        }
        if(score <= 0) {
            score = 0;
            StellarCrush.setState(2);
        }
    }

    /**
     * Utility method which handle the Input for the player.
     * This method handle the thrust and rotation.
     * @param delay The delay to apply to the input (not used)
     */
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
                    score -= USE_OF_FUEL_THRUST;
                }
                if (StellarCrush.getListener().isDown())
                {
                    setVelocity(getVelocity().plus(direction.times(-100.0)));
                    score -= USE_OF_FUEL_THRUST;
                }

                //Rotation
                if (StellarCrush.getListener().isRight())
                {
                    yaw -= FOV_INCREMENT;
                    score -= USE_OF_FUEL_ROTATION;

                    yaw = clampYaw(yaw);// -Pi -> Pi
                    updateFacingVector();
                }
                if (StellarCrush.getListener().isLeft())
                {
                    yaw += FOV_INCREMENT;
                    score -= USE_OF_FUEL_ROTATION;

                    yaw = clampYaw(yaw);// -Pi -> Pi
                    updateFacingVector();
                }

                checkScore();
            }
        }
    }

    /**
     * Draw the object on the main screen.
     * @param dr The screen to draw
     */
    @Override
    public void draw(Draw dr)
    {
        //Circle for direction
        dr.setPenColor(Color.RED);
        double rayon = getRadius() * SIZE * StellarCrush.scale;

        double length = 0.018 * StellarCrush.scale;
        double width = Math.PI/12;

        Collection<Map.Entry<Double, Double>> points = new HashSet<>();

        points.add(new IEntry<>(
                getLocation().cartesian(0) + (Math.cos(yaw - width) * rayon),
                getLocation().cartesian(1) + (Math.sin(yaw - width) * rayon)
        ));
        points.add(new IEntry<>(
                getLocation().cartesian(0) + (Math.cos(yaw + width) * rayon),
                getLocation().cartesian(1) + (Math.sin(yaw + width) * rayon)
        ));

        points.add(new IEntry<>(
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
        return facingVector;
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
    public double getScore() {
        return score;
    }

    public void incrementScore(int value)
    {
        score += value;
    }

    /**
     * The method to close all the needed stuff.
     */
    public void close() {
        cam.close();
        cam = null;
    }
}
