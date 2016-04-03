package itdelatrisu.potato.states;

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

import itdelatrisu.potato.App;
import itdelatrisu.potato.GameImage;
import itdelatrisu.potato.Utils;
import itdelatrisu.potato.audio.MusicController;
import itdelatrisu.potato.audio.SoundController;
import itdelatrisu.potato.audio.SoundEffect;
import itdelatrisu.potato.map.MapParser;
import itdelatrisu.potato.map.PotatoMap;
import itdelatrisu.potato.ui.Colors;
import itdelatrisu.potato.ui.Fonts;
import itdelatrisu.potato.ui.KineticScrolling;
import itdelatrisu.potato.ui.StarStream;
import itdelatrisu.potato.ui.UI;
import itdelatrisu.potato.ui.animations.AnimatedValue;
import itdelatrisu.potato.ui.animations.AnimationEquation;

/**
 * "Main Menu" state.
 * <p>
 * Players are able to enter the song menu or downloads menu from this state.
 */
public class MainMenu extends BasicGameState {
	/** Delay time, in milliseconds, for double-clicking focused result. */
	private static final int FOCUS_DELAY = 250;

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

	/** Background alpha level (for fade-in effect). */
	private AnimatedValue bgAlpha = new AnimatedValue(1100, 0f, 0.9f, AnimationEquation.LINEAR);

	/** The star stream. */
	private StarStream starStream;

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

		// star stream
		starStream = new StarStream(width, height);
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		int width = container.getWidth(), height = container.getHeight();
		int mouseX = input.getMouseX(), mouseY = input.getMouseY();
		boolean inDropdownMenu = false;  // TODO?

		List<PotatoMap> maps = MapParser.getMaps();
		int numMaps = maps.size();
		PotatoMap focusMap = (focusIndex != -1) ? maps.get(focusIndex) : null;

		// background
		Image bg = GameImage.BACKGROUND.getImage().copy();
		bg.setAlpha(bgAlpha.getValue());
		bg.draw();

		// star stream
		starStream.draw();

		// title
		float textY = height * 0.03f;
		Fonts.XLARGE.drawString(buttonBaseX, textY, "Potato", Color.white);
		textY += height * 0.01f;
		if (focusMap != null) {
			float textOffsetX = Fonts.MEDIUMBOLD.getWidth("Selected: ");
			Fonts.MEDIUMBOLD.drawString(buttonBaseX, textY + Fonts.XLARGE.getLineHeight(), "Selected: ");
			Fonts.MEDIUM.drawString(buttonBaseX + textOffsetX, textY + Fonts.XLARGE.getLineHeight(), focusMap.title);
			Fonts.MEDIUM.drawString(buttonBaseX + textOffsetX, textY + Fonts.XLARGE.getLineHeight() + Fonts.MEDIUM.getLineHeight(), focusMap.artist);
		} else
			Fonts.MEDIUM.drawString(buttonBaseX, textY + Fonts.XLARGE.getLineHeight(), "Select a song to begin!");

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
					resultContains(mouseX, mouseY - offset, i) && !inDropdownMenu,
					(index == focusIndex));
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
		bgAlpha.update(delta);
		startResultPos.setMinMax(0, buttonOffset * (MapParser.getMaps().size() - maxResultsShown));
		startResultPos.update(delta);
		if (focusIndex != -1 && focusTimer < FOCUS_DELAY)
			focusTimer += delta;
		starStream.update(delta);
	}

	@Override
	public int getID() { return state; }

	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		UI.enter();
		startResultPos.setPosition(0);
		focusIndex = -1;
		starStream.clear();
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game)
			throws SlickException {
		
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		// map listing
		List<PotatoMap> maps = MapParser.getMaps();
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
					break;
				}
			}
			return;
		}
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
	 */
	private void drawResult(Graphics g, PotatoMap map, float position, boolean hover, boolean focus) {
		float textX = buttonBaseX + buttonWidth * 0.001f;
		float edgeX = buttonBaseX + buttonWidth * 0.985f;
		float y = buttonBaseY + position;
		float marginY = buttonHeight * 0.04f;

		// rectangle outline
		g.setColor((focus) ? Colors.BLACK_BG_FOCUS : (hover) ? Colors.BLACK_BG_HOVER : Colors.BLACK_BG_NORMAL);
		g.fillRect(buttonBaseX, y, buttonWidth, buttonHeight);

		// grade? (TODO)
		Image grade = GameImage.MUSIC_PLAY.getImage(); // TODO
		grade.drawCentered(textX + grade.getWidth() / 2, y + buttonHeight / 2f);
		textX += grade.getWidth() + buttonWidth * 0.001f;

		// text
		Fonts.BOLD.drawString(
				textX, y + marginY,
				String.format("%s - %s", map.artist, map.title), Color.white);
		Fonts.DEFAULT.drawString(
				textX, y + marginY + Fonts.BOLD.getLineHeight(),
				String.format("Difficulty: %d", map.difficulty), Color.white);
		Fonts.DEFAULT.drawString(
				edgeX - Fonts.DEFAULT.getWidth(map.creator), y + marginY,
				map.creator, Color.white);
	}
}
