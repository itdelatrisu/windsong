package itdelatrisu.potato.ui;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import itdelatrisu.potato.ErrorHandler;

/**
 * Updates and draws the cursor.
 */
public class Cursor {
	/** Empty cursor. */
	private static org.lwjgl.input.Cursor emptyCursor;

	// game-related variables
	private static GameContainer container;

	/**
	 * Initializes the class.
	 * @param container the game container
	 * @param game the game object
	 */
	public static void init(GameContainer container, StateBasedGame game) {
		Cursor.container = container;

		// create empty cursor to simulate hiding the cursor
		try {
			int min = org.lwjgl.input.Cursor.getMinCursorSize();
			IntBuffer tmp = BufferUtils.createIntBuffer(min * min);
			emptyCursor = new org.lwjgl.input.Cursor(min, min, min/2, min/2, 1, tmp, null);
		} catch (LWJGLException e) {
			ErrorHandler.error("Failed to create hidden cursor.", e, true);
		}
	}

	/**
	 * Constructor.
	 */
	public Cursor() {}

	/**
	 * Hides the cursor, if possible.
	 */
	public void hide() {
		if (emptyCursor != null) {
			try {
				container.setMouseCursor(emptyCursor, 0, 0);
			} catch (SlickException e) {
				ErrorHandler.error("Failed to hide the cursor.", e, true);
			}
		}
	}

	/**
	 * Unhides the cursor.
	 */
	public void show() {
		container.setDefaultMouseCursor();
	}
}
