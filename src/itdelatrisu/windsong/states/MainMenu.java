package itdelatrisu.windsong.states;

import java.util.List;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.EasedFadeOutTransition;
import org.newdawn.slick.state.transition.FadeInTransition;

import itdelatrisu.windsong.App;
import itdelatrisu.windsong.GameImage;
import itdelatrisu.windsong.Utils;
import itdelatrisu.windsong.audio.MusicController;
import itdelatrisu.windsong.audio.SoundController;
import itdelatrisu.windsong.audio.SoundEffect;
import itdelatrisu.windsong.map.MapParser;
import itdelatrisu.windsong.map.MapFile;
import itdelatrisu.windsong.ui.Colors;
import itdelatrisu.windsong.ui.Fonts;
import itdelatrisu.windsong.ui.KineticScrolling;
import itdelatrisu.windsong.ui.PetalStream;
import itdelatrisu.windsong.ui.UI;
import itdelatrisu.windsong.ui.animations.AnimatedValue;
import itdelatrisu.windsong.ui.animations.AnimationEquation;

/**
 * "Main Menu" state.
 */
public class MainMenu extends BasicGameState {
	/** Initial fade-in times. */
	private static final int WELCOME_SHOW_TIME = 1500, WELCOME_FADE_TIME = 1000, ENTER_TIME = 1250;

	/** Delay time, in milliseconds, for double-clicking focused result. */
	private static final int FOCUS_DELAY = 250;

	/** Number of petal types, for the petal streams. */
	private static final int NUM_PETAL_STREAMS = 4;

	/** Current focused (selected) result. */
	private int focusIndex = -1;

	/** Delay time, in milliseconds, for double-clicking focused result. */
	private int focusTimer = 0;

	/** Current start result button (topmost entry). */
	private KineticScrolling startResultPos = new KineticScrolling();

	/** Button drawing values. */
	private float buttonBaseX, buttonBaseY, buttonWidth, buttonHeight, buttonOffset;

	/** Maximum number of listings to display on one screen. */
	private int maxResultsShown;

	/** The petal stream. */
	private PetalStream[] petalStreams;

	/** States (for the initial loading). */
	private enum State { INITIAL, FADE_OUT, FADE_IN, FINAL }

	/** Current state. */
	private State currentState = State.INITIAL;

	/** State transition timer. */
	private AnimatedValue stateTimer = new AnimatedValue(WELCOME_SHOW_TIME, 0f, 1f, AnimationEquation.LINEAR);

	// game-related variables
	private GameContainer container;
	private StateBasedGame game;
	private Input input;
	private final int state;

	public MainMenu(int state) {
		this.state = state;
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		this.container = container;
		this.game = game;
		this.input = container.getInput();

		int width = container.getWidth();
		int height = container.getHeight();

		// map listing coordinates/dimensions
		buttonBaseX = width * 0.04f;
		buttonBaseY = height * 0.2f;
		buttonWidth = width - buttonBaseX * 1.98f;
		buttonHeight = Fonts.MEDIUM.getLineHeight() * 2.1f;
		buttonOffset = buttonHeight * 1.1f;
		maxResultsShown = (int) ((height - buttonBaseY - (height * 0.05f) + Fonts.LARGE.getLineHeight()) / buttonOffset);

		// petal stream
		petalStreams = new PetalStream[NUM_PETAL_STREAMS];
		for (int i = 0; i < NUM_PETAL_STREAMS; ++i)
			petalStreams[i] = new PetalStream(i, width, height);
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		int width = container.getWidth(), height = container.getHeight();
		int mouseX = input.getMouseX(), mouseY = input.getMouseY();

		// initial load
		if (currentState == State.INITIAL || currentState == State.FADE_OUT) {
			// petal stream
			for (int i = 0; i < NUM_PETAL_STREAMS; ++i)
				petalStreams[i].draw();

			if (currentState == State.FADE_OUT)
				GameImage.WELCOME.getImage().setAlpha(stateTimer.getValue());
			GameImage.WELCOME.getImage().drawCentered(width / 2, height / 2);

			UI.draw(g);
			return;
		}

		List<MapFile> maps = MapParser.getMaps();
		int numMaps = maps.size();
		MapFile focusMap = (focusIndex != -1) ? maps.get(focusIndex) : null;

		// background
		Image bg = GameImage.BACKGROUND.getImage().copy();
		bg.setAlpha(stateTimer.getValue());
		bg.draw();

		// petal stream
		for (int i = 0; i < NUM_PETAL_STREAMS; ++i)
			petalStreams[i].draw();

		// title
		Color color = new Color(1f, 1f, 1f, stateTimer.getValue());
		float textY = height * 0.03f;
		Fonts.XLARGE.drawString(buttonBaseX, textY, "Windsong", color);
		textY += Fonts.XLARGE.getLineHeight() + height * 0.01f;
		if (focusMap != null) {
			String s1 = "You've selected ", s2 = " by ", s3 = ".";
			float offsetS1 = Fonts.MEDIUM.getWidth(s1), offsetS2 = Fonts.MEDIUM.getWidth(s2);
			float offsetTitle = Fonts.MEDIUMBOLD.getWidth(focusMap.title), offsetArtist = Fonts.MEDIUMBOLD.getWidth(focusMap.artist);
			Fonts.MEDIUM.drawString(buttonBaseX, textY, s1, color);
			Fonts.MEDIUMBOLD.drawString(buttonBaseX + offsetS1, textY, focusMap.title, color);
			Fonts.MEDIUM.drawString(buttonBaseX + offsetS1 + offsetTitle, textY, s2, color);
			Fonts.MEDIUMBOLD.drawString(buttonBaseX + offsetS1 + offsetTitle + offsetS2, textY, focusMap.artist, color);
			Fonts.MEDIUM.drawString(buttonBaseX + offsetS1 + offsetTitle + offsetS2 + offsetArtist, textY, s3, color);
		} else
			Fonts.MEDIUM.drawString(buttonBaseX, textY, "Select a song and press space to begin.", color);

		// map listing
		clipToResultArea(g);
		int startResult = (int) (startResultPos.getPosition() / buttonOffset);
		int offset = (int) (-startResultPos.getPosition() + startResult * buttonOffset);
		for (int i = 0; i < maxResultsShown + 1; i++) {
			int index = startResult + i;
			if (index < 0)
				continue;
			if (index >= numMaps)
				break;
			drawResult(g, maps.get(index), offset + i * buttonOffset,
					resultContains(mouseX, mouseY - offset, i), (index == focusIndex), stateTimer.getValue());
		}
		g.clearClip();
		if (numMaps > maxResultsShown)
			drawResultScrollbar(g, startResultPos.getPosition(), numMaps * buttonOffset);

		UI.draw(g);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		UI.update(delta);
		for (int i = 0; i < NUM_PETAL_STREAMS; ++i)
			petalStreams[i].update(delta);

		// initial load
		if (currentState != State.FINAL) {
			if (!stateTimer.update(delta)) {
				if (currentState == State.INITIAL) {
					currentState = State.FADE_OUT;
					stateTimer = new AnimatedValue(WELCOME_FADE_TIME, 1f, 0f, AnimationEquation.OUT_CUBIC);
				} else if (currentState == State.FADE_OUT) {
					currentState = State.FADE_IN;
					stateTimer = new AnimatedValue(ENTER_TIME, 0f, 1f, AnimationEquation.OUT_QUAD);
				} else
					currentState = State.FINAL;
			}
			return;
		}

		startResultPos.setMinMax(0, buttonOffset * (MapParser.getMaps().size() - maxResultsShown));
		startResultPos.update(delta);
		if (focusIndex != -1 && focusTimer < FOCUS_DELAY)
			focusTimer += delta;
	}

	@Override
	public int getID() { return state; }

	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		UI.enter();
		startResultPos.setPosition(0);
		focusIndex = -1;
		for (int i = 0; i < NUM_PETAL_STREAMS; ++i)
			petalStreams[i].clear();
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game)
			throws SlickException {
		currentState = State.FINAL;
		stateTimer.setTime(stateTimer.getDuration());
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		if (currentState != State.FINAL)
			return;

		// map listing
		List<MapFile> maps = MapParser.getMaps();
		int numMaps = maps.size();
		if (resultAreaContains(x, y)) {
			startResultPos.pressed();
			for (int i = 0; i < maxResultsShown + 1; i++) {
				int startResult = (int) (startResultPos.getPosition() / buttonOffset);
				int offset = (int) (-startResultPos.getPosition() + startResult * buttonOffset);

				int index = startResult + i;
				if (index >= numMaps)
					break;
				if (resultContains(x, y - offset, i)) {
					SoundController.playSound(SoundEffect.MENUCLICK);
					if (index == focusIndex) {
						if (focusTimer >= FOCUS_DELAY)  // too slow for double-click
							focusTimer = 0;
						else  // select map, change state
							game.enterState(App.STATE_TRAINING, new EasedFadeOutTransition(), new FadeInTransition());
					} else {
						// set focus and play track
						focusIndex = index;
						focusTimer = 0;
						MusicController.play(maps.get(index), true);
					}
					return;
				}
			}
		}
		focusIndex = -1;
	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		startResultPos.released();
	}

	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
		int diff = newy - oldy;
		if (diff == 0)
			return;
		startResultPos.dragged(-diff);
	}

	@Override
	public void mouseWheelMoved(int newValue) {
		// volume control if "alt" is pressed
		if (input.isKeyDown(Input.KEY_LALT) || input.isKeyDown(Input.KEY_RALT)) {
			UI.changeVolume((newValue < 0) ? -1 : 1);
			return;
		}

		// scroll map listing
		int shift = (newValue < 0) ? 1 : -1;
		int mouseX = input.getMouseX(), mouseY = input.getMouseY();
		if (resultAreaContains(mouseX, mouseY))
			startResultPos.scrollOffset(shift * buttonOffset);
	}

	@Override
	public void keyPressed(int key, char c) {
		switch (key) {
		case Input.KEY_ESCAPE:
			container.exit();
			break;
		case Input.KEY_SPACE:
		case Input.KEY_ENTER:
			if (currentState != State.FINAL)
				return;
			if (focusIndex != -1)
				game.enterState(App.STATE_TRAINING, new EasedFadeOutTransition(), new FadeInTransition());
			break;
		case Input.KEY_F12:
			Utils.takeScreenShot();
			break;
		}
	}

	/**
	 * Returns true if the coordinates are within the bounds of the
	 * download result button at the given index.
	 * @param cx the x coordinate
	 * @param cy the y coordinate
	 * @param index the index (to offset the button from the topmost button)
	 */
	private boolean resultContains(float cx, float cy, int index) {
		float y = buttonBaseY + (index * buttonOffset);
		return ((cx > buttonBaseX && cx < buttonBaseX + buttonWidth) &&
		        (cy > y && cy < y + buttonHeight));
	}

	/**
	 * Returns true if the coordinates are within the bounds of the
	 * download result button area.
	 * @param cx the x coordinate
	 * @param cy the y coordinate
	 */
	private boolean resultAreaContains(float cx, float cy) {
		return ((cx > buttonBaseX && cx < buttonBaseX + buttonWidth) &&
		        (cy > buttonBaseY && cy < buttonBaseY + buttonOffset * maxResultsShown));
	}

	/**
	 * Sets a clip to the download result button area.
	 * @param g the graphics context
	 */
	private void clipToResultArea(Graphics g) {
		g.setClip((int) buttonBaseX, (int) buttonBaseY, (int) buttonWidth, (int) (buttonOffset * maxResultsShown));
	}

	/**
	 * Draws the scroll bar for the download result buttons.
	 * @param g the graphics context
	 * @param position the start button index
	 * @param total the total number of buttons
	 */
	private void drawResultScrollbar(Graphics g, float position, float total) {
		UI.drawScrollbar(g, position, total, maxResultsShown * buttonOffset, buttonBaseX, buttonBaseY,
				buttonWidth * 1.01f, (maxResultsShown-1) * buttonOffset + buttonHeight,
				Colors.BLACK_BG_NORMAL, Color.white, true);
	}

	/**
	 * Draws the map as a rectangular button.
	 * @param g the graphics context
	 * @param map the map to draw
	 * @param position the index (to offset the button from the topmost button)
	 * @param hover true if the mouse is hovering over this button
	 * @param focus true if the button is focused
	 * @param alpha the alpha level multiplier
	 */
	private void drawResult(Graphics g, MapFile map, float position, boolean hover, boolean focus, float alpha) {
		float textX = buttonBaseX + buttonWidth * 0.001f;
		float edgeX = buttonBaseX + buttonWidth * 0.985f;
		float y = buttonBaseY + position;
		float marginY = buttonHeight * 0.04f;

		// rectangle outline
		Color c = (focus) ? Colors.BLACK_BG_FOCUS : (hover) ? Colors.BLACK_BG_HOVER : Colors.BLACK_BG_NORMAL;
		float oldAlpha = c.a;
		c.a *= alpha;
		g.setColor(c);
		g.fillRect(buttonBaseX, y, buttonWidth, buttonHeight);
		c.a = oldAlpha;

		// grade? (TODO)
		Image img = GameImage.MUSIC_PLAY.getImage().copy(); // TODO
		img.setAlpha(alpha);
		img.drawCentered(textX + img.getWidth() / 2, y + buttonHeight / 2f);
		textX += img.getWidth() + buttonWidth * 0.001f;

		// text
		Color color = new Color(1f, 1f, 1f, alpha);
		Fonts.BOLD.drawString(
				textX, y + marginY,
				String.format("%s - %s", map.artist, map.title), color);
		Fonts.DEFAULT.drawString(
				textX, y + marginY + Fonts.BOLD.getLineHeight(),
				String.format("Difficulty: %s", map.getDifficulty()), color);
		Fonts.DEFAULT.drawString(
				edgeX - Fonts.DEFAULT.getWidth(map.creator), y + marginY,
				map.creator, color);
	}
}
