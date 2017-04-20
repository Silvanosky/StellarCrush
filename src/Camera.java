import libs.Draw;
import libs.Entry;
import libs.StdDraw;

import java.awt.*;
import java.util.Collection;
import java.util.PriorityQueue;

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

        //Use this queue to first
        PriorityQueue<Entry<Runnable, Double>> renderer = new PriorityQueue<>((o1, o2) -> -o1.getValue().compareTo(o2.getValue()));

        for(GameObject obj : objects) {
            if(holder.getId() == obj.getId())
                continue;

            Vector position = obj.getPosition();
            Vector location = holder.getLocation();

            double deltaX = (position.cartesian(0) - location.cartesian(0)) / StellarCrush.scale;
            double deltaY = (position.cartesian(1) - location.cartesian(1)) / StellarCrush.scale;
            double angle = Math.atan2(deltaY, deltaX) - holder.getYaw();

            if (Math.abs(angle) > FOV/2.0) {
                continue;//Object out of view don't show it
            }
            final double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY) ;
            renderer.add(new Entry<>(() -> {
                double finalDist = dist;
                if(finalDist * finalDist < 0.0001)
                    finalDist = 0.0001;

                dr.setPenColor(obj.getColor());
                dr.setPenRadius((GameObject.SIZE * obj.getRadius()) / finalDist);
                dr.point(Math.sin(angle), 0.0);

            }, dist));
        }

        while (!renderer.isEmpty())
        {
            renderer.poll().getKey().run();
        }
        dr.show();
        dr.enableDoubleBuffering();

	}

    public Draw getDraw() {
        return dr;
    }
}
