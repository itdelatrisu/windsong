package itdelatrisu.potato.leap;

/**
 * Interface for Leap Motion events.
 */
public interface LeapListener {
	/** Notification that the Leap Motion controller has connected to the device. */
	public void onConnect();

	/** Notification that the Leap Motion controller has disconnected from the device. */
	public void onDisconnect();

	/** Notification that the hand is at a current position */
	public void onPos(boolean leftHand, int pos);
	
	/** Notification that a hit has occurred at the given position for the given hand ( 0 left, 1 right) */
	public void onHit(int pos);
}
