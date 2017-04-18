import java.awt.*;

public class PlayerObject extends GameObject implements IViewPort {

    private static final Color DEFAULT_COLOR = StdDraw.WHITE;
    private static final Color DEFAULT_FACING_COLOR = StdDraw.BLACK;
    private static final double DEFAULT_FOV = Math.PI/2; // field of view of player's viewport
    private static final double FOV_INCREMENT = Math.PI/36; // rotation speed

    private Camera cam;

    public PlayerObject(Vector r, Vector v, double mass) {
        super(-1, r, v, mass);
    }

    //@Override
    void processCommand(int delay) {
        // Process keys applying to the player
		// Retrieve 
        if (cam != null) {
            // No commands if no draw canvas to retrieve them from!
            Draw dr = cam.getDraw();
            if (dr != null) {
				// Example code
                /*if (dr.isKeyPressed(KeyEvent.VK_UP)) up = true;
                if (dr.isKeyPressed(KeyEvent.VK_DOWN)) down = true;*/
            }
        }
    }

    @Override
    public Vector getLocation() {
        return null;
    }

    @Override
    public Vector getFacingVector() {
        return null;
    }

    @Override
    public double highlightLevel() {
        return 0;
    }
}
