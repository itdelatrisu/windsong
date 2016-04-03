package itdelatrisu.windsong.leap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.leapmotion.leap.Controller;

/**
 * Leap Motion controller class.
 */
public class LeapController {
	/** The game listeners. */
	private static List<LeapListener> gameListeners = new ArrayList<LeapListener>();

	// leap controller classes
	private static Controller controller;
	private static LeapInput listener;

	// This class should not be instantiated.
	private LeapController() {}

	/**
	 * Connects to the Leap Motion controller and starts listening for events.
	 */
	public static void init() {
		controller = new Controller();
		listener = new LeapInput();
		controller.addListener(listener);

		// continue listening...
		try {
			System.in.read();
		} catch (IOException e) {}
		close();
	}

	/**
	 * Cleans up resources.
	 */
	public static void close() {
		if (controller != null)
			controller.removeListener(listener);
	}

	/**
	 * Adds a listener to the Leap Motion controller.
	 * @param listener the listener
	 */
	public static void addListener(LeapListener listener) { gameListeners.add(listener); }

	/**
	 * Returns all registered listeners.
	 * @return the list of registerd listeners
	 */
	public static List<LeapListener> getListeners() { return gameListeners; }
}
