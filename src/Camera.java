import libs.StdDraw;
import libs.Draw;

import java.awt.*;
import java.util.Collection;

public class Camera {
    // Virtual camera - uses a plane one unit away from the focal point
    // For ease of use, this simply locates where the centre of the object is, and renders it if that is in the field of view.
	// Further, the correct rendering is approximated by a circle centred on the projected centre point.

    private final IViewPort holder; // Object from whose perspective the first-person view is drawn
    private final Draw dr; // Canvas on which to draw
    private double FOV; // field of view of camera



    Camera(IViewPort holder, double FOV) {
        // Constructs a camera with field of view FOV, held by holder, and rendered on canvas dr.
        this.holder = holder;

        dr = new Draw();
        Point location = StdDraw.getLocationOnScreen();

        setFOV(FOV);

        dr.setLocationOnScreen(location.x + StdDraw.getWidth()+ 11, location.y);
        dr.toFocus();
    }

    public void setFOV(double FOV)
    {
        this.FOV = FOV;
        dr.setXscale(FOV/2.0, -FOV/2.0);
        dr.setYscale(-1.0, 1.0);
    }

	void render(Collection<GameObject> objects) {
        dr.clear();
        for(GameObject obj : objects) {
            if(holder.getId() == obj.getId())
                continue;

            Vector position = obj.getPosition();
            Vector location = holder.getLocation();

            double deltaX = position.cartesian(0) - location.cartesian(0);
            double deltaY = position.cartesian(1) - location.cartesian(1);
            double angle = Math.atan2(deltaY, deltaX) - holder.getYaw();

            if (Math.abs(angle) > FOV/2.0) {
                continue;//Object out of view don't show it
            }
            double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY) / StellarCrush.scale;
            if(dist * dist < 0.5)
                dist = 1.0;

            dr.setPenRadius(0.2 * obj.getRadius() * (1/dist));
            dr.setPenColor(obj.getColor());
            dr.point(angle, 0.0);
        }
        dr.show();
        dr.enableDoubleBuffering();

	}

    public Draw getDraw() {
        return dr;
    }
}
