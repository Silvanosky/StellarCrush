import java.awt.event.KeyEvent;

public class MainKeyListener implements libs.DrawListener {

    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;
    private boolean space;

    public MainKeyListener()
    {
        //Empty constructor
    }

    @Override
    public void mousePressed(double x, double y) {
        //not used
    }

    @Override
    public void mouseDragged(double x, double y) {
        //not used
    }

    @Override
    public void mouseReleased(double x, double y) {
        //not used
    }

    @Override
    public void keyTyped(int keycode) {
        //not used
    }

    @Override
    synchronized public void keyPressed(int keycode) {

        switch (keycode)
        {
            case KeyEvent.VK_UP:
                up = true;
                break;
            case KeyEvent.VK_DOWN:
                down = true;
                break;
            case KeyEvent.VK_LEFT:
                left = true;
                break;
            case KeyEvent.VK_RIGHT:
                right = true;
                break;
            case KeyEvent.VK_SPACE:
                space = true;
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
                up = false;
                break;
            case KeyEvent.VK_DOWN:
                down = false;
                break;
            case KeyEvent.VK_LEFT:
                left = false;
                break;
            case KeyEvent.VK_RIGHT:
                right = false;
                break;
            case KeyEvent.VK_SPACE:
                space = false;
                break;
            default:
                break;
        }
    }

    public boolean isUp() {
        return up;
    }

    public boolean isDown() {
        return down;
    }

    public boolean isLeft() {
        return left;
    }

    public boolean isRight() {
        return right;
    }

    public boolean isSpace() {
        return space;
    }
}
