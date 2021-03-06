package itdelatrisu.windsong;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.util.jar.JarFile;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.Log;

import itdelatrisu.windsong.audio.SoundController;
import itdelatrisu.windsong.audio.SoundEffect;
import itdelatrisu.windsong.ui.Fonts;
import itdelatrisu.windsong.ui.UI;

/**
 * Contains miscellaneous utilities.
 */
public class Utils {
	/**
	 * List of illegal filename characters.
	 * @see #cleanFileName(String, char)
	 */
	private final static int[] illegalChars = {
		34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
		11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
		24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47
	};
	static {
		Arrays.sort(illegalChars);
	}

	// game-related variables
	private static Input input;

	// This class should not be instantiated.
	private Utils() {}

	/**
	 * Initializes game settings and class data.
	 * @param container the game container
	 * @param game the game object
	 */
	public static void init(GameContainer container, StateBasedGame game) {
		input = container.getInput();
		int width = container.getWidth();
		int height = container.getHeight();

		// game settings
		container.setTargetFrameRate(Options.getTargetFPS());
		container.setVSync(Options.getTargetFPS() == 60);
		container.setMusicVolume(Options.getMusicVolume() * Options.getMasterVolume());
		container.setShowFPS(false);
		container.getInput().enableKeyRepeat();
		container.setAlwaysRender(true);
		container.setUpdateOnlyWhenVisible(false);

		// calculate UI scale
		GameImage.init(width, height);

		// create fonts
		try {
			Fonts.init();
		} catch (Exception e) {
			ErrorHandler.error("Failed to load fonts.", e, true);
		}

		// initialize game images
		for (GameImage img : GameImage.values()) {
			if (img.isPreload())
				img.setDefaultImage();
		}

		// initialize UI components
		UI.init(container, game);
	}

	/**
	 * Draws an animation based on its center.
	 * @param anim the animation to draw
	 * @param x the center x coordinate
	 * @param y the center y coordinate
	 */
	public static void drawCentered(Animation anim, float x, float y) {
		anim.draw(x - (anim.getWidth() / 2f), y - (anim.getHeight() / 2f));
	}

	/**
	 * Clamps a value between a lower and upper bound.
	 * @param val the value to clamp
	 * @param low the lower bound
	 * @param high the upper bound
	 * @return the clamped value
	 * @author fluddokt
	 */
	public static int clamp(int val, int low, int high) {
		if (val < low)
			return low;
		if (val > high)
			return high;
		return val;
	}

	/**
	 * Clamps a value between a lower and upper bound.
	 * @param val the value to clamp
	 * @param low the lower bound
	 * @param high the upper bound
	 * @return the clamped value
	 * @author fluddokt
	 */
	public static float clamp(float val, float low, float high) {
		if (val < low)
			return low;
		if (val > high)
			return high;
		return val;
	}

	/**
	 * Returns the distance between two points.
	 * @param x1 the x-component of the first point
	 * @param y1 the y-component of the first point
	 * @param x2 the x-component of the second point
	 * @param y2 the y-component of the second point
	 * @return the Euclidean distance between points (x1,y1) and (x2,y2)
	 */
	public static float distance(float x1, float y1, float x2, float y2) {
		float v1 = Math.abs(x1 - x2);
		float v2 = Math.abs(y1 - y2);
		return (float) Math.sqrt((v1 * v1) + (v2 * v2));
	}

	/**
	 * Linear interpolation of a and b at t.
	 */
	public static float lerp(float a, float b, float t) {
		return a * (1 - t) + b * t;
	}

	/**
	 * Returns true if a game input key is pressed (mouse/keyboard left/right).
	 * @return true if pressed
	 */
	public static boolean isGameKeyPressed() {
		boolean mouseDown = input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON) || input.isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON);
		return (mouseDown ||
				input.isKeyDown(Options.getGameKeyLeft()) ||
				input.isKeyDown(Options.getGameKeyRight()));
	}

	/**
	 * Takes a screenshot.
	 * @author http://wiki.lwjgl.org/index.php?title=Taking_Screen_Shots
	 */
	public static void takeScreenShot() {
		// create the screenshot directory
		File dir = Options.getScreenshotDir();
		if (!dir.isDirectory() && !dir.mkdir()) {
			ErrorHandler.error(String.format("Failed to create screenshot directory at '%s'.", dir.getAbsolutePath()), null, false);
			return;
		}

		// create file name
		SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd_HHmmss");
		final File file = new File(dir, String.format("screenshot_%s.%s",
				date.format(new Date()), Options.getScreenshotFormat()));

		SoundController.playSound(SoundEffect.SHUTTER);

		// copy the screen to file
		final int width = Display.getWidth();
		final int height = Display.getHeight();
		final int bpp = 3;  // assuming a 32-bit display with a byte each for red, green, blue, and alpha
		final ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
		GL11.glReadBuffer(GL11.GL_FRONT);
		GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
		GL11.glReadPixels(0, 0, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);
		new Thread() {
			@Override
			public void run() {
				try {
					BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
					for (int x = 0; x < width; x++) {
						for (int y = 0; y < height; y++) {
							int i = (x + (width * y)) * bpp;
							int r = buffer.get(i) & 0xFF;
							int g = buffer.get(i + 1) & 0xFF;
							int b = buffer.get(i + 2) & 0xFF;
							image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
						}
					}
					ImageIO.write(image, Options.getScreenshotFormat(), file);
				} catch (Exception e) {
					ErrorHandler.error("Failed to take a screenshot.", e, true);
				}
			}
		}.start();
	}

	/**
	 * Returns a human-readable representation of a given number of bytes.
	 * @param bytes the number of bytes
	 * @return the string representation
	 * @author aioobe (http://stackoverflow.com/a/3758880)
	 */
	public static String bytesToString(long bytes) {
		if (bytes < 1024)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(1024));
		char pre = "KMGTPE".charAt(exp - 1);
		return String.format("%.1f %cB", bytes / Math.pow(1024, exp), pre);
	}

	/**
	 * Cleans a file name.
	 * @param badFileName the original name string
	 * @param replace the character to replace illegal characters with (or 0 if none)
	 * @return the cleaned file name
	 * @author Sarel Botha (http://stackoverflow.com/a/5626340)
	 */
	public static String cleanFileName(String badFileName, char replace) {
		if (badFileName == null)
			return null;

		boolean doReplace = (replace > 0 && Arrays.binarySearch(illegalChars, replace) < 0);
		StringBuilder cleanName = new StringBuilder();
		for (int i = 0, n = badFileName.length(); i < n; i++) {
			int c = badFileName.charAt(i);
			if (Arrays.binarySearch(illegalChars, c) < 0)
				cleanName.append((char) c);
			else if (doReplace)
				cleanName.append(replace);
		}
		return cleanName.toString();
	}

	/**
	 * Converts an input stream to a string.
	 * @param is the input stream
	 * @author Pavel Repin, earcam (http://stackoverflow.com/a/5445161)
	 */
	public static String convertStreamToString(InputStream is) {
		try (Scanner s = new Scanner(is)) {
			return s.useDelimiter("\\A").hasNext() ? s.next() : "";
		}
	}

	/**
	 * Returns the md5 hash of a file in hex form.
	 * @param file the file to hash
	 * @return the md5 hash
	 */
	public static String getMD5(File file) {
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(file));
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] buf = new byte[4096];

			while (true) {
				int len = in.read(buf);
				if (len < 0)
					break;
				md.update(buf, 0, len);
			}
			in.close();

			byte[] md5byte = md.digest();
			StringBuilder result = new StringBuilder();
			for (byte b : md5byte)
				result.append(String.format("%02x", b));
			return result.toString();
		} catch (NoSuchAlgorithmException | IOException e) {
			ErrorHandler.error("Failed to calculate MD5 hash.", e, true);
		}
		return null;
	}

	/**
	 * Returns a formatted time string for a given number of seconds.
	 * @param seconds the number of seconds
	 * @return the time as a readable string
	 */
	public static String getTimeString(int seconds) {
		if (seconds < 3600)
			return String.format("%02d:%02d", seconds / 60, seconds % 60);
		else
			return String.format("%02d:%02d:%02d", seconds / 3600, (seconds / 60) % 60, seconds % 60);
	}

	/**
	 * Returns whether or not the application is running within a JAR.
	 * @return true if JAR, false if file
	 */
	public static boolean isJarRunning() {
		return App.class.getResource(String.format("%s.class", App.class.getSimpleName())).toString().startsWith("jar:");
	}

	/**
	 * Returns the JarFile for the application.
	 * @return the JAR file, or null if it could not be determined
	 */
	public static JarFile getJarFile() {
		if (!isJarRunning())
			return null;

		try {
			return new JarFile(new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI()), false);
		} catch (URISyntaxException | IOException e) {
			Log.error("Could not determine the JAR file.", e);
			return null;
		}
	}

	/**
	 * Returns the directory where the application is being run.
	 * @return the directory, or null if it could not be determined
	 */
	public static File getRunningDirectory() {
		try {
			return new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		} catch (URISyntaxException e) {
			Log.error("Could not get the running directory.", e);
			return null;
		}
	}

	/**
	 * Parses the integer string argument as a boolean:
	 * {@code 1} is {@code true}, and all other values are {@code false}.
	 * @param s the {@code String} containing the boolean representation to be parsed
	 * @return the boolean represented by the string argument
	 */
	public static boolean parseBoolean(String s) {
		return (Integer.parseInt(s) == 1);
	}

	/**
	 * Returns the git hash of the remote-tracking branch 'origin/master' from the
	 * most recent update to the working directory (e.g. fetch or successful push).
	 * @return the 40-character SHA-1 hash, or null if it could not be determined
	 */
	public static String getGitHash() {
		if (isJarRunning())
			return null;
		File f = new File(".git/refs/remotes/origin/master");
		if (!f.isFile())
			return null;
		try (BufferedReader in = new BufferedReader(new FileReader(f))) {
			char[] sha = new char[40];
			if (in.read(sha, 0, sha.length) < sha.length)
				return null;
			for (int i = 0; i < sha.length; i++) {
				if (Character.digit(sha[i], 16) == -1)
					return null;
			}
			return String.valueOf(sha);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Returns the file extension of a file.
	 * @param file the file name
	 */
	public static String getExtension(String file) {
		int i = file.lastIndexOf('.');
		return (i != -1) ? file.substring(i + 1).toLowerCase() : "";
	}
}
