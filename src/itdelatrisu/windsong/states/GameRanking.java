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
import itdelatrisu.windsong.GameImage;
import itdelatrisu.windsong.ScoreData;
import itdelatrisu.windsong.Utils;
import itdelatrisu.windsong.audio.MusicController;
import itdelatrisu.windsong.audio.SoundController;
import itdelatrisu.windsong.audio.SoundEffect;
import itdelatrisu.windsong.ui.UI;

/**
 * "Game Ranking" (score card) state.
 */
public class GameRanking extends BasicGameState {
	// game-related variables
	private StateBasedGame game;
	private final int state;
	
	private ScoreData scoreData;

	public GameRanking(int state) {
		this.state = state;
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		this.game = game;
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		g.drawImage(GameImage.BACKGROUND.getImage(), 0, 0);
		scoreData.drawScoreScreen();
		UI.draw(g);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		UI.update(delta);
	}
	
	/**
	 * Sets the score data for the score screen.
	 * @param scoreData the score data
	 */
	public void setScoreData(ScoreData scoreData) {
		this.scoreData = scoreData;
	}

	@Override
	public int getID() { return state; }

	@Override
	public void mouseWheelMoved(int newValue) {
		UI.changeVolume((newValue < 0) ? -1 : 1);
	}

	@Override
	public void keyPressed(int key, char c) {
		switch (key) {
		case Input.KEY_ESCAPE:
			SoundController.playSound(SoundEffect.MENUBACK);
			MusicController.playAt(0, true);
			game.enterState(App.STATE_MAINMENU, new EasedFadeOutTransition(), new FadeInTransition());
			break;
		case Input.KEY_ENTER:
		case Input.KEY_SPACE:
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
		SoundController.playSound(SoundEffect.MENUBACK);
		MusicController.playAt(0, true);
		game.enterState(App.STATE_MAINMENU, new EasedFadeOutTransition(), new FadeInTransition());
	}

	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		UI.enter();
		SoundController.playSound(SoundEffect.APPLAUSE);
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game)
			throws SlickException {
		
	}
}
