package itdelatrisu.potato.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.newdawn.slick.Image;

import itdelatrisu.potato.GameImage;
import itdelatrisu.potato.Utils;
import itdelatrisu.potato.ui.animations.AnimatedValue;
import itdelatrisu.potato.ui.animations.AnimationEquation;

/**
 * Horizontal petal stream.
 */
public class PetalStream {
	/** The container dimensions. */
	private final int containerWidth, containerHeight;

	/** The petal image. */
	private final Image petalImg;

	/** The current list of petals. */
	private final List<Petal> petals;

	/** The maximum number of petals to draw at once. */
	private static final int MAX_PETALS = 10;

	/** Random number generator instance. */
	private final Random random;

	/** Contains data for a single petal. */
	private class Petal {
		/** The petal animation progress. */
		private final AnimatedValue animatedValue;

		/** The petal properties. */
		private final int distance, yOffset, angle;

		/**
		 * Creates a petal with the given properties.
		 * @param duration the time, in milliseconds, to show the petal
		 * @param distance the distance for the petal to travel in {@code duration}
		 * @param yOffset the vertical offset from the center of the container
		 * @param angle the rotation angle
		 * @param eqn the animation equation to use
		 */
		public Petal(int duration, int distance, int yOffset, int angle, AnimationEquation eqn) {
			this.animatedValue = new AnimatedValue(duration, 0f, 1f, eqn);
			this.distance = distance;
			this.yOffset = yOffset;
			this.angle = angle;
		}

		/**
		 * Draws the petal.
		 */
		public void draw() {
			float t = animatedValue.getValue();
			petalImg.setImageColor(1f, 1f, 1f, Math.min((1 - t) * 5f, 1f));
			petalImg.drawEmbedded(
					containerWidth - (distance * t), ((containerHeight - petalImg.getHeight()) / 2) + yOffset,
					petalImg.getWidth(), petalImg.getHeight(), angle);
		}

		/**
		 * Updates the animation by a delta interval.
		 * @param delta the delta interval since the last call
		 * @return true if an update was applied, false if the animation was not updated
		 */
		public boolean update(int delta) { return animatedValue.update(delta); }
	}

	/**
	 * Initializes the petal stream.
	 * @param imgId the petal type
	 * @param width the container width
	 * @param height the container height 
	 */
	public PetalStream(int imgId, int width, int height) {
		this.containerWidth = width;
		this.containerHeight = height;
		this.petalImg = GameImage.valueOf(String.format("PETAL_%d", imgId)).getImage().copy();
		this.petals = new ArrayList<Petal>();
		this.random = new Random();
	}

	/**
	 * Draws the petal stream.
	 */
	public void draw() {
		if (petals.isEmpty())
			return;

		petalImg.startUse();
		for (Petal petal : petals)
			petal.draw();
		petalImg.endUse();
	}

	/**
	 * Updates the petals in the stream by a delta interval.
	 * @param delta the delta interval since the last call
	 */
	public void update(int delta) {
		// update current petals
		Iterator<Petal> iter = petals.iterator();
		while (iter.hasNext()) {
			Petal petal = iter.next();
			if (!petal.update(delta))
				iter.remove();
		}

		// create new petals
		for (int i = petals.size(); i < MAX_PETALS; i++) {
			if (Math.random() < ((i < 5) ? 0.25 : 0.66))
				break;

			// generate petal properties
			float distanceRatio = Utils.clamp((float) getGaussian(0.65, 0.25), 0.2f, 0.925f);
			int distance = (int) (containerWidth * distanceRatio);
			int duration = (int) (distanceRatio * getGaussian(2000, 600));
			int yOffset = (int) getGaussian(0, containerHeight / 4);
			int angle = (int) getGaussian(0, 45);
			AnimationEquation eqn = random.nextBoolean() ? AnimationEquation.IN_OUT_QUAD : AnimationEquation.OUT_QUAD;

			petals.add(new Petal(duration, distance, yOffset, angle, eqn));
		}
	}

	/**
	 * Clears the petals currently in the stream.
	 */
	public void clear() { petals.clear(); }

	/**
	 * Returns the next pseudorandom, Gaussian ("normally") distributed {@code double} value
	 * with the given mean and standard deviation.
	 * @param mean the mean
	 * @param stdDev the standard deviation
	 */
	private double getGaussian(double mean, double stdDev) {
		return mean + random.nextGaussian() * stdDev;
	}
}
