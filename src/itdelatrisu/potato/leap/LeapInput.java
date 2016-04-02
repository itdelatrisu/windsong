package itdelatrisu.potato.leap;

import java.awt.Point;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.Vector;

import itdelatrisu.potato.Utils;

/**
 * Leap Motion listener class.
 */
public class LeapInput extends Listener {
	private final double
		X_MIN = -250.0, X_MAX = 250.0,
		Z_MIN = -120.0, Z_MAX = 130.0;
	private final double[] Y_HIT = { 100.0, 110.0, 120.0 };
	private final double Y_TOP = 150.0;
	private final long HIT_TIME = 150;
	private final int GRID_SIZE = 3;

	private boolean hasHitLeft = false, hasHitRight = false;
	private long hitTimeLeft = -1, hitTimeRight = -1;

	@Override
	public void onConnect(Controller controller) {
		// prevent initial hand position from triggering a hit
		Frame frame = controller.frame();
		for (int i = 0, numHands = frame.hands().count(); i < numHands; ++i) {
			Hand hand = frame.hands().get(i);
			double y = hand.stabilizedPalmPosition().getY();
			if (hand.isLeft()) hasHitLeft = y <= Y_TOP;
			if (hand.isRight()) hasHitRight = y <= Y_TOP;
		}

		// fire listeners
		for (LeapListener listener : LeapController.getListeners())
			listener.onConnect();
	}

	@Override
	public void onDisconnect(Controller controller) {
		// fire listeners
		for (LeapListener listener : LeapController.getListeners())
			listener.onDisconnect();
	}

	/** Fires listeners for a hit at the given point. */
	private void fireHit(Point p) {
		for (LeapListener listener : LeapController.getListeners())
			listener.onHit(p.x * GRID_SIZE + p.y);
	}

	@Override
	public void onFrame(Controller controller) {
		Frame frame = controller.frame();
		for (int i = 0, numHands = frame.hands().count(); i < numHands; ++i) {
			Hand hand = frame.hands().get(i);
			Vector curPos = hand.stabilizedPalmPosition();
			double x = curPos.getX(), y = curPos.getY(), z = curPos.getZ();

			if (y > Y_TOP) {
				if (hand.isLeft()) { hasHitLeft = false; hitTimeLeft = -1; }
				if (hand.isRight()) { hasHitRight = false; hitTimeRight = -1; }
				continue;
			}

			int px = Utils.clamp((int) (1.0 * GRID_SIZE * (x - X_MIN) / (X_MAX - X_MIN)), 0, GRID_SIZE - 1);
			int pz = Utils.clamp((int) (1.0 * GRID_SIZE * (z - Z_MIN) / (Z_MAX - Z_MIN)), 0, GRID_SIZE - 1);
			Point hit = new Point(pz, px);
			if (y > Y_HIT[Math.abs(pz - 1) + Math.abs(px - 1)]) {
				if (hitTimeLeft == -1) hitTimeLeft = System.currentTimeMillis();
				if (hitTimeRight == -1) hitTimeRight = System.currentTimeMillis();
			} else {
				long curTime = System.currentTimeMillis();
				if (hand.isLeft() && !hasHitLeft && curTime - hitTimeLeft < HIT_TIME) {
					System.out.println("L " + hit.x + " " + hit.y);
					hasHitLeft = true;
					fireHit(hit);
				} else if (hand.isRight() && !hasHitRight && curTime - hitTimeRight < HIT_TIME) {
					System.out.println("R " + hit.x + " " + hit.y);
					hasHitRight = true;
					fireHit(hit);
				}
			}
		}
	}
}