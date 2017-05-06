import libs.Draw;
import libs.IEntry;
import libs.RootMath;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
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

        Rectangle screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();

        dr = StellarCrush.createDraw();
        dr.setCanvasSize((int) (width * 0.45), (int) height);
        double ratio = (double) dr.getWidth() / dr.getHeight();
        dr.setXscale(100, 0);
        dr.setYscale(100.0/ratio, 0);

        Point location = StellarCrush.getDraw().getLocationOnScreen();
        setFOV(FOV);
        dr.setLocationOnScreen(location.x + StellarCrush.getDraw().getWidth(), location.y);
        dr.toFocus();
    }

	void render(Collection<GameObject> objects) {
        dr.clear();

        //Use this queue to first
        PriorityQueue<IEntry<Runnable, Double>> renderer = new PriorityQueue<>((o1, o2) -> -o1.getValue().compareTo(o2.getValue()));

        for(GameObject obj : objects) {
            if(holder.getId() == obj.getId())
                continue;

            Vector position = obj.getLocation();
            Vector location = holder.getLocation();

            double deltaX = (position.cartesian(0) - location.cartesian(0)) / StellarCrush.scale;
            double deltaY = (position.cartesian(1) - location.cartesian(1)) / StellarCrush.scale;
            double angle = clampYaw(Math.atan2(deltaY, deltaX) - holder.getYaw());

            if (Math.abs(angle) < FOV/2.0 + Math.PI / 12.0) { // Don't compute out of range objects
                final double dist = RootMath.sqrtApprox((float) (deltaX * deltaX + deltaY * deltaY)) ;
                renderer.add(new IEntry<>(() -> {
                    double radius =(GameObject.SIZE * obj.getRadius()) / dist;
                    //double posX = Math.sin(angle * Math.PI / FOV); // Non linear
                    double posX = angle; // Non linear
                    double posY = Math.abs(dr.getYmax() - dr.getYmin())/2.0;
                    dr.setPenColor(Color.RED);
                    dr.setPenRadius(radius * 1.01);
                    dr.point(scaleX(posX), posY);

                    dr.setPenColor(obj.getColor());
                    dr.setPenRadius(radius);
                    dr.point(scaleX(posX), posY);
                }, dist));
            }
        }

        while (!renderer.isEmpty())
        {
            renderer.poll().getKey().run();
        }

        showHUD();

        dr.show();
        dr.enableDoubleBuffering();
	}

	//Fade red to yellow to green
	private Color prcToRGB(double prc)
    {
        double x = prc;
        if (x >= 100) {
            x = 99.0;
        }
        if(x <= 1)
            x = 1.0;
        float red   = (float) (x > 50 ? 1 - 2 * (x - 50) / 100.0 : 1.0);
        float green = (float) (x > 50 ? 1.0 : 2 * x / 100.0);
        float blue  = 0.0f;
        return new Color(red, green, blue);
    }

	private void showHUD()
    {
        dr.setPenColor(Color.black);
        dr.text(90, scaleY(99), "FPS: " + StellarCrush.getFPS());

        // orientation
        double ox = 50.0;
        double oy = scaleY(95.0);

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
        drawTriangle(dr, Color.BLACK, ox, oy, velocity);

        drawTriangle(dr, Color.RED, ox, oy, yaw);

        dr.setPenColor(holder.getColor());
        dr.setPenRadius(0.1);
        dr.point(ox, oy);

        dr.setPenColor(Color.BLACK);
        dr.text(50.0, scaleY(90), "SpeedY: " +  String.format("%.0f", -v));
        dr.text(65.0, scaleY(95), "SpeedX: " +  String.format("%.0f", -v1));

        dr.text(85, scaleY(84.5), "Fuel: ");

        dr.setPenRadius(0.01);
        Color color = prcToRGB(holder.getScore());

        double prc = holder.getScore() * (6.0 / 10.0);
        drawRectangle(dr, color, 80.0, 83.0, prc, 3.0);
        //Border
        drawRectangle(dr, Color.BLACK, 80.0, 83.0, 60.0, 3.0, false);
    }

    private void drawTriangle(Draw dr, Color color,  double ox, double oy, double yaw) {
        double rayon = 3.0;
        double length = 2.0;
        double width = Math.PI / 6.0;

        dr.setPenColor(color);

        Collection<Map.Entry<Double, Double>> points = new ArrayList<>();

        points.add(new IEntry<>(
                ox + (Math.cos(yaw - width) * rayon),
                oy + (Math.sin(yaw - width) * rayon)
        ));
        points.add(new IEntry<>(
                ox + (Math.cos(yaw + width) * rayon),
                oy + (Math.sin(yaw + width) * rayon)
        ));

        points.add(new IEntry<>(
                ox + (Math.cos(yaw) * (rayon + length)),
                oy + (Math.sin(yaw) * (rayon + length))
        ));

        dr.filledPolygon(points);
    }

    private void drawRectangle(Draw dr, Color color, double ox, double oy, double width, double height)
    {
        drawRectangle(dr, color, ox, oy, width, height, true);
    }

    private void drawRectangle(Draw dr, Color color, double ox, double oy, double width, double height, boolean filled)
    {
        dr.setPenColor(color);

        Collection<Map.Entry<Double, Double>> points = new ArrayList<>();

        points.add(new IEntry<>(
                ox,
                scaleY(oy)
        ));
        points.add(new IEntry<>(
                ox - width,
                scaleY(oy)
        ));

        points.add(new IEntry<>(
                ox - width,
                scaleY(oy + height)
        ));

        points.add(new IEntry<>(
                ox,
                scaleY(oy + height)
        ));
        if(filled)
            dr.filledPolygon(points);
        else
            dr.polygon(points);
    }

    //Utils
    private double  scaleX(double x) { return 100  * (x + FOV/2.0) / FOV; }
    //private double  scaleX(double x) { return 100  * (x + 1.0) / 2.0; }
    private double  scaleY(double y) { return dr.getYmin() * y / 100; }

    private double clampYaw(double yaw)
    {
        double nam = yaw;
        if(yaw > Math.PI)
            nam += -Math.PI * 2.0;
        if(yaw < -Math.PI)
            nam += Math.PI * 2.0;
        return nam;
    }

    //Getters/Setters
    public void setFOV(double FOV)
    {
        this.FOV = FOV;
    }

    public Draw getDraw() {
        return dr;
    }

    public void close() {
        dr.closeWindow();
    }
}
