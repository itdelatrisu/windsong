package itdelatrisu.windsong;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.Log;

import itdelatrisu.windsong.audio.MusicController;
import itdelatrisu.windsong.map.MapFile;
import itdelatrisu.windsong.ui.UI;

/**
 * Handles all user options.
 */
public class Options {
	/** Whether to use XDG directories. */
	public static final boolean USE_XDG = checkXDGFlag();

	/** The config directory. */
	private static final File CONFIG_DIR = getXDGBaseDir("XDG_CONFIG_HOME", ".config");

	/** The data directory. */
	private static final File DATA_DIR = getXDGBaseDir("XDG_DATA_HOME", ".local/share");

	/** The cache directory. */
	private static final File CACHE_DIR = getXDGBaseDir("XDG_CACHE_HOME", ".cache");

	/** File for logging errors. */
	public static final File LOG_FILE = new File(CONFIG_DIR, ".windsong.log");

	/** File for storing user options. */
	private static final File OPTIONS_FILE = new File(CONFIG_DIR, ".windsong.cfg");

	/** The default map directory. */
	private static final File MAP_DIR = new File(DATA_DIR, "Songs/");

	/** Directory where natives are unpacked. */
	public static final File NATIVE_DIR = new File(CACHE_DIR, "Natives/");

	/** Font file name. */
	public static final String FONT_NAME = "DroidSansFallback.ttf";

	/** Version file name. */
	public static final String VERSION_FILE = "version";

	/** Repository address. */
	public static final URI REPOSITORY_URI = URI.create("https://github.com/itdelatrisu/windsong");

	/** Issue reporting address. */
	public static final String ISSUES_URL = "https://github.com/itdelatrisu/windsong/issues/new?title=%s&body=%s";

	/** The map directory. */
	private static File mapDir;

	/** The screenshot directory (created when needed). */
	private static File screenshotDir;

	/** Port binding. */
	private static int port = 55556;

	/**
	 * Returns whether the XDG flag in the manifest (if any) is set to "true".
	 * @return true if XDG directories are enabled, false otherwise
	 */
	private static boolean checkXDGFlag() {
		JarFile jarFile = Utils.getJarFile();
		if (jarFile == null)
			return false;
		try {
			Manifest manifest = jarFile.getManifest();
			if (manifest == null)
				return false;
			Attributes attributes = manifest.getMainAttributes();
			String value = attributes.getValue("Use-XDG");
			return (value != null && value.equalsIgnoreCase("true"));
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Returns the directory based on the XDG base directory specification for
	 * Unix-like operating systems, only if the "XDG" flag is enabled.
	 * @param env the environment variable to check (XDG_*_*)
	 * @param fallback the fallback directory relative to ~home
	 * @return the XDG base directory, or the working directory if unavailable
	 */
	private static File getXDGBaseDir(String env, String fallback) {
		if (!USE_XDG)
			return new File("./");

		String OS = System.getProperty("os.name").toLowerCase();
		if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0) {
			String rootPath = System.getenv(env);
			if (rootPath == null) {
				String home = System.getProperty("user.home");
				if (home == null)
					return new File("./");
				rootPath = String.format("%s/%s", home, fallback);
			}
			File dir = new File(rootPath, "windsong");
			if (!dir.isDirectory() && !dir.mkdir())
				ErrorHandler.error(String.format("Failed to create configuration folder at '%s/windsong'.", rootPath), null, false);
			return dir;
		} else
			return new File("./");
	}

	/**
	 * The theme song string:
	 * {@code filename,title,artist,length(ms)}
	 */
	private static String themeString = "theme.ogg,On the Bach,Jingle Punks,66000";

	/** Game options. */
	public enum GameOption {
		// internal options (not displayed in-game)
		MAP_DIRECTORY ("MapDirectory") {
			@Override
			public String write() { return getMapDir().getAbsolutePath(); }

			@Override
			public void read(String s) { mapDir = new File(s); }
		},
		SCREENSHOT_DIRECTORY ("ScreenshotDirectory") {
			@Override
			public String write() { return getScreenshotDir().getAbsolutePath(); }

			@Override
			public void read(String s) { screenshotDir = new File(s); }
		},
		THEME_SONG ("ThemeSong") {
			@Override
			public String write() { return themeString; }

			@Override
			public void read(String s) { themeString = s; }
		},
		PORT ("Port") {
			@Override
			public String write() { return Integer.toString(port); }

			@Override
			public void read(String s) {
				int i = Integer.parseInt(s);
				if (i > 0 && i <= 65535)
					port = i;
			}
		},

		// in-game options
		SCREEN_RESOLUTION ("Screen Resolution", "ScreenResolution", "Restart (Ctrl+Shift+F5) to apply resolution changes.") {
			@Override
			public String getValueString() { return resolution.toString(); }

			@Override
			public void click(GameContainer container) {
				do {
					resolution = resolution.next();
				} while (resolution != Resolution.RES_800_600 && (
				         container.getScreenWidth() < resolution.getWidth() ||
				         container.getScreenHeight() < resolution.getHeight()));
			}

			@Override
			public void read(String s) {
				try {
					Resolution res = Resolution.valueOf(String.format("RES_%s", s.replace('x', '_')));
					resolution = res;
				} catch (IllegalArgumentException e) {}
			}
		},
		TARGET_FPS ("Frame Limiter", "FrameSync", "Higher values may cause high CPU usage.") {
			@Override
			public String getValueString() {
				return String.format((getTargetFPS() == 60) ? "%dfps (vsync)" : "%dfps", getTargetFPS());
			}

			@Override
			public void click(GameContainer container) {
				targetFPSindex = (targetFPSindex + 1) % targetFPS.length;
				container.setTargetFrameRate(getTargetFPS());
				container.setVSync(getTargetFPS() == 60);
			}

			@Override
			public String write() { return Integer.toString(targetFPS[targetFPSindex]); }

			@Override
			public void read(String s) {
				int i = Integer.parseInt(s);
				for (int j = 0; j < targetFPS.length; j++) {
					if (i == targetFPS[j]) {
						targetFPSindex = j;
						break;
					}
				}
			}
		},
		SHOW_FPS ("Show FPS Counter", "FpsCounter", "Show an FPS counter in the bottom-right hand corner.", true),
		SCREENSHOT_FORMAT ("Screenshot Format", "ScreenshotFormat", "Press F12 to take a screenshot.") {
			@Override
			public String getValueString() { return screenshotFormat[screenshotFormatIndex].toUpperCase(); }

			@Override
			public void click(GameContainer container) { screenshotFormatIndex = (screenshotFormatIndex + 1) % screenshotFormat.length; }

			@Override
			public String write() { return Integer.toString(screenshotFormatIndex); }

			@Override
			public void read(String s) {
				int i = Integer.parseInt(s);
				if (i >= 0 && i < screenshotFormat.length)
					screenshotFormatIndex = i;
			}
		},
		LOAD_VERBOSE ("Show Detailed Loading Progress", "LoadVerbose", "Display more specific loading information in the splash screen.", false),
		MASTER_VOLUME ("Master Volume", "VolumeUniversal", "Global volume level.", 50, 0, 100) {
			@Override
			public void drag(GameContainer container, int d) {
				super.drag(container, d);
				container.setMusicVolume(getMasterVolume() * getMusicVolume());
			}
		},
		MUSIC_VOLUME ("Music Volume", "VolumeMusic", "Volume of music.", 80, 0, 100) {
			@Override
			public void drag(GameContainer container, int d) {
				super.drag(container, d);
				container.setMusicVolume(getMasterVolume() * getMusicVolume());
			}
		},
		EFFECT_VOLUME ("Effect Volume", "VolumeEffect", "Volume of menu and game sounds.", 70, 0, 100),
		HITSOUND_VOLUME ("Hit Sound Volume", "VolumeHitSound", "Volume of hit sounds.", 90, 0, 100),
		MUSIC_OFFSET ("Music Offset", "Offset", "Adjust this value if hit objects are out of sync.", -75, -500, 500) {
			@Override
			public String getValueString() { return String.format("%dms", val); }
		},
		DISABLE_SOUNDS ("Disable All Sound Effects", "DisableSound", "May resolve Linux sound driver issues.  Requires a restart.",
				(System.getProperty("os.name").toLowerCase().contains("linux"))),
		KEY_LEFT ("Left Game Key", "keyLeft", "Select this option to input a key.") {
			@Override
			public String getValueString() { return Keyboard.getKeyName(getGameKeyLeft()); }

			@Override
			public String write() { return Keyboard.getKeyName(getGameKeyLeft()); }

			@Override
			public void read(String s) { setGameKeyLeft(Keyboard.getKeyIndex(s)); }
		},
		KEY_RIGHT ("Right Game Key", "keyRight", "Select this option to input a key.") {
			@Override
			public String getValueString() { return Keyboard.getKeyName(getGameKeyRight()); }

			@Override
			public String write() { return Keyboard.getKeyName(getGameKeyRight()); }

			@Override
			public void read(String s) { setGameKeyRight(Keyboard.getKeyIndex(s)); }
		},
		BACKGROUND_DIM ("Background Dim", "DimLevel", "Percentage to dim the background image during gameplay.", 50, 0, 100),
		IGNORE_MAP_SKINS ("Ignore All Map Skins", "IgnoremapSkins", "Never use skin element overrides provided by maps.", false),
		LOAD_HD_IMAGES ("Load HD Images", "LoadHDImages", String.format("Loads HD (%s) images when available. Increases memory usage and loading times.", GameImage.HD_SUFFIX), true),
		ENABLE_THEME_SONG ("Enable Theme Song", "MenuMusic", "Whether to play the theme song upon starting the game.", true),
		NO_FAIL ("No Fail", "NoFail", "Whether to disable failing songs.", false),
		HIDE_LOADING_PROGRESS ("Hide Loading Progress", "HideLoadingProgress", "Whether to hide loading progress on the splash screen.", true);

		/** Option name. */
		private final String name;

		/** Option name, as displayed in the configuration file. */
		private final String displayName;

		/** Option description. */
		private final String description;

		/** The boolean value for the option (if applicable). */
		protected boolean bool;

		/** The integer value for the option (if applicable). */
		protected int val;

		/** The upper and lower bounds on the integer value (if applicable). */
		private int max, min;

		/** Option types. */
		private enum OptionType { BOOLEAN, NUMERIC, OTHER };

		/** Whether or not this is a numeric option. */
		private OptionType type = OptionType.OTHER;

		/**
		 * Constructor for internal options (not displayed in-game).
		 * @param displayName the option name, as displayed in the configuration file
		 */
		GameOption(String displayName) {
			this(null, displayName, null);
		}

		/**
		 * Constructor for other option types.
		 * @param name the option name
		 * @param displayName the option name, as displayed in the configuration file
		 * @param description the option description
		 */
		GameOption(String name, String displayName, String description) {
			this.name = name;
			this.displayName = displayName;
			this.description = description;
		}

		/**
		 * Constructor for boolean options.
		 * @param name the option name
		 * @param displayName the option name, as displayed in the configuration file
		 * @param description the option description
		 * @param value the default boolean value
		 */
		GameOption(String name, String displayName, String description, boolean value) {
			this(name, displayName, description);
			this.bool = value;
			this.type = OptionType.BOOLEAN;
		}

		/**
		 * Constructor for numeric options.
		 * @param name the option name
		 * @param displayName the option name, as displayed in the configuration file
		 * @param description the option description
		 * @param value the default integer value
		 */
		GameOption(String name, String displayName, String description, int value, int min, int max) {
			this(name, displayName, description);
			this.val = value;
			this.min = min;
			this.max = max;
			this.type = OptionType.NUMERIC;
		}

		/**
		 * Returns the option name.
		 * @return the name string
		 */
		public String getName() { return name; }

		/**
		 * Returns the option name, as displayed in the configuration file.
		 * @return the display name string
		 */
		public String getDisplayName() { return displayName; }

		/**
		 * Returns the option description.
		 * @return the description string
		 */
		public String getDescription() { return description; }

		/**
		 * Returns the boolean value for the option, if applicable.
		 * @return the boolean value
		 */
		public boolean getBooleanValue() { return bool; }

		/**
		 * Returns the integer value for the option, if applicable.
		 * @return the integer value
		 */
		public int getIntegerValue() { return val; }

		/**
		 * Sets the boolean value for the option.
		 * @param value the new boolean value
		 */
		public void setValue(boolean value) { this.bool = value; }

		/**
		 * Sets the integer value for the option.
		 * @param value the new integer value
		 */
		public void setValue(int value) { this.val = value; }

		/**
		 * Returns the value of the option as a string (via override).
		 * <p>
		 * By default, this returns "{@code val}%" for numeric options,
		 * "Yes" or "No" based on the {@code bool} field for boolean options,
		 * and an empty string otherwise.
		 * @return the value string
		 */
		public String getValueString() {
			if (type == OptionType.NUMERIC)
				return String.format("%d%%", val);
			else if (type == OptionType.BOOLEAN)
				return (bool) ? "Yes" : "No";
			else
				return "";
		}

		/**
		 * Processes a mouse click action (via override).
		 * <p>
		 * By default, this inverts the current {@code bool} field.
		 * @param container the game container
		 */
		public void click(GameContainer container) { bool = !bool; }

		/**
		 * Processes a mouse drag action (via override).
		 * <p>
		 * By default, only if this is a numeric option, the {@code val} field
		 * will be shifted by {@code d} within the given bounds.
		 * @param container the game container
		 * @param d the dragged distance (modified by multiplier)
		 */
		public void drag(GameContainer container, int d) {
			if (type == OptionType.NUMERIC)
				val = Utils.clamp(val + d, min, max);
		}

		/**
		 * Returns the string to write to the configuration file (via override).
		 * <p>
		 * By default, this returns "{@code val}" for numeric options,
		 * "true" or "false" based on the {@code bool} field for boolean options,
		 * and {@link #getValueString()} otherwise.
		 * @return the string to write
		 */
		public String write() {
			if (type == OptionType.NUMERIC)
				return Integer.toString(val);
			else if (type == OptionType.BOOLEAN)
				return Boolean.toString(bool);
			else
				return getValueString();
		}

		/**
		 * Reads the value of the option from the configuration file (via override).
		 * <p>
		 * By default, this sets {@code val} for numeric options only if the
		 * value is between the min and max bounds, sets {@code bool} for
		 * boolean options, and does nothing otherwise.
		 * @param s the value string read from the configuration file
		 */
		public void read(String s) {
			if (type == OptionType.NUMERIC) {
				int i = Integer.parseInt(s);
				if (i >= min && i <= max)
					val = i;
			} else if (type == OptionType.BOOLEAN)
				bool = Boolean.parseBoolean(s);
		}
	};

	/** Map of option display names to GameOptions. */
	private static HashMap<String, GameOption> optionMap;

	/** Screen resolutions. */
	private enum Resolution {
		RES_800_600 (800, 600),
		RES_1024_600 (1024, 600),
		RES_1024_768 (1024, 768),
		RES_1280_720 (1280, 720),
		RES_1280_800 (1280, 800),
		RES_1280_960 (1280, 960),
		RES_1280_1024 (1280, 1024),
		RES_1366_768 (1366, 768),
		RES_1440_900 (1440, 900),
		RES_1600_900 (1600, 900),
		RES_1600_1200 (1600, 1200),
		RES_1680_1050 (1680, 1050),
		RES_1920_1080 (1920, 1080),
		RES_1920_1200 (1920, 1200),
		RES_2560_1440 (2560, 1440),
		RES_2560_1600 (2560, 1600),
		RES_3840_2160 (3840, 2160);

		/** Screen dimensions. */
		private int width, height;

		/** Enum values. */
		private static Resolution[] values = Resolution.values();

		/**
		 * Constructor.
		 * @param width the screen width
		 * @param height the screen height
		 */
		Resolution(int width, int height) {
			this.width = width;
			this.height = height;
		}

		/**
		 * Returns the screen width.
		 */
		public int getWidth() { return width; }

		/**
		 * Returns the screen height.
		 */
		public int getHeight() { return height; }

		/**
		 * Returns the next (larger) Resolution.
		 */
		public Resolution next() { return values[(this.ordinal() + 1) % values.length]; }

		@Override
		public String toString() { return String.format("%sx%s", width, height); }
	}

	/** Current screen resolution. */
	private static Resolution resolution = Resolution.RES_1024_768;

	/** Frame limiters. */
	private static final int[] targetFPS = { 60, 120, 240 };

	/** Index in targetFPS[] array. */
	private static int targetFPSindex = 0;

	/** Screenshot file formats. */
	private static String[] screenshotFormat = { "png", "jpg", "bmp" };

	/** Index in screenshotFormat[] array. */
	private static int screenshotFormatIndex = 0;

	/** Left and right game keys. */
	private static int
		keyLeft  = Keyboard.KEY_NONE,
		keyRight = Keyboard.KEY_NONE;

	// This class should not be instantiated.
	private Options() {}

	/**
	 * Returns the target frame rate.
	 * @return the target FPS
	 */
	public static int getTargetFPS() { return targetFPS[targetFPSindex]; }

	/**
	 * Sets the target frame rate to the next available option, and sends a
	 * bar notification about the action.
	 * @param container the game container
	 */
	public static void setNextFPS(GameContainer container) {
		GameOption.TARGET_FPS.click(container);
		UI.sendBarNotification(String.format("Frame limiter: %s", GameOption.TARGET_FPS.getValueString()));
	}

	/**
	 * Returns the master volume level.
	 * @return the volume [0, 1]
	 */
	public static float getMasterVolume() { return GameOption.MASTER_VOLUME.getIntegerValue() / 100f; }

	/**
	 * Sets the master volume level (if within valid range).
	 * @param container the game container
	 * @param volume the volume [0, 1]
	 */
	public static void setMasterVolume(GameContainer container, float volume) {
		if (volume >= 0f && volume <= 1f) {
			GameOption.MASTER_VOLUME.setValue((int) (volume * 100f));
			MusicController.setVolume(getMasterVolume() * getMusicVolume());
		}
	}

	/**
	 * Returns the default music volume.
	 * @return the volume [0, 1]
	 */
	public static float getMusicVolume() { return GameOption.MUSIC_VOLUME.getIntegerValue() / 100f; }

	/**
	 * Returns the default sound effect volume.
	 * @return the sound volume [0, 1]
	 */
	public static float getEffectVolume() { return GameOption.EFFECT_VOLUME.getIntegerValue() / 100f; }

	/**
	 * Returns the default hit sound volume.
	 * @return the hit sound volume [0, 1]
	 */
	public static float getHitSoundVolume() { return GameOption.HITSOUND_VOLUME.getIntegerValue() / 100f; }

	/**
	 * Returns the music offset time.
	 * @return the offset (in milliseconds)
	 */
	public static int getMusicOffset() { return GameOption.MUSIC_OFFSET.getIntegerValue(); }

	/**
	 * Returns the screenshot file format.
	 * @return the file extension ("png", "jpg", "bmp")
	 */
	public static String getScreenshotFormat() { return screenshotFormat[screenshotFormatIndex]; }

	/**
	 * Sets the container size and makes the window borderless if the container
	 * size is identical to the screen resolution.
	 * <p>
	 * If the configured resolution is larger than the screen size, the smallest
	 * available resolution will be used.
	 * @param app the game container
	 */
	public static void setDisplayMode(Container app) {
		int screenWidth = app.getScreenWidth();
		int screenHeight = app.getScreenHeight();

		// check for larger-than-screen dimensions
		if (screenWidth < resolution.getWidth() || screenHeight < resolution.getHeight())
			resolution = Resolution.RES_800_600;

		try {
			app.setDisplayMode(resolution.getWidth(), resolution.getHeight(), false);
		} catch (SlickException e) {
			ErrorHandler.error("Failed to set display mode.", e, true);
		}

		// set borderless window if dimensions match screen size
		boolean borderless = (screenWidth == resolution.getWidth() && screenHeight == resolution.getHeight());
		System.setProperty("org.lwjgl.opengl.Window.undecorated", Boolean.toString(borderless));
	}

	/**
	 * Returns whether or not the FPS counter display is enabled.
	 * @return true if enabled
	 */
	public static boolean isFPSCounterEnabled() { return GameOption.SHOW_FPS.getBooleanValue(); }

	/**
	 * Returns the port number to bind to.
	 * @return the port
	 */
	public static int getPort() { return port; }

	/**
	 * Returns the background dim level.
	 * @return the alpha level [0, 1]
	 */
	public static float getBackgroundDim() { return (100 - GameOption.BACKGROUND_DIM.getIntegerValue()) / 100f; }

	/**
	 * Returns whether or not map skins are ignored.
	 * @return true if ignored
	 */
	public static boolean isMapSkinIgnored() { return GameOption.IGNORE_MAP_SKINS.getBooleanValue(); }

	/**
	 * Returns whether or not to render loading text in the splash screen.
	 * @return true if enabled
	 */
	public static boolean isLoadVerbose() { return GameOption.LOAD_VERBOSE.getBooleanValue(); }

	/**
	 * Returns whether or not all sound effects are disabled.
	 * @return true if disabled
	 */
	public static boolean isSoundDisabled() { return GameOption.DISABLE_SOUNDS.getBooleanValue(); }

	/**
	 * Returns whether or not to play the theme song.
	 * @return true if enabled
	 */
	public static boolean isThemeSongEnabled() { return GameOption.ENABLE_THEME_SONG.getBooleanValue(); }

	/**
	 * Returns whether or not to load HD (@2x) images.
	 * @return true if HD images are enabled, false if only SD images should be loaded
	 */
	public static boolean loadHDImages() { return GameOption.LOAD_HD_IMAGES.getBooleanValue(); }

	/**
	 * Returns the left game key.
	 * @return the left key code
	 */
	public static int getGameKeyLeft() {
		if (keyLeft == Keyboard.KEY_NONE)
			setGameKeyLeft(Input.KEY_Z);
		return keyLeft;
	}

	/**
	 * Returns the right game key.
	 * @return the right key code
	 */
	public static int getGameKeyRight() {
		if (keyRight == Keyboard.KEY_NONE)
			setGameKeyRight(Input.KEY_X);
		return keyRight;
	}

	/**
	 * Sets the left game key.
	 * This will not be set to the same key as the right game key, nor to any
	 * reserved keys (see {@link #isValidGameKey(int)}).
	 * @param key the keyboard key
	 * @return {@code true} if the key was set, {@code false} if it was rejected
	 */
	public static boolean setGameKeyLeft(int key) {
		if ((key == keyRight && key != Keyboard.KEY_NONE) || !isValidGameKey(key))
			return false;
		keyLeft = key;
		return true;
	}

	/**
	 * Sets the right game key.
	 * This will not be set to the same key as the left game key, nor to any
	 * reserved keys (see {@link #isValidGameKey(int)}).
	 * @param key the keyboard key
	 * @return {@code true} if the key was set, {@code false} if it was rejected
	 */
	public static boolean setGameKeyRight(int key) {
		if ((key == keyLeft && key != Keyboard.KEY_NONE) || !isValidGameKey(key))
			return false;
		keyRight = key;
		return true;
	}

	/**
	 * Checks if the given key is a valid game key.
	 * @param key the keyboard key
	 * @return {@code true} if valid, {@code false} otherwise
	 */
	private static boolean isValidGameKey(int key) {
		return (key != Keyboard.KEY_ESCAPE && key != Keyboard.KEY_SPACE &&
		        key != Keyboard.KEY_UP && key != Keyboard.KEY_DOWN &&
		        key != Keyboard.KEY_F7 && key != Keyboard.KEY_F10 && key != Keyboard.KEY_F12);
	}

	/**
	 * Returns whether or not game failure is disabled.
	 * @return true if disabled
	 */
	public static boolean isNoFail() { return GameOption.NO_FAIL.getBooleanValue(); }

	/**
	 * Returns whether or not all loading progress is hidden.
	 * @return true if disabled
	 */
	public static boolean isLoadProgressHidden() { return GameOption.HIDE_LOADING_PROGRESS.getBooleanValue(); }

	/**
	 * Returns the map directory.
	 * If invalid, this will attempt to search for the directory,
	 * and if nothing found, will create one.
	 * @return the map directory
	 */
	public static File getMapDir() {
		if (mapDir != null && mapDir.isDirectory())
			return mapDir;

		mapDir = MAP_DIR;
		if (!mapDir.isDirectory() && !mapDir.mkdir())
			ErrorHandler.error(String.format("Failed to create map directory at '%s'.", mapDir.getAbsolutePath()), null, false);
		return mapDir;
	}

	/**
	 * Returns the screenshot directory.
	 * If invalid, this will return a "Screenshot" directory.
	 * @return the screenshot directory
	 */
	public static File getScreenshotDir() {
		if (screenshotDir != null && screenshotDir.isDirectory())
			return screenshotDir;

		screenshotDir = new File(DATA_DIR, "Screenshots/");
		return screenshotDir;
	}

	/**
	 * Returns a dummy map containing the theme song.
	 * @return the theme song map
	 */
	public static MapFile getThemeMap() {
		String[] tokens = themeString.split(",");
		if (tokens.length != 4) {
			ErrorHandler.error("Theme song string is malformed.", null, false);
			return null;
		}

		MapFile map = new MapFile(null);
		map.audioFilename = new File(tokens[0]);
		map.title = tokens[1];
		map.artist = tokens[2];

		return map;
	}

	/**
	 * Reads user options from the options file, if it exists.
	 */
	public static void parseOptions() {
		// if no config file, use default settings
		if (!OPTIONS_FILE.isFile()) {
			saveOptions();
			return;
		}

		// create option map
		if (optionMap == null) {
			optionMap = new HashMap<String, GameOption>();
			for (GameOption option : GameOption.values())
				optionMap.put(option.getDisplayName(), option);
		}

		// read file
		try (BufferedReader in = new BufferedReader(new FileReader(OPTIONS_FILE))) {
			String line;
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (line.length() < 2 || line.charAt(0) == '#')
					continue;
				int index = line.indexOf('=');
				if (index == -1)
					continue;

				// read option
				String name = line.substring(0, index).trim();
				GameOption option = optionMap.get(name);
				if (option != null) {
					try {
						String value = line.substring(index + 1).trim();
						option.read(value);
					} catch (NumberFormatException e) {
						Log.warn(String.format("Format error in options file for line: '%s'.", line), e);
					}
				}
			}
		} catch (IOException e) {
			ErrorHandler.error(String.format("Failed to read file '%s'.", OPTIONS_FILE.getAbsolutePath()), e, false);
		}
	}

	/**
	 * (Over)writes user options to a file.
	 */
	public static void saveOptions() {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(OPTIONS_FILE), "utf-8"))) {
			// header
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
			String date = dateFormat.format(new Date());
			writer.write("# windsong configuration");
			writer.newLine();
			writer.write("# last updated on ");
			writer.write(date);
			writer.newLine();
			writer.newLine();

			// options
			for (GameOption option : GameOption.values()) {
				writer.write(option.getDisplayName());
				writer.write(" = ");
				writer.write(option.write());
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			ErrorHandler.error(String.format("Failed to write to file '%s'.", OPTIONS_FILE.getAbsolutePath()), e, false);
		}
	}
}
