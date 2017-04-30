import libs.Draw;
import libs.Entry;
import libs.RootMath;

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

        dr = StellarCrush.createDraw();
        dr.setCanvasSize(780, 1024);
        double ratio = (double) dr.getWidth() / dr.getHeight();
        dr.setXscale(100, 0);
        dr.setYscale(100.0/ratio, 0);

        //dr.addListener(StellarCrush.getDraw());
        Point location = StellarCrush.getDraw().getLocationOnScreen();
        setFOV(FOV);
        dr.setLocationOnScreen(location.x + StellarCrush.getDraw().getWidth(), location.y);
        //dr.setLocationOnScreen(location.x, location.y + StellarCrush.getDraw().getHeight() + 35);
        dr.toFocus();
    }

    public void setFOV(double FOV)
    {
        this.FOV = FOV;
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
            double angle = clampYaw(Math.atan2(deltaY, deltaX) - holder.getYaw());

            if (Math.abs(angle) > FOV/2.0 + Math.PI / 12.0) {
                continue;//Object out of view don't show it
            }
            final double dist = RootMath.sqrtApprox((float) (deltaX * deltaX + deltaY * deltaY)) ;
            renderer.add(new Entry<>(() -> {
                double finalDist = dist;
                double radius =(GameObject.SIZE * obj.getRadius()) / finalDist;
                double posx = Math.sin(angle);
                double posy = Math.abs(dr.getYmax() - dr.getYmin())/2.0;
                dr.setPenColor(Color.RED);
                dr.setPenRadius(radius * 1.01);
                dr.point(scaleX(posx), posy);

                dr.setPenColor(obj.getColor());
                dr.setPenRadius(radius);
                dr.point(scaleX(posx), posy);

            }, dist));
        }

        while (!renderer.isEmpty())
        {
            renderer.poll().getKey().run();
        }

        showHUD();

        dr.show();
        dr.enableDoubleBuffering();
	}

	private void showHUD()
    {
        dr.setPenColor(Color.black);
        dr.text(90, scaleY(99), "FPS: " + StellarCrush.getFPS());

        // orientation
        double ox = 50.0;
        double oy = scaleY(90.0);

        double yaw = holder.getYaw() + Math.PI;
        double v = -holder.getVelocity().cartesian(1);
        if(Math.abs(v) < 0.1)
        {
            v = 0.0;
        }
        double v1 = -holder.getVelocity().cartesian(0);
        if(Math.abs(v1) < 0.1) {
            v1 = 0.0;
        }
        double velocity = Math.atan2(v, v1);
        drawTriangle(dr, Color.BLACK, ox, oy, 3, 2, Math.PI/6, velocity);

        drawTriangle(dr, Color.RED, ox, oy, 3, 2, Math.PI/6, yaw);

        dr.setPenColor(holder.getColor());
        dr.setPenRadius(0.1);
        dr.point(ox, oy);

        dr.setPenColor(Color.BLACK);
        dr.text(50.0, scaleY(85), "SpeedY: " +  String.format("%.0f", -v));
        dr.text(65.0, scaleY(90), "SpeedX: " +  String.format("%.0f", -v1));

    }

    public Draw getDraw() {
        return dr;
    }

    private void drawTriangle(Draw dr, Color color,  double ox, double oy, double rayon, double length, double width, double yaw)
    {
        dr.setPenColor(color);
        double[] x = new double[3];//Not the choice with stddraw so need to use array
        double[] y = new double[3];

        x[0] = ox + (Math.cos(yaw - width) * rayon);
        y[0] = oy + (Math.sin(yaw - width) * rayon);

        x[1] = ox + (Math.cos(yaw + width) * rayon);
        y[1] = oy + (Math.sin(yaw + width) * rayon);

        x[2] = ox + (Math.cos(yaw) * (rayon + length));
        y[2] = oy + (Math.sin(yaw) * (rayon + length));
        dr.filledPolygon(x, y);
    }

    private double  scaleX(double x) { return 100  * (x + FOV/2.0) / FOV; }
    private double  scaleY(double y) { return dr.getYmin() * y / 100; }
}
