package itdelatrisu.potato.ui;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.util.Log;

import itdelatrisu.potato.ErrorHandler;
import itdelatrisu.potato.GameImage;
import itdelatrisu.potato.leap.LeapController;
import itdelatrisu.potato.leap.LeapListener;
import itdelatrisu.potato.ui.animations.AnimatedValue;
import itdelatrisu.potato.ui.animations.AnimationEquation;

/**
 * Leap Motion gamepad.
 */
public class Gamepad implements LeapListener {
	/** Number of gamepad positions. */
	private static final int GAMEPAD_BUTTONS = 9;

	/** Time to fade out hits, in ms. */
	private static final int HIT_FADEOUT_TIME = 750;

	/** Time to fade out map hit objects, in ms. */
	private static final int HIT_OBJECT_FADEOUT_TIME = 400;

	/** Gamepad hit images. */
	private Image[] hitImages;
	
	/** Hand position images. */
	private Image[] handPosImages;

	/** Alpha values of hits for each gamepad position. */
	private AnimatedValue[] hitValues;

	/** Gamepad map object images. */
	private Image[] mapObjectImages;

	/** Alpha values of map hit objects for each gamepad position. */
	private AnimatedValue[] mapObjectValues;

	/** Whether the map object is fading in at each gamepad position. */
	private boolean[] mapObjectFadingIn;
	
	/** Current location of the user's hand */
	private int leftHandPos = -1;
	private int rightHandPos = -1;

	/**
	 * Constructor.
	 */
	public Gamepad() {
		hitImages = new Image[GAMEPAD_BUTTONS];
		hitValues = new AnimatedValue[GAMEPAD_BUTTONS];
		handPosImages = new Image[GAMEPAD_BUTTONS * 2];
		mapObjectImages = new Image[GAMEPAD_BUTTONS];
		mapObjectValues = new AnimatedValue[GAMEPAD_BUTTONS];
		mapObjectFadingIn = new boolean[GAMEPAD_BUTTONS];
		for (int i = 0; i < GAMEPAD_BUTTONS; i++) {
			hitImages[i] = GameImage.valueOf(String.format("GAMEPAD_%d", i)).getImage().copy();
			hitValues[i] = new AnimatedValue(HIT_FADEOUT_TIME, 1f, 0f, AnimationEquation.OUT_CUBIC);
			mapObjectImages[i] = GameImage.valueOf(String.format("GAMEPAD_MAP_%d", i)).getImage().copy();
			mapObjectValues[i] = new AnimatedValue(1, 0f, 0f, AnimationEquation.LINEAR);  // dummy
			handPosImages[i] = GameImage.valueOf(String.format("GAMEPAD_LEFT_%d", i)).getImage().copy();
			handPosImages[i+GAMEPAD_BUTTONS] = GameImage.valueOf(String.format("GAMEPAD_RIGHT_%d", i)).getImage().copy();
		}

		LeapController.addListener(this);
	}

	/**
	 * Draws the gamepad.
	 * @param g the graphics context
	 */
	public void draw(Graphics g) {
		Image bg = GameImage.BACKGROUND.getImage();
		bg.draw();
		for (int i = 0; i < GAMEPAD_BUTTONS; i++) {
			float mapObjectAlpha = mapObjectValues[i].getValue();
			if (mapObjectAlpha != 0f) {
				mapObjectImages[i].setAlpha(mapObjectAlpha);
				mapObjectImages[i].draw();
			}
			float hitAlpha = hitValues[i].getValue();
			if (hitAlpha != 0f) {
				hitImages[i].setAlpha(hitAlpha);
				hitImages[i].draw();
			}
		}
		Image gamepad = GameImage.GAMEPAD.getImage();
		gamepad.draw();
		
		if (leftHandPos > -1)
			handPosImages[leftHandPos].draw();
		if (rightHandPos > -1)
			handPosImages[rightHandPos].draw();
	}

	/**
	 * Updates the gamepad by a delta interval.
	 * @param delta the delta interval since the last call
	 */
	public void update(int delta) {
		for (int i = 0; i < GAMEPAD_BUTTONS; i++) {
			hitValues[i].update(delta);
			mapObjectValues[i].update(delta);
			if (mapObjectValues[i].getValue() == 1f) {
				// start fading out
				mapObjectValues[i] = new AnimatedValue(HIT_OBJECT_FADEOUT_TIME, 1f, 0f, AnimationEquation.OUT_CUBIC);
				mapObjectFadingIn[i] = false;
			}
		}
	}

	/**
	 * Checks if the given position is in bounds, and throws an error if it is not.
	 * @param pos the gamepad position
	 */
	private void checkBounds(int pos) {
		if (pos < 0 || pos >= GAMEPAD_BUTTONS)
			ErrorHandler.error(String.format("Send gamepad hit out of bounds: %d", pos), null, false);
	}

	/**
	 * Sends a hit at the given position.
	 * @param pos the gamepad position
	 * @param isMapObjectHit if this hit a map hit object
	 */
	public void sendHit(int pos, boolean isMapObjectHit) {
		checkBounds(pos);
		hitValues[pos].setTime(0);

		// start fading out any map hit object immediately
		if (isMapObjectHit && mapObjectFadingIn[pos]) {
			mapObjectValues[pos] = new AnimatedValue(HIT_OBJECT_FADEOUT_TIME, 1f, 0f, AnimationEquation.OUT_CUBIC);
			mapObjectFadingIn[pos] = false;
		}
	}

	/**
	 * Sends a map hit object to fade in over the given duration.
	 * @param pos the gamepad position
	 * @param fadeIn the fade-in time (not including the fade-out afterwards)
	 */
	public void sendMapObject(int pos, int fadeIn) {
		checkBounds(pos);
		mapObjectValues[pos] = new AnimatedValue(fadeIn, 0f, 1f, AnimationEquation.LINEAR);
	}

	/**
	 * Resets all gamepad values.
	 */
	public void reset() {
		for (int i = 0; i < GAMEPAD_BUTTONS; i++) {
			hitValues[i].setTime(HIT_FADEOUT_TIME);
			mapObjectValues[i] = new AnimatedValue(1, 0f, 0f, AnimationEquation.LINEAR);  // dummy
			mapObjectFadingIn[i] = false;
		}
	}
	
	@Override
	public void onConnect() {
		Log.info("Connected Leap Motion controller.");
	}

	@Override
	public void onDisconnect() {
		Log.error("Leap Motion controller has disconnected.");
	}

	@Override
	public void onHit(int pos) {}
	
	@Override
	public void onPos(boolean leftHand, int pos) {
		if (leftHand)
			this.leftHandPos = pos;
		else
			this.rightHandPos = pos;
	}
}
