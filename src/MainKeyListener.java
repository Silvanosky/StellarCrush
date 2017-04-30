import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ╱╲＿＿＿＿＿＿╱╲
 * ▏╭━━╮╭━━╮▕
 * ▏┃＿＿┃┃＿＿┃▕
 * ▏┃＿▉┃┃▉＿┃▕
 * ▏╰━━╯╰━━╯▕
 * ╲╰╰╯╲╱╰╯╯╱  Created by Silvanosky on 30/04/2017
 * ╱╰╯╰╯╰╯╰╯╲
 * ▏▕╰╯╰╯╰╯▏▕
 * ▏▕╯╰╯╰╯╰▏▕
 * ╲╱╲╯╰╯╰╱╲╱
 * ＿＿╱▕▔▔▏╲＿＿
 * ＿＿▔▔＿＿▔▔＿＿
 */
public class MainKeyListener implements libs.DrawListener {

    private AtomicBoolean up = new AtomicBoolean();
    private AtomicBoolean down = new AtomicBoolean();
    private AtomicBoolean left = new AtomicBoolean();
    private AtomicBoolean right = new AtomicBoolean();
    private AtomicBoolean space = new AtomicBoolean();

    private final Object keyLock = new Object();

    public MainKeyListener()
    {

    }

    @Override
    public void mousePressed(double x, double y) {

    }

    @Override
    public void mouseDragged(double x, double y) {

    }

    @Override
    public void mouseReleased(double x, double y) {

    }

    @Override
    public void keyTyped(int keycode) {
    }

    @Override
    synchronized public void keyPressed(int keycode) {

        switch (keycode)
        {
            case KeyEvent.VK_UP:
                up.set(true);
                break;
            case KeyEvent.VK_DOWN:
                down.set(true);
                break;
            case KeyEvent.VK_LEFT:
                left.set(true);
                break;
            case KeyEvent.VK_RIGHT:
                right.set(true);
                break;
            case KeyEvent.VK_SPACE:
                space.set(true);
                break;
            case KeyEvent.VK_M:
            case KeyEvent.VK_ESCAPE:
                StellarCrush.setState(-1);
                System.exit(1);
                break;
            case KeyEvent.VK_P:
                StellarCrush.screen();
                break;
            default:
                if(StellarCrush.getState() == 0)//Start game
                {
                    StellarCrush.setState(1);
                }
                break;
        }

    }

    @Override
    synchronized public void keyReleased(int keycode) {
        switch (keycode)
        {
            case KeyEvent.VK_UP:
                up.set(false);
                break;
            case KeyEvent.VK_DOWN:
                down.set(false);
                break;
            case KeyEvent.VK_LEFT:
                left.set(false);
                break;
            case KeyEvent.VK_RIGHT:
                right.set(false);
                break;
            case KeyEvent.VK_SPACE:
                space.set(false);
                break;
            default:
                break;
        }
    }

    public boolean isUp() {
        return up.get();
    }

    public boolean isDown() {
        return down.get();
    }

    public boolean isLeft() {
        return left.get();
    }

    public boolean isRight() {
        return right.get();
    }

    public boolean isSpace() {
        return space.get();
    }
}
