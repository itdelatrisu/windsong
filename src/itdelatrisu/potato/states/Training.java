package itdelatrisu.potato.states;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.EasedFadeOutTransition;
import org.newdawn.slick.state.transition.FadeInTransition;

import itdelatrisu.potato.App;
import itdelatrisu.potato.Utils;
import itdelatrisu.potato.audio.MusicController;
import itdelatrisu.potato.leap.LeapController;
import itdelatrisu.potato.leap.LeapListener;
import itdelatrisu.potato.map.HitObject;
import itdelatrisu.potato.ui.Fonts;
import itdelatrisu.potato.ui.UI;

/**
 * "Training" state.
 */
public class Training extends BasicGameState implements LeapListener {
	// game-related variables
	private GameContainer container;
	private StateBasedGame game;
	private Input input;
	private final int state;

	public Training(int state) {
		this.state = state;
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		this.container = container;
		this.game = game;
		this.input = container.getInput();

		LeapController.addListener(this);
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		int width = container.getWidth(), height = container.getHeight();

		// show gamepad
		UI.getGamepad().draw(g);

		// text
		float textY = height * 0.03f;
		Fonts.XLARGE.drawString(width * 0.04f, textY, "Training", Color.white);
		textY += height * 0.01f;
		Fonts.MEDIUM.drawString(width * 0.04f, textY + Fonts.XLARGE.getLineHeight(), "Click anywhere or press space to continue.");

		UI.draw(g);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		UI.update(delta);
		UI.getGamepad().update(delta);
	}

	@Override
	public int getID() { return state; }

	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		UI.enter();
		UI.getGamepad().reset();
		MusicController.pause();
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game)
			throws SlickException {
		
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		game.enterState(App.STATE_GAME, new EasedFadeOutTransition(), new FadeInTransition());
	}

	@Override
	public void mouseWheelMoved(int newValue) {
		
	}

	@Override
	public void keyPressed(int key, char c) {
		switch (key) {
		case Input.KEY_ESCAPE:
			MusicController.playAt(0, true);
			game.enterState(App.STATE_MAINMENU, new EasedFadeOutTransition(), new FadeInTransition());
			break;
		case Input.KEY_SPACE:
			game.enterState(App.STATE_GAME, new EasedFadeOutTransition(), new FadeInTransition());
			break;
		case Input.KEY_F12:
			Utils.takeScreenShot();
			break;
		}
	}

	@Override
	public void onConnect() {}

	@Override
	public void onDisconnect() {}

	@Override
	public void onHit(int pos) {
		if (game.getCurrentStateID() != this.getID())
			return;
		UI.getGamepad().sendHit(pos, HitObject.SOUND_CLAP);
	}
}
