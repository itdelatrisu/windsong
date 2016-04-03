package itdelatrisu.potato;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import itdelatrisu.potato.audio.SoundController;
import itdelatrisu.potato.audio.SoundEffect;
import itdelatrisu.potato.map.HitObject;

/**
 * Holds all score-related data.
 */
public class ScoreData {
	/** Time, in ms, before/after a hit time to achieve the corresponding score. */
	public static final int PERFECT_TIME = 50, GOOD_TIME = 150, OKAY_TIME = 375;

	/** Score points. */
	public static final int PERFECT_SCORE = 100, GOOD_SCORE = 50, OKAY_SCORE = 20, MISS = 0;

	/** Hit object fade-in time, in ms. */
	public static final int HIT_OBJECT_FADEIN_TIME = 750;

	/** Duration, in milliseconds, of a combo pop effect. */
	private static final int COMBO_POP_TIME = 250;

	/** Count for each type of hit result. */
	private int hitPerfect, hitGood, hitOkay, hitMiss;

	/** Total object count (so far). */
	private int objectCount = 0;

	/** The current combo streak. */
	private int combo = 0;

	/** The max combo streak obtained. */
	private int comboMax = 0;

	/** The current combo pop timer, in milliseconds. */
	private int comboPopTime = 0;

	/** Current game score. */
	private long score = 0;

	/** Displayed game score (for animation, slightly behind score). */
	private long scoreDisplay;

	/** Displayed game score percent (for animation, slightly behind score percent). */
	private float scorePercentDisplay;

	/** Current health bar percentage. */
	private float health = 100f;

	/** Displayed health (for animation, slightly behind health). */
	private float healthDisplay = 0f;

	/** Score text symbol images. */
	private HashMap<Character, Image> scoreSymbols;

	/** List of hit objects currently able to be hit. */
	private List<HitObject> hitObjects = new ArrayList<HitObject>();

	/** Container dimensions. */
	private int width, height;

	/**
	 * Constructor.
	 */
	public ScoreData(GameContainer container) {
		this.width = container.getWidth();
		this.height = container.getHeight();

		// score symbol images
		scoreSymbols = new HashMap<Character, Image>(14);
		scoreSymbols.put('0', GameImage.SCORE_0.getImage());
		scoreSymbols.put('1', GameImage.SCORE_1.getImage());
		scoreSymbols.put('2', GameImage.SCORE_2.getImage());
		scoreSymbols.put('3', GameImage.SCORE_3.getImage());
		scoreSymbols.put('4', GameImage.SCORE_4.getImage());
		scoreSymbols.put('5', GameImage.SCORE_5.getImage());
		scoreSymbols.put('6', GameImage.SCORE_6.getImage());
		scoreSymbols.put('7', GameImage.SCORE_7.getImage());
		scoreSymbols.put('8', GameImage.SCORE_8.getImage());
		scoreSymbols.put('9', GameImage.SCORE_9.getImage());
		scoreSymbols.put(',', GameImage.SCORE_COMMA.getImage());
		scoreSymbols.put('.', GameImage.SCORE_DOT.getImage());
		scoreSymbols.put('%', GameImage.SCORE_PERCENT.getImage());
		scoreSymbols.put('x', GameImage.SCORE_X.getImage());
	}

	/**
	 * Draws a string of scoreSymbols of fixed width.
	 * @param str the string to draw
	 * @param x the starting x coordinate
	 * @param y the y coordinate
	 * @param scale the scale to apply
	 * @param fixedsize the width to use for all symbols
	 * @param rightAlign align right (true) or left (false)
	 */
	private void drawFixedSizeSymbolString(String str, float x, float y, float scale, float fixedsize, boolean rightAlign) {
		char[] c = str.toCharArray();
		float cx = x;
		if (rightAlign) {
			for (int i = c.length - 1; i >= 0; i--) {
				Image digit = scoreSymbols.get(c[i]);
				if (scale != 1.0f)
					digit = digit.getScaledCopy(scale);
				cx -= fixedsize;
				digit.draw(cx + (fixedsize - digit.getWidth()) / 2, y);
			}
		} else {
			for (int i = 0; i < c.length; i++) {
				Image digit = scoreSymbols.get(c[i]);
				if (scale != 1.0f)
					digit = digit.getScaledCopy(scale);
				digit.draw(cx + (fixedsize - digit.getWidth()) / 2, y);
				cx += fixedsize;
			}
		}
	}

	/**
	 * Draws a string of scoreSymbols.
	 * @param str the string to draw
	 * @param x the starting x coordinate
	 * @param y the y coordinate
	 * @param scale the scale to apply
	 * @param alpha the alpha level
	 * @param rightAlign align right (true) or left (false)
	 */
	public void drawSymbolString(String str, float x, float y, float scale, float alpha, boolean rightAlign) {
		char[] c = str.toCharArray();
		float cx = x;
		if (rightAlign) {
			for (int i = c.length - 1; i >= 0; i--) {
				Image digit = scoreSymbols.get(c[i]);
				if (scale != 1.0f)
					digit = digit.getScaledCopy(scale);
				cx -= digit.getWidth();
				digit.setAlpha(alpha);
				digit.draw(cx, y);
				digit.setAlpha(1f);
			}
		} else {
			for (int i = 0; i < c.length; i++) {
				Image digit = scoreSymbols.get(c[i]);
				if (scale != 1.0f)
					digit = digit.getScaledCopy(scale);
				digit.setAlpha(alpha);
				digit.draw(cx, y);
				digit.setAlpha(1f);
				cx += digit.getWidth();
			}
		}
	}

	/**
	 * Draws game elements.
	 * @param g the graphics context
	 */
	public void drawGameElements(Graphics g) {
		int margin = (int) (width * 0.008f);
		float uiScale = GameImage.getUIscale();

		// score
		drawFixedSizeSymbolString((scoreDisplay < 100000000) ? String.format("%08d", scoreDisplay) : Long.toString(scoreDisplay),
				width - margin, 0, 1.0f, scoreSymbols.get('0').getWidth() - 2, true);

		// score percentage
		int symbolHeight = scoreSymbols.get('0').getHeight();
		drawSymbolString(
			String.format((scorePercentDisplay < 10f) ? "0%.2f%%" : "%.2f%%", scorePercentDisplay),
			width - margin, symbolHeight, 0.60f, 1f, true);

		// scorebar
		float healthRatio = healthDisplay / 100f;
		Image scorebar = GameImage.SCOREBAR_BG.getImage();
		Image colour = GameImage.SCOREBAR_COLOUR.getImage();
		float colourX = 4 * uiScale, colourY = 15 * uiScale;
		Image colourCropped = colour.getSubImage(0, 0, (int) (645 * uiScale * healthRatio), colour.getHeight());
		scorebar.setAlpha(1f);
		scorebar.draw(0, 0);
		colourCropped.draw(colourX, colourY);
		Image ki = null;
		if (health >= 50f)
			ki = GameImage.SCOREBAR_KI.getImage();
		else if (health >= 25f)
			ki = GameImage.SCOREBAR_KI_DANGER.getImage();
		else
			ki = GameImage.SCOREBAR_KI_DANGER2.getImage();
		if (comboPopTime < COMBO_POP_TIME)
			ki = ki.getScaledCopy(1f + (0.45f * (1f - (float) comboPopTime / COMBO_POP_TIME)));
		ki.drawCentered(colourX + colourCropped.getWidth(), colourY);

		// combo count
		if (combo > 0) {
			float comboPop = 1 - ((float) comboPopTime / COMBO_POP_TIME);
			float comboPopBack  = 1 + comboPop * 0.45f;
			float comboPopFront = 1 + comboPop * 0.08f;
			String comboString = String.format("%dx", combo);
			if (comboPopTime != COMBO_POP_TIME)
				drawSymbolString(comboString, margin, height - margin - (symbolHeight * comboPopBack), comboPopBack, 0.5f, false);
			drawSymbolString(comboString, margin, height - margin - (symbolHeight * comboPopFront), comboPopFront, 1f, false);
		}
	}

	/** Returns the current health percentage. */
	public float getHealth() { return health; }

	/**
	 * Changes health by a given percentage.
	 * @param percent the health percentage
	 */
	public void changeHealth(float percent) { health = Utils.clamp(health + percent, 0f, 100f); }

	/** Returns the current score. */
	public long getScore() { return score; }

	/** Returns the current combo streak. */
	public int getComboStreak() { return combo; }

	/**
	 * Increases the combo streak by one.
	 */
	private void incrementComboStreak() {
		combo++;
		comboPopTime = 0;
		if (combo > comboMax)
			comboMax = combo;
	}

	/** Resets the combo streak to zero. */
	private void resetComboStreak() {
		if (combo >= 20)
			SoundController.playSound(SoundEffect.COMBOBREAK);
		combo = 0;
	}

	/**
	 * Returns the raw score percentage.
	 * @param perfect the number of perfect hits
	 * @param good the number of good hits
	 * @param okay the number of okay hits
	 * @param miss the number of misses
	 * @return the score percentage
	 */
	private static float getScorePercent(int perfect, int good, int okay, int miss) {
		float percent = 0;
		int objectCount = perfect + good + okay + miss;
		if (objectCount > 0)
			percent = (float) (perfect * PERFECT_SCORE + good * GOOD_SCORE + okay * OKAY_SCORE) / (objectCount * PERFECT_SCORE) * 100f;
		return percent;
	}

	/**
	 * Returns the raw score percentage.
	 */
	private float getScorePercent() { return getScorePercent(hitPerfect, hitGood, hitOkay, hitMiss); }

	/**
	 * Registers the given map hit object.
	 * This should be called when the hit object first appears on the screen (i.e. during fade-in).
	 * @param hitObject the hit object
	 */
	public void sendMapObject(HitObject hitObject) {
		hitObjects.add(hitObject);
		objectCount++;
	}

	/**
	 * Sends the hit to be judged.
	 * @param pos the gamepad position
	 * @param time the hit time
	 * @return the score for the hit
	 */
	public long sendHit(int pos, int time) {
		// check all registered hit objects...
		for (HitObject h : hitObjects) {
			if (h.getPosition() == pos) {
				// TODO: animation graphic magic stuff

				// compute score based on time difference
				int timeDiff = Math.abs(h.getTime() - time);
				int points;
				if (timeDiff < PERFECT_TIME) {
					points = PERFECT_SCORE;
					hitPerfect++;
					changeHealth(5f);
				} else if (timeDiff < GOOD_TIME) {
					points = GOOD_SCORE;
					hitGood++;
					changeHealth(2f);
				} else if (timeDiff < OKAY_TIME) {
					points = OKAY_SCORE;
					hitOkay++;
				} else {
					points = MISS;
					hitMiss++;
					changeHealth(-5f);
					resetComboStreak();
				}

				// successful hit!
				if (points != MISS) {
					// increment score/combo
					score += points;
					incrementComboStreak();

					// play the hit sound
					SoundController.playHitSound(h.getSound());
				}

				// remove the hit object
				hitObjects.remove(h);

				return score;
			}
		}
		return MISS;
	}

	/**
	 * Updates the score data by a delta interval.
	 * @param delta the delta interval since the last call
	 * @param trackPosition the track position
	 */
	public void update(int delta, int trackPosition) {
		// remove hit objects after their time has expired
		List<HitObject> toRemove = new ArrayList<HitObject>();
		for (HitObject h : hitObjects) {
			if (h.getTime() - trackPosition < -OKAY_TIME)
				toRemove.add(h);
		}
		if (!toRemove.isEmpty()) {
			for (HitObject r : toRemove)
				hitObjects.remove(r);

			// count misses and break combo
			hitMiss += toRemove.size();
			changeHealth(-5f * toRemove.size());
			resetComboStreak();
		}

		// score display
		if (scoreDisplay < score) {
			scoreDisplay += (score - scoreDisplay) * delta / 50 + 1;
			if (scoreDisplay > score)
				scoreDisplay = score;
		}

		// score percent display
		float scorePercent = getScorePercent();
		if (scorePercentDisplay != scorePercent) {
			if (scorePercentDisplay < scorePercent) {
				scorePercentDisplay += (scorePercent - scorePercentDisplay) * delta / 50f + 0.01f;
				if (scorePercentDisplay > scorePercent)
					scorePercentDisplay = scorePercent;
			} else {
				scorePercentDisplay -= (scorePercentDisplay - scorePercent) * delta / 50f + 0.01f;
				if (scorePercentDisplay < scorePercent)
					scorePercentDisplay = scorePercent;
			}
		}

		// drain health...
		changeHealth(-delta / 200f);

		// health display
		if (healthDisplay != health) {
			float shift = delta / 15f;
			if (healthDisplay < health) {
				healthDisplay += shift;
				if (healthDisplay > health)
					healthDisplay = health;
			} else {
				healthDisplay -= shift;
				if (healthDisplay < health)
					healthDisplay = health;
			}
		}

		// combo pop
		comboPopTime += delta;
		if (comboPopTime > COMBO_POP_TIME)
			comboPopTime = COMBO_POP_TIME;
	}
}