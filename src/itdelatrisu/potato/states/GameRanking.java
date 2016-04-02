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
import itdelatrisu.potato.GameImage;
import itdelatrisu.potato.Utils;
import itdelatrisu.potato.ui.Fonts;
import itdelatrisu.potato.ui.UI;

/**
 * "Game Ranking" (score card) state.
 */
public class GameRanking extends BasicGameState {
	// game-related variables
	private GameContainer container;
	private StateBasedGame game;
	private final int state;
	private Input input;

	public GameRanking(int state) {
		this.state = state;
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		this.container = container;
		this.game = game;
		this.input = container.getInput();
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		g.drawImage(GameImage.BACKGROUND.getImage(), 0, 0);
		int width = container.getWidth(), height = container.getHeight();
		Fonts.LARGE.drawString(width / 10, height / 2 - Fonts.LARGE.getLineHeight() / 2, "Scorecard", Color.white);

		// TODO
		// - Show score/stats:
		//
		
		UI.draw(g);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		UI.update(delta);
		
	}

	@Override
	public int getID() { return state; }

	@Override
	public void mouseWheelMoved(int newValue) {
		if (input.isKeyDown(Input.KEY_LALT) || input.isKeyDown(Input.KEY_RALT))
			UI.changeVolume((newValue < 0) ? -1 : 1);
	}

	@Override
	public void keyPressed(int key, char c) {
		switch (key) {
		case Input.KEY_ESCAPE:
			
			break;
		case Input.KEY_F12:
			Utils.takeScreenShot();
			break;
		}
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		// TODO
		// - If "back" is selected:
		game.enterState(App.STATE_MAINMENU, new EasedFadeOutTransition(), new FadeInTransition());
	}

	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		UI.enter();
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game)
			throws SlickException {
		
	}
}
