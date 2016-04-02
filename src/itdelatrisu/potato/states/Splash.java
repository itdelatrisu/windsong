package itdelatrisu.potato.states;

import java.io.File;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import itdelatrisu.potato.App;
import itdelatrisu.potato.GameImage;
import itdelatrisu.potato.Options;
import itdelatrisu.potato.Utils;
import itdelatrisu.potato.audio.MusicController;
import itdelatrisu.potato.audio.SoundController;
import itdelatrisu.potato.map.MapParser;
import itdelatrisu.potato.ui.UI;
import itdelatrisu.potato.ui.animations.AnimatedValue;
import itdelatrisu.potato.ui.animations.AnimationEquation;

/**
 * "Splash Screen" state.
 * <p>
 * Loads game resources and enters "Main Menu" state.
 */
public class Splash extends BasicGameState {
	/** Minimum time, in milliseconds, to display the splash screen (and fade in the logo). */
	private static final int MIN_SPLASH_TIME = 2000;

	/** Whether or not loading has completed. */
	private boolean finished = false;

	/** Loading thread. */
	private Thread thread;

	/** Number of times the 'Esc' key has been pressed. */
	private int escapeCount = 0;

	/** Logo alpha level. */
	private AnimatedValue logoAlpha;

	// game-related variables
	private final int state;
	private GameContainer container;
	private boolean init = false;

	public Splash(int state) {
		this.state = state;
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		this.container = container;

		// load Utils class first (needed in other 'init' methods)
		Utils.init(container, game);

		// fade in logo
		this.logoAlpha = new AnimatedValue(MIN_SPLASH_TIME, 0f, 1f, AnimationEquation.OUT_CUBIC);
		GameImage.MENU_LOGO.getImage().setAlpha(0f);
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		g.setBackground(Color.black);
		GameImage.MENU_LOGO.getImage().drawCentered(container.getWidth() / 2, container.getHeight() / 2);
		UI.drawLoadingProgress(g);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		if (!init) {
			init = true;

			// load all resources in a new thread
			thread = new Thread() {
				@Override
				public void run() {
					File mapDir = Options.getMapDir();

					// parse song directory
					MapParser.parseAllFiles(mapDir);

					// load sounds
					SoundController.init();

					finished = true;
					thread = null;
				}
			};
			thread.start();
		}

		// fade in logo
		if (logoAlpha.update(delta))
			GameImage.MENU_LOGO.getImage().setAlpha(logoAlpha.getValue());

		// change states when loading complete
		if (finished && logoAlpha.getValue() >= 1f) {
			MusicController.playThemeSong();

			game.enterState(App.STATE_MAINMENU);
		}
	}

	@Override
	public int getID() { return state; }

	@Override
	public void keyPressed(int key, char c) {
		if (key == Input.KEY_ESCAPE) {
			// close program
			if (++escapeCount >= 3)
				container.exit();

			// stop parsing maps by sending interrupt to MapParser
			else if (thread != null)
				thread.interrupt();
		}
	}
}
