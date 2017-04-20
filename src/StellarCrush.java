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

import libs.StdDraw;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;

public class StellarCrush {
	// Main game class

	// CONSTANTS TUNED FOR GAMEPLAY EXPERIENCE
	static final int GAME_DELAY_TIME = 5000; // in-game time units between frame updates
	static final int TIME_PER_MS = 1000; // how long in-game time corresponds to a real-time millisecond
	static final double G = 6.67e-11; // gravitational constant
	static final double softE = 0.001; // softening factor to avoid division by zero calculating force for co-located objects
	static double scale = 5e10; // plotted universe size

    private static void screen()
    {
        new File("screenshots").mkdir();
        StdDraw.save("screenshots/" + Long.toString(System.currentTimeMillis()) + ".png");
    }

	private static boolean menu()
	{
		StdDraw.setCanvasSize();
		StdDraw.setXscale(0.0, 100.0);
		StdDraw.setYscale(0.0, 100.0);

		Font font = new Font("Arial", Font.BOLD, 45); 
		StdDraw.setFont(font);
		StdDraw.text(50.0, 90.0, "Stellar Crush");
		StdDraw.setFont();
		StdDraw.text(50.0, 80.0, "Press any Key to start!");

		StdDraw.text(50.0, 45.0, "Arrows to rotate left or roght, accelerate or decelerate");

		StdDraw.text(50.0, 35.0, "You are borg. Assimilate all who stand against you!");

		libs.StdDraw.text(50.0, 20.0, "Quit (m). Screencap (p)");
		
		StdDraw.changeWindowTitle("StellarCrush");

		try{
            while(true) {
                //Key handle
                if (StdDraw.isKeyPressed(KeyEvent.VK_M)) {
                    if(StdDraw.hasNextKeyTyped()) StdDraw.nextKeyTyped();
                    return false;
                } else if (StdDraw.isKeyPressed(KeyEvent.VK_P)) {
                    if(StdDraw.hasNextKeyTyped()) StdDraw.nextKeyTyped();
                    screen();
                } else if (StdDraw.hasNextKeyTyped()) {
                    StdDraw.nextKeyTyped();    //Process key to avoid infinite loop
                    StdDraw.closeWindow();
                    return true;
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            StdDraw.closeWindow();
        }
        return false;
	}

	public static void main(String[] args) {
        if(!menu()) //
        {
            return;
        }
        StdDraw.setCanvasSize();
        GameState gameState = new GameState(GameObjectLibrary.createPlayerObject(), scale);
        boolean run = true;
        while (run) // MAIN LOOP
        {
            long startTime = System.currentTimeMillis();
            if (StdDraw.isKeyPressed(KeyEvent.VK_M)) {
                if(StdDraw.hasNextKeyTyped()) StdDraw.nextKeyTyped();
                run = false;
            } else if (StdDraw.isKeyPressed(KeyEvent.VK_P)) {
                if(StdDraw.hasNextKeyTyped()) StdDraw.nextKeyTyped();
                screen();
            }


            long currentTime = System.currentTimeMillis();
            StdDraw.clear();
            gameState.update((int) ((currentTime - startTime) * TIME_PER_MS + GAME_DELAY_TIME));
            gameState.draw();
            StdDraw.show();
            try {
                //Sleep for the next frame
                Thread.sleep(GAME_DELAY_TIME / TIME_PER_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                //Interrupted so return
                run = false;
            }
            StdDraw.enableDoubleBuffering();
        }
        StdDraw.closeWindow();
	}

}
