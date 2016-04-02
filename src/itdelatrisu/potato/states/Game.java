package itdelatrisu.potato.states;

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
import itdelatrisu.potato.audio.SoundController;
import itdelatrisu.potato.audio.SoundEffect;
import itdelatrisu.potato.leap.LeapController;
import itdelatrisu.potato.leap.LeapListener;
import itdelatrisu.potato.map.HitObject;
import itdelatrisu.potato.ui.UI;

/**
 * "Game" state.
 */
public class Game extends BasicGameState implements LeapListener {
	/** Time before the music starts, in ms. */
	private static final int MUSIC_ENTER_TIME = 1000;

	/** Time left the music starts. */
	private int musicEnterTimer;

	// game-related variables
	private GameContainer container;
	private StateBasedGame game;
	private Input input;
	private final int state;

	public Game(int state) {
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
		UI.getGamepad().draw(g);
		UI.draw(g);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		UI.update(delta);
		UI.getGamepad().update(delta);
		if (musicEnterTimer > 0) {
			musicEnterTimer -= delta;
			if (musicEnterTimer <= 0)
				MusicController.playAt(0, false);
			return;
		}
		
	}

	@Override
	public int getID() { return state; }

	@Override
	public void keyPressed(int key, char c) {
		switch (key) {
		case Input.KEY_ESCAPE:
			SoundController.playSound(SoundEffect.MENUBACK);
			MusicController.playAt(0, true);
			game.enterState(App.STATE_MAINMENU, new EasedFadeOutTransition(), new FadeInTransition());
			break;
		case Input.KEY_F12:
			Utils.takeScreenShot();
			break;
		}
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		// TODO (get rid of this)
		game.enterState(App.STATE_GAMERANKING, new EasedFadeOutTransition(), new FadeInTransition());
	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		
	}

	@Override
	public void keyReleased(int key, char c) {
		
	}

	@Override
	public void mouseWheelMoved(int newValue) {
		UI.changeVolume((newValue < 0) ? -1 : 1);
	}

	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		UI.enter();
		UI.getCursor().hide();
		UI.getGamepad().reset();
		musicEnterTimer = MUSIC_ENTER_TIME;
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game)
			throws SlickException {
		UI.getCursor().show();
	}

	@Override
	public void onConnect() {}

	@Override
	public void onDisconnect() {}

	@Override
	public void onHit(int pos) {
		if (game.getCurrentStateID() != this.getID())
			return;
		UI.getGamepad().sendHit(pos, HitObject.SOUND_CLAP);  // TODO: send hit object sound
	}
}
