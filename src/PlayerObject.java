import libs.Draw;
import libs.StdDraw;

import java.awt.*;
import java.awt.event.KeyEvent;

public class PlayerObject extends GameObject implements IViewPort {

    private static final Color DEFAULT_COLOR = StdDraw.WHITE;
    private static final Color DEFAULT_FACING_COLOR = libs.StdDraw.BLACK;
    private static final double DEFAULT_FOV = Math.PI/2; // field of view of player's viewport
    private static final double FOV_INCREMENT = Math.PI/36; // rotation speed

    private Camera cam;

    private double yaw;

    public PlayerObject(Vector r, Vector v, double mass) {
        super(-1, r, v, mass, 1.0);
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

    //@Override
    void processCommand(int delay) {
        // Process keys applying to the player
		// Retrieve 
        if (cam != null) {
            // No commands if no draw canvas to retrieve them from!
            Draw dr = cam.getDraw();
            if (dr != null) {

                Vector direction = getFacingVector();
                if (dr.isKeyPressed(KeyEvent.VK_UP))
                {
                    v = v.plus(direction.times(50.0));
                }
                if (dr.isKeyPressed(KeyEvent.VK_DOWN))
                {
                    v = v.plus(direction.times(-50.0));
                }

                //Rotation
                if (dr.isKeyPressed(KeyEvent.VK_RIGHT))
                    yaw -= FOV_INCREMENT;
                if (dr.isKeyPressed(KeyEvent.VK_LEFT))
                    yaw += FOV_INCREMENT;

                yaw = clampYaw(yaw);// -Pi -> Pi

                //Circle for direction
                libs.StdDraw.setPenRadius(0.015);
                libs.StdDraw.setPenColor(Color.RED);
                double rayon = getRadius() * 0.05 * StellarCrush.scale;
                libs.StdDraw.point( r.cartesian(0) + (Math.cos(yaw) * rayon), r.cartesian(1) + (Math.sin(yaw) * rayon));
            }
        }
    }

    public Camera getCam() {
        return cam;
    }

    @Override
    public Vector getLocation() {
        return this.getPosition();
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
}
