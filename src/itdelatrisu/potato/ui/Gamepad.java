package itdelatrisu.potato.ui;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import itdelatrisu.potato.ErrorHandler;
import itdelatrisu.potato.GameImage;
import itdelatrisu.potato.ui.animations.AnimatedValue;
import itdelatrisu.potato.ui.animations.AnimationEquation;

/**
 * Leap Motion gamepad.
 */
public class Gamepad {
	/** Number of gamepad buttons. */
	private static final int GAMEPAD_BUTTONS = 9;

	/** Time to fade out hits, in ms. */
	private static final int HIT_FADEOUT_TIME = 750;

	/** Gamepad images. */
	private Image[] gamepadImages;

	/** Alpha values of hits for each gamepad button. */
	private AnimatedValue[] hitValues;

	/**
	 * Constructor.
	 */
	public Gamepad() {
		gamepadImages = new Image[GAMEPAD_BUTTONS];
		hitValues = new AnimatedValue[GAMEPAD_BUTTONS];
		for (int i = 0; i < GAMEPAD_BUTTONS; i++) {
			gamepadImages[i] = GameImage.valueOf(String.format("GAMEPAD_%d", i)).getImage().copy();
			hitValues[i] = new AnimatedValue(HIT_FADEOUT_TIME, 1f, 0f, AnimationEquation.OUT_CUBIC);
		}
	}

	/**
	 * Draws the gamepad.
	 * @param g the graphics context
	 */
	public void draw(Graphics g) {
		Image bg = GameImage.GAMEPAD_BG.getImage();
		bg.draw();
		for (int i = 0; i < GAMEPAD_BUTTONS; i++) {
			float alpha = hitValues[i].getValue();
			if (alpha != 0f) {
				gamepadImages[i].setAlpha(alpha);
				gamepadImages[i].draw();
			}
		}
		Image gamepad = GameImage.GAMEPAD.getImage();
		gamepad.draw();
	}

	/**
	 * Updates the gamepad by a delta interval.
	 * @param delta the delta interval since the last call
	 */
	public void update(int delta) {
		for (int i = 0; i < GAMEPAD_BUTTONS; i++)
			hitValues[i].update(delta);
	}

	/**
	 * Send a hit at the given position.
	 * @param pos the gamepad position
	 */
	public void sendHit(int pos) {
		if (pos < 0 || pos >= GAMEPAD_BUTTONS) {
			ErrorHandler.error(String.format("Send gamepad hit out of bounds: %d", pos), null, false);
			return;
		}
		hitValues[pos].setTime(0);
	}

	/**
	 * Resets all gamepad values.
	 */
	public void reset() {
		for (int i = 0; i < GAMEPAD_BUTTONS; i++)
			hitValues[i].setTime(HIT_FADEOUT_TIME);
	}
}
