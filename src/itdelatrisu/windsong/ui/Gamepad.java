package itdelatrisu.windsong.ui;

import itdelatrisu.windsong.ErrorHandler;
import itdelatrisu.windsong.GameImage;
import itdelatrisu.windsong.leap.LeapController;
import itdelatrisu.windsong.leap.LeapListener;
import itdelatrisu.windsong.ui.animations.AnimatedValue;
import itdelatrisu.windsong.ui.animations.AnimationEquation;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.util.Log;

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
	
	/** Time to fade in / out glow, in ms. */
	private static final int GLOW_FADE_TIME = 500;

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
	
	/** Alpha values of hand position images for each gamepad position. */
	private AnimatedValue[] handPosValues;
	
	/** Whether the hand position image has already begun fading out. */
	private boolean[] handPosFadingOut;

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
		handPosImages = new Image[GAMEPAD_BUTTONS];
		handPosValues = new AnimatedValue[GAMEPAD_BUTTONS];
		handPosFadingOut = new boolean[GAMEPAD_BUTTONS];
		mapObjectImages = new Image[GAMEPAD_BUTTONS];
		mapObjectValues = new AnimatedValue[GAMEPAD_BUTTONS];
		mapObjectFadingIn = new boolean[GAMEPAD_BUTTONS];
		for (int i = 0; i < GAMEPAD_BUTTONS; i++) {
			hitImages[i] = GameImage.valueOf(String.format("GAMEPAD_%d", i)).getImage().copy();
			hitValues[i] = new AnimatedValue(HIT_FADEOUT_TIME, 1f, 0f, AnimationEquation.OUT_CUBIC);
			mapObjectImages[i] = GameImage.valueOf(String.format("GAMEPAD_MAP_%d", i)).getImage().copy();
			mapObjectValues[i] = new AnimatedValue(1, 0f, 0f, AnimationEquation.LINEAR);  // dummy
			handPosImages[i] = GameImage.valueOf(String.format("GAMEPAD_GLOW_%d", i)).getImage().copy();
			handPosValues[i] = new AnimatedValue(1, 0f, 0f, AnimationEquation.LINEAR); // dummy
		}

		LeapController.addListener(this);
	}

	/**
	 * Draws the gamepad.
	 * @param g the graphics context
	 */
	public void draw(Graphics g) {
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
		
		for (int i = 0; i < GAMEPAD_BUTTONS; i++) {
			float handPosAlpha = handPosValues[i].getValue();
			if (handPosAlpha == 0f && (leftHandPos == i || rightHandPos == i)) {
				handPosValues[i] = new AnimatedValue(GLOW_FADE_TIME, 0f, 1f, AnimationEquation.OUT_CUBIC);
				handPosImages[i].setAlpha(handPosAlpha);
				handPosImages[i].draw();
				handPosFadingOut[i] = false;
			}
			else if (handPosAlpha != 0f) {
				handPosImages[i].setAlpha(handPosAlpha);
				handPosImages[i].draw();
			}
		}
	}

	/**
	 * Updates the gamepad by a delta interval.
	 * @param delta the delta interval since the last call
	 */
	public void update(int delta) {
		for (int i = 0; i < GAMEPAD_BUTTONS; i++) {
			hitValues[i].update(delta);
			mapObjectValues[i].update(delta);
			handPosValues[i].update(delta);
			if (mapObjectValues[i].getValue() == 1f) {
				// start fading out
				mapObjectValues[i] = new AnimatedValue(HIT_OBJECT_FADEOUT_TIME, 1f, 0f, AnimationEquation.OUT_CUBIC);
				mapObjectFadingIn[i] = false;
			}
			if (!handPosFadingOut[i] && leftHandPos != i && rightHandPos != i) {
				// start fade out if hand isn't over this position
				float curValue = handPosValues[i].getValue();
				handPosValues[i] = new AnimatedValue(GLOW_FADE_TIME, curValue, 0f, AnimationEquation.OUT_CUBIC);
				handPosFadingOut[i] = true;
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
