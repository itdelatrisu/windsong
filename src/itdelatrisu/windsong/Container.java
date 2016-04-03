package itdelatrisu.windsong;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.Game;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.opengl.InternalTextureLoader;

import itdelatrisu.windsong.audio.MusicController;
import itdelatrisu.windsong.leap.LeapController;

/**
 * AppGameContainer extension that sends critical errors to ErrorHandler.
 */
public class Container extends AppGameContainer {
	/** SlickException causing game failure. */
	protected SlickException e = null;

	/**
	 * Create a new container wrapping a game
	 *
	 * @param game The game to be wrapped
	 * @throws SlickException Indicates a failure to initialise the display
	 */
	public Container(Game game) throws SlickException {
		super(game);
	}

	/**
	 * Create a new container wrapping a game
	 *
	 * @param game The game to be wrapped
	 * @param width The width of the display required
	 * @param height The height of the display required
	 * @param fullscreen True if we want fullscreen mode
	 * @throws SlickException Indicates a failure to initialise the display
	 */
	public Container(Game game, int width, int height, boolean fullscreen) throws SlickException {
		super(game, width, height, fullscreen);
	}

	@Override
	public void start() throws SlickException {
		try {
			setup();
			getDelta();
			while (running())
				gameLoop();
		} finally {
			// destroy the game container
			close_sub();
			destroy();

			// report any critical errors
			if (e != null) {
				ErrorHandler.error(null, e, true);
				e = null;
			}
		}

		if (forceExit) {
			App.close();
			System.exit(0);
		}
	}

	@Override
	protected void gameLoop() throws SlickException {
		int delta = getDelta();
		if (!Display.isVisible() && updateOnlyOnVisible) {
			try { Thread.sleep(100); } catch (Exception e) {}
		} else {
			try {
				updateAndRender(delta);
			} catch (SlickException e) {
				this.e = e;  // store exception to display later
				running = false;
				return;
			}
		}
		updateFPS();
		Display.update();
		if (Display.isCloseRequested()) {
			if (game.closeRequested())
				running = false;
		}
	}

	/**
	 * Actions to perform before destroying the game container.
	 */
	private void close_sub() {
		// save user options
		Options.saveOptions();

		// destroy images
		InternalTextureLoader.get().clear();

		// reset image references
		GameImage.clearReferences();

		// prevent loading tracks from re-initializing OpenAL
		MusicController.reset();

		// disconnect Leap Motion controller
		LeapController.close();
	}

	@Override
	public void exit() {
		super.exit();
	}
}
