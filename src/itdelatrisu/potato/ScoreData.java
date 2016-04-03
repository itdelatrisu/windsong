package itdelatrisu.potato;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import itdelatrisu.potato.audio.SoundController;
import itdelatrisu.potato.map.HitObject;

public class ScoreData {
	/** Time, in ms, before/after a hit time to achieve the corresponding score. */
	public static final int PERFECT_TIME = 50, GOOD_TIME = 150, OKAY_TIME = 375;

	/** Score points. */
	public static final int PERFECT_SCORE = 100, GOOD_SCORE = 50, OKAY_SCORE = 20, MISS = 0;

	/** Count for each type of hit result. */
	private int hitPerfect, hitGood, hitOkay, hitMiss;

	/** Total object count (so far). */
	private int objectCount = 0;

	/** Hit object fade-in time, in ms. */
	public static final int HIT_OBJECT_FADEIN_TIME = 750;

	/** Current game score. */
	private long score = 0;

	/** Displayed game score (for animation, slightly behind score). */
	private long scoreDisplay;

	/** Displayed game score percent (for animation, slightly behind score percent). */
	private float scorePercentDisplay;

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

		// score
		drawFixedSizeSymbolString((scoreDisplay < 100000000) ? String.format("%08d", scoreDisplay) : Long.toString(scoreDisplay),
				width - margin, 0, 1.0f, scoreSymbols.get('0').getWidth() - 2, true);

		// score percentage
		int symbolHeight = scoreSymbols.get('0').getHeight();
		drawSymbolString(
			String.format((scorePercentDisplay < 10f) ? "0%.2f%%" : "%.2f%%", scorePercentDisplay),
			width - margin, symbolHeight, 0.60f, 1f, true);
	}

	/** Returns the current score. */
	public long getScore() { return score; }

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
				} else if (timeDiff < GOOD_TIME) {
					points = GOOD_SCORE;
					hitGood++;
				} else if (timeDiff < OKAY_TIME) {
					points = OKAY_SCORE;
					hitOkay++;
				} else {
					points = MISS;
					hitMiss++;
					// TODO: combo break
				}

				// increment score
				score += points;

				// play the hit sound
				if (points != MISS)
					SoundController.playHitSound(h.getSound());

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
		for (HitObject r : toRemove)
			hitObjects.remove(r);
		hitMiss += toRemove.size();
		// TODO: combo break

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
	}
}