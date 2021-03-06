package itdelatrisu.windsong.states;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.EasedFadeOutTransition;
import org.newdawn.slick.state.transition.FadeInTransition;

import itdelatrisu.windsong.App;
import itdelatrisu.windsong.ErrorHandler;
import itdelatrisu.windsong.GameImage;
import itdelatrisu.windsong.Options;
import itdelatrisu.windsong.ScoreData;
import itdelatrisu.windsong.Utils;
import itdelatrisu.windsong.audio.MusicController;
import itdelatrisu.windsong.audio.SoundController;
import itdelatrisu.windsong.audio.SoundEffect;
import itdelatrisu.windsong.leap.LeapController;
import itdelatrisu.windsong.leap.LeapListener;
import itdelatrisu.windsong.map.MapFile;
import itdelatrisu.windsong.ui.UI;

/**
 * "Game" state.
 */
public class Game extends BasicGameState implements LeapListener {
	/** Time before the music starts, in ms. */
	private static final int MUSIC_ENTER_TIME = 1000;

	/** Time before entering the ranking screen after the last hit object, in ms. */
	private static final int MUSIC_END_TIME_DELAY = 2000;

	/** Music fade-out time, in milliseconds. */
	private static final int MUSIC_FADEOUT_TIME = 500;

	/** Time left the music starts. */
	private int musicEnterTimer;

	/** The associated map. */
	private MapFile map;

	/** Current hit object index. */
	private int objectIndex = 0;

	/** The score data instance. */
	private ScoreData scoreData;

	// game-related variables
	private StateBasedGame game;
	private final int state;

	public Game(int state) {
		this.state = state;
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		this.game = game;

		LeapController.addListener(this);
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		// background
		GameImage.BACKGROUND.getImage().draw();

		// show gamepad
		UI.getGamepad().draw(g);

		// last hit result
		scoreData.drawLastHitResult(g);

		// draw game elements
		scoreData.drawGameElements(g);

		UI.draw(g);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		UI.update(delta);
		UI.getGamepad().update(delta);
		int trackPosition = MusicController.getPosition();

		// update score data
		scoreData.update(delta, trackPosition);

		// delay before music starts
		if (musicEnterTimer > 0) {
			musicEnterTimer -= delta;
			if (musicEnterTimer <= 0)
				MusicController.playAt(0, false);
			return;
		}

		// is the game finished?
		if (objectIndex >= map.objects.length) {
			if (trackPosition >= map.getEndTime() + MUSIC_END_TIME_DELAY) {
				((GameRanking)(game.getState(App.STATE_GAMERANKING))).setScoreData(scoreData);
				game.enterState(App.STATE_GAMERANKING, new EasedFadeOutTransition(), new FadeInTransition());
			}
			return;
		}

		// dead?
		if (scoreData.getHealth() < 1f && !Options.isNoFail()) {
			MusicController.fadeOut(MUSIC_FADEOUT_TIME);
			game.enterState(App.STATE_GAMEOVER, new EasedFadeOutTransition(), new FadeInTransition());
			return;
		}

		// advance objectIndex
		while (map.objects[objectIndex].getTime() - trackPosition <= ScoreData.HIT_OBJECT_FADEIN_TIME) {
			scoreData.sendMapObject(map.objects[objectIndex]);
			UI.getGamepad().sendMapObject(map.objects[objectIndex].getPosition(), map.objects[objectIndex].getTime() - trackPosition);
			if (++objectIndex >= map.objects.length)
				break;
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
		case Input.KEY_SPACE:
			// TODO for debugging, delete me
			((GameRanking)(game.getState(App.STATE_GAMERANKING))).setScoreData(scoreData);
			game.enterState(App.STATE_GAMERANKING, new EasedFadeOutTransition(), new FadeInTransition());
		case Input.KEY_F12:
			Utils.takeScreenShot();
			break;
		}
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
		map = MusicController.getMap();
		if (map == null)
			ErrorHandler.error("Starting game with no map.", null, false);
		objectIndex = 0;
		scoreData = new ScoreData(container);
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
	public void onPos(boolean leftHand, int pos) {}

	@Override
	public void onHit(int pos) {
		if (game.getCurrentStateID() != this.getID())
			return;
		boolean isMapObjectHit = scoreData.sendHit(pos, MusicController.getPosition()) != ScoreData.MISS;
		UI.getGamepad().sendHit(pos, isMapObjectHit);
	}
}
