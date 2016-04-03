package itdelatrisu.windsong.states;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.EasedFadeOutTransition;
import org.newdawn.slick.state.transition.FadeInTransition;

import itdelatrisu.windsong.App;
import itdelatrisu.windsong.ScoreData;
import itdelatrisu.windsong.Utils;
import itdelatrisu.windsong.audio.MusicController;
import itdelatrisu.windsong.audio.SoundController;
import itdelatrisu.windsong.audio.SoundEffect;
import itdelatrisu.windsong.leap.LeapController;
import itdelatrisu.windsong.leap.LeapListener;
import itdelatrisu.windsong.map.HitObject;
import itdelatrisu.windsong.ui.Fonts;
import itdelatrisu.windsong.ui.UI;

/**
 * "Training" state.
 */
public class Training extends BasicGameState implements LeapListener {
	/** The score data instance. */
	private ScoreData scoreData;

	/** The interval between map hit objects, in ms. */
	private static final int EVENT_INTERVAL = 2000;

	/** Time remaining until the next hit object. */
	private int timeToNext = EVENT_INTERVAL;

	/** Time since starting training. */
	private int time = 0;

	/** Whether the click sound was played. */
	private boolean soundPlayed = false;

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

		// last hit result
		scoreData.drawLastHitResult(g);

		// text	
		float textY = height * 0.03f;
		Fonts.XLARGE.drawString(width * 0.04f, textY, "Training", Color.white);
		textY += height * 0.01f + Fonts.XLARGE.getLineHeight();
		Fonts.MEDIUM.drawString(width * 0.04f, textY, "Hit the illuminated square when the pattern is fully visible.");
		textY += Fonts.MEDIUM.getLineHeight() * 1.1f;
		Fonts.MEDIUM.drawString(width * 0.04f, textY, "When you are ready, press space or click anywhere to continue.");

		UI.draw(g);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		time += delta;
		timeToNext -= delta;
		if (timeToNext < 0) {
			int pos = (int) (Math.random() * 9);
			UI.getGamepad().sendMapObject(pos, ScoreData.HIT_OBJECT_FADEIN_TIME);
			HitObject hit = new HitObject(time + ScoreData.HIT_OBJECT_FADEIN_TIME, pos, HitObject.SOUND_CLAP);
			scoreData.sendMapObject(hit);

			soundPlayed = false;
			timeToNext = EVENT_INTERVAL;
		}
		if (!soundPlayed && timeToNext < EVENT_INTERVAL - ScoreData.HIT_OBJECT_FADEIN_TIME) {
			soundPlayed = true;
			SoundController.playSound(SoundEffect.MENUCLICK);
		}

		UI.update(delta);
		UI.getGamepad().update(delta);
		scoreData.update(delta, time);
	}

	@Override
	public int getID() { return state; }

	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		UI.enter();
		UI.getGamepad().reset();
		MusicController.pause();
		scoreData = new ScoreData(container);
		timeToNext = EVENT_INTERVAL;
		time = 0;
		soundPlayed = true;
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
		UI.changeVolume((newValue < 0) ? -1 : 1);
	}

	@Override
	public void keyPressed(int key, char c) {
		switch (key) {
		case Input.KEY_ESCAPE:
			MusicController.playAt(0, true);
			game.enterState(App.STATE_MAINMENU, new EasedFadeOutTransition(), new FadeInTransition());
			break;
		case Input.KEY_SPACE:
		case Input.KEY_ENTER:
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
		boolean isMapObjectHit = scoreData.sendHit(pos, time) != ScoreData.MISS;
		UI.getGamepad().sendHit(pos, isMapObjectHit);
	}
}
