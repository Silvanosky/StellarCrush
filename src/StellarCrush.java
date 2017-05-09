/* Acknowledgements/notes:
   - Some of this code based on code for Rubrica by Steve Kroon
   - Original inspiration idea for this project was IntelliVision's AstroSmash, hence the name
   */

/* Ideas for extensions/improvements:
PRESENTATION:
-theme your game
-hall of fame/high score screen
-modifiable field of view, rear-view mirror, enhance first-person display by showing extra information on screen
-mouse control
-autoscaling universe to keep all universe objects on screen (or making the edge of the universe repel objects)
-better rendering in camera (better handling of objects on edges, and more accurate location rendering
-improved gameplay graphics, including pictures/sprites/textures for game objects
-add sounds for various game events/music: Warning: adding both sounds and music will likely lead to major
headaches and frustration, due to the way the StdAudio library works.  If you go down this route, you choose
to walk the road alone...
-full 3D graphics with 3D universe (no libraries)

MECHANICS/GAMEPLAY CHANGES:
-avoid certain other game objects rather than/in addition to riding into them
-more interactions - missiles, auras, bombs, explosions, shields, etc.
-more realistic physics for thrusters, inertia, friction, momentum, relativity?
-multiple levels/lives
-energy and hit points/health for game objects and players
-multi-player mode (competitive/collaborative)
-checking for impacts continuously during moves, rather than at end of each time step
-Optimize your code to be able to deal with more objects (e.g. with a quad-tree) - document the improvement you get
--QuadTree implementation with some of what you may want at : http://algs4.cs.princeton.edu/92search/QuadTree.java.html
--https://github.com/phishman3579/java-algorithms-implementation/blob/master/src/com/jwetherell/algorithms/data_structures/QuadTree.java may also be useful - look at the Point Region Quadtree
*/

import libs.Draw;

import java.awt.*;
import java.io.File;
import java.util.PriorityQueue;

public class StellarCrush {
	// Main game class

	// CONSTANTS TUNED FOR GAMEPLAY EXPERIENCE
	private static final int GAME_DELAY_TIME = 5000; // in-game time units between frame updates
	private static final int TIME_PER_MS = 1000; // how long in-game time corresponds to a real-time millisecond
	static final double G = 6.67e-11; // gravitational constant
	static final double softE = 0.001; // softening factor to avoid division by zero calculating force for co-located objects
	static final double scale = 5e10; // plotted universe size

    private static long FPS = 0;

    private static Draw dr;
    private static MainKeyListener listener;

    //0: menu, 1: game, 2: game over; -1: stop all
    private static int state = 0;

    private static PriorityQueue<Runnable> pool = new PriorityQueue<>();

    private StellarCrush()
    {

    }

    public static void screen()
    {
        pool.add(() -> {
            new File("screenshots").mkdir();
            dr.save("screenshots/" + Long.toString(System.currentTimeMillis()) + ".png");
        });
    }

    private static void execPool()
    {
        while (!pool.isEmpty())
        {
            pool.poll().run();
        }
    }

	private static void menu()
	{
        dr.setXscale(0.0, 100.0);
        dr.setYscale(0.0, 100.0);

		Font font = new Font("Arial", Font.BOLD, 45);
        dr.setFont(font);
        dr.text(50.0, 90.0, "Stellar Crush");
        dr.setFont();
        dr.text(50.0, 80.0, "Press any Key to start!");

        dr.text(50.0, 45.0, "Arrows to rotate left or right, accelerate or decelerate");

        dr.text(50.0, 35.0, "You are lost in the Galaxy without fuel ! Destroy objects to make fuel !");

        dr.text(50.0, 30.0, "But caution ! If an object touch you from behind you will lose fuel !");

        dr.text(50.0, 20.0, "Quit (m). Screencap (p)");

        dr.changeWindowTitle("StellarCrush");

		try{
            while(state == 0) {
                execPool();
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            dr.closeWindow();
        }
	}

	private static void gameOver(boolean win)
    {
        dr.setXscale(0.0, 100.0);
        dr.setYscale(0.0, 100.0);

        Font font = new Font("Arial", Font.BOLD, 45);
        dr.setFont(font);
        dr.text(50.0, 90.0, "Stellar Crush");
        dr.setFont();
        if(win)
        {
            dr.text(50.0, 80.0, "Congratulation :D !");

            dr.text(50.0, 45.0, "You won the game !");
        }else{
            dr.text(50.0, 80.0, "Sorry :( !");

            dr.text(50.0, 45.0, "You lost the game !");
        }

        dr.text(50.0, 20.0, "Quit (m). Screencap (p)");

        dr.changeWindowTitle("StellarCrush");

        try{
            while(state == 2) {
                execPool();
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            dr.closeWindow();
        }
    }

	private static long clamp(long value)
    {
        if(value < 0)
            return 0;
        return value;
    }

    public static Draw createDraw()
    {
        Draw dr = new Draw(true);
        dr.addListener(listener);
        return dr;
    }

	public static void main(String[] args) {

        Rectangle screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();

        listener = new MainKeyListener();

        while (state != -1)
        {
            dr = createDraw();
            dr.setCanvasSize((int) (width * 0.55), (int) height);

            menu();
            dr = createDraw();

            dr.setCanvasSize((int) (width * 0.55), (int) height);
            GameState gameState = new GameState(GameObjectLibrary.createPlayerObject(), scale);
            long time = System.currentTimeMillis();
            long frame = 0;
            long lastFrame = System.currentTimeMillis();
            while (state == 1) // MAIN LOOP
            {
                long currentTime = System.currentTimeMillis();
                dr.clear();
                int delay = (int) ((currentTime - lastFrame) * TIME_PER_MS);
                //System.out.println("delay: " + delay);
                gameState.update(delay);
                gameState.draw();
                dr.show();
                execPool();
                try {
                    //Sleep for the next frame
                    Thread.sleep(GAME_DELAY_TIME  / TIME_PER_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    //Interrupted so return
                    state = -1;
                }
                dr.enableDoubleBuffering();
                frame++;

                if(System.currentTimeMillis() - time > 1000)
                {
                    FPS = frame;
                    frame = 0;
                    time = System.currentTimeMillis();
                }
                lastFrame = currentTime;
            }
            gameState.close();
            dr.closeWindow();

            dr = createDraw();
            dr.setCanvasSize((int) (width * 0.55), (int) height);

            gameOver(gameState.getPlayer().getScore() >= 100);
        }
	}

    public static long getFPS() {
        return FPS;
    }

    public static Draw getDraw() {
        return dr;
    }

    public static int getState() {
        return state;
    }

    public static void setState(int state) {
        StellarCrush.state = state;
    }

    public static MainKeyListener getListener() {
        return listener;
    }
}
