package itdelatrisu.potato.leap;

import itdelatrisu.potato.ui.UI;

import java.awt.Point;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.Vector;

/**
 * Leap Motion listener class.
 */
public class LeapInput extends Listener {
	private final double
		X_LEFT = -80.0, X_RIGHT = 80.0,
		Z_FAR = -40.0, Z_NEAR = 40.0;
	private final double[] Y_HIT = { 105.0, 115.0, 130.0 };
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
	
	private void firePos(boolean leftHand, Point p) {
		for (LeapListener listener : LeapController.getListeners())
			listener.onPos(leftHand, p.x * GRID_SIZE + p.y);
	}

	@Override
	public void onFrame(Controller controller) {
		Frame frame = controller.frame();
		
		int numHands = frame.hands().count();
		if (numHands < 1) { 
			firePos(false, new Point(0, -1));
			firePos(true, new Point(0, -1));
		}
		else if (numHands == 1) {
			Hand hand = frame.hands().get(0);
			firePos(!hand.isLeft(), new Point(0, -1));
		}
		
		for (int i = 0; i < numHands; ++i) {
			Hand hand = frame.hands().get(i);
			Vector curPos = hand.stabilizedPalmPosition();
			double x = curPos.getX(), y = curPos.getY(), z = curPos.getZ();

			int px = (x < X_LEFT) ? 0 : ((x < X_RIGHT) ? 1 : 2);
			int pz = (z < Z_FAR) ? 0 : ((z < Z_NEAR) ? 1 : 2);
			Point hit = new Point(pz, px);

			firePos(hand.isLeft(), hit);

			if (y > Y_TOP) {
				if (hand.isLeft()) { hasHitLeft = false; hitTimeLeft = -1; }
				if (hand.isRight()) { hasHitRight = false; hitTimeRight = -1; }
				continue;
			}

			if (y > Y_HIT[Math.abs(pz - 1) + Math.abs(px - 1)]) {
				if (hitTimeLeft == -1) hitTimeLeft = System.currentTimeMillis();
				if (hitTimeRight == -1) hitTimeRight = System.currentTimeMillis();
			} else {
				long curTime = System.currentTimeMillis();
				if (hand.isLeft() && !hasHitLeft && curTime - hitTimeLeft < HIT_TIME) {
					hasHitLeft = true;
					fireHit(hit);
				} else if (hand.isRight() && !hasHitRight && curTime - hitTimeRight < HIT_TIME) {
					hasHitRight = true;
					fireHit(hit);
				}
			}
		}
	}
}