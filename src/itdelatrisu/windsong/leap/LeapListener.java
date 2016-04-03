package itdelatrisu.windsong.leap;

/**
 * Interface for Leap Motion events.
 */
public interface LeapListener {
	/** Notification that the Leap Motion controller has connected to the device. */
	public void onConnect();

	/** Notification that the Leap Motion controller has disconnected from the device. */
	public void onDisconnect();

	/** Notification that a hit has occurred at the given position. */
	public void onHit(int pos);
}
