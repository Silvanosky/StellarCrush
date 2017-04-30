import java.awt.*;

public interface IViewPort {
// Methods required by an object that can hold a camera
	int getId();
	Vector getLocation(); // location of camera
	Vector getVelocity();
	Vector getFacingVector(); //direction camera is facing in
	double highlightLevel(); // highlight objects below this mass

	double getYaw();
	Color getColor();

	int getScore();
}
