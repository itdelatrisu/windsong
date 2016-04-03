package itdelatrisu.potato.states;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.EasedFadeOutTransition;
import org.newdawn.slick.state.transition.FadeInTransition;

import itdelatrisu.potato.App;
import itdelatrisu.potato.GameImage;
import itdelatrisu.potato.audio.MusicController;
import itdelatrisu.potato.audio.SoundController;
import itdelatrisu.potato.audio.SoundEffect;
import itdelatrisu.potato.ui.UI;
import itdelatrisu.potato.ui.animations.AnimatedValue;
import itdelatrisu.potato.ui.animations.AnimationEquation;

/**
 * Lose state.
 */
public class GameOver extends BasicGameState {
	/** Fade times, in ms. */
	private static final int FADE_TIME_INITIAL = 1500, SHOW_TIME = 1800, FADE_TIME_FINAL = 1800;

	/** Text fade value. */
	private AnimatedValue fadeValue;

	/** Fade timer. */
	private int timer;

	// game-related variables
	private final int state;

	public GameOver(int state) {
		this.state = state;
	}

	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		Image img = GameImage.LOSE.getImage().copy();
		img.setAlpha((timer > 0) ? 1f : fadeValue.getValue());
		img.drawCentered(container.getWidth() / 2, container.getHeight() / 2);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		UI.update(delta);
		if (timer == -1) {
			fadeValue.update(delta);
			if (fadeValue.getValue() == 1f)
				timer = SHOW_TIME;
		} else if (timer != 0) {
			timer -= delta;
			if (timer <= 0) {
				timer = 0;
				fadeValue = new AnimatedValue(FADE_TIME_FINAL, 1f, 0f, AnimationEquation.LINEAR);
			}
		} else if (fadeValue.getValue() > 0f)
			fadeValue.update(delta);
		else
			game.enterState(App.STATE_MAINMENU, new EasedFadeOutTransition(), new FadeInTransition());
	}

	@Override
	public int getID() { return state; }

	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		UI.enter();
		UI.getCursor().hide();
		SoundController.playSound(SoundEffect.FAIL);
		fadeValue = new AnimatedValue(FADE_TIME_INITIAL, 0f, 1f, AnimationEquation.IN_QUAD);
		timer = -1;
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game)
			throws SlickException {
		UI.getCursor().show();
		MusicController.playAt(0, true);
	}
}
