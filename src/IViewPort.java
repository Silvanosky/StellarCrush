public interface IViewPort {
// Methods required by an object that can hold a camera

	Vector getLocation(); // location of camera
	Vector getFacingVector(); //direction camera is facing in
	double highlightLevel(); // highlight objects below this mass
}
