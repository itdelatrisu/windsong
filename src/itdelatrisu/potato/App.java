package itdelatrisu.potato;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.DefaultLogSystem;
import org.newdawn.slick.util.FileSystemLocation;
import org.newdawn.slick.util.Log;
import org.newdawn.slick.util.ResourceLoader;

import itdelatrisu.potato.states.Game;
import itdelatrisu.potato.states.GameRanking;
import itdelatrisu.potato.states.MainMenu;
import itdelatrisu.potato.states.Splash;
import itdelatrisu.potato.states.Training;

/**
 * Main class.
 */
public class App extends StateBasedGame {
	/** Game states. */
	public static final int
		STATE_SPLASH        = 0,
		STATE_MAINMENU      = 1,
		STATE_TRAINING      = 2,
		STATE_GAME          = 3,
		STATE_GAMERANKING   = 4;

	/** Server socket for restricting the program to a single instance. */
	private static ServerSocket SERVER_SOCKET;

	/**
	 * Constructor.
	 * @param name the program name
	 */
	public App(String name) {
		super(name);
	}

	@Override
	public void initStatesList(GameContainer container) throws SlickException {
		addState(new Splash(STATE_SPLASH));
		addState(new MainMenu(STATE_MAINMENU));
		addState(new Training(STATE_TRAINING));
		addState(new Game(STATE_GAME));
		addState(new GameRanking(STATE_GAMERANKING));
	}

	/**
	 * Launches the game.
	 */
	public static void main(String[] args) {
		// log all errors to a file
		Log.setVerbose(false);
		try {
			DefaultLogSystem.out = new PrintStream(new FileOutputStream(Options.LOG_FILE, true));
		} catch (FileNotFoundException e) {
			Log.error(e);
		}
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				ErrorHandler.error("** Uncaught Exception! **", e, true);
			}
		});

		// parse configuration file
		Options.parseOptions();

		// only allow a single instance
		try {
			SERVER_SOCKET = new ServerSocket(Options.getPort(), 1, InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// shouldn't happen
		} catch (IOException e) {
			ErrorHandler.error(String.format(
					"The game could not be launched for one of these reasons:\n" +
					"- An instance of the game is already running.\n" +
					"- Another program is bound to port %d. " +
					"You can change the port the game uses by editing the \"Port\" field in the configuration file.",
					Options.getPort()), null, false);
			System.exit(1);
		}

		File nativeDir;
		if (!Utils.isJarRunning() && (
		    (nativeDir = new File("./target/natives/")).isDirectory() ||
		    (nativeDir = new File("./build/natives/")).isDirectory()))
			;
		else {
			nativeDir = Options.NATIVE_DIR;
			try {
				new NativeLoader(nativeDir).loadNatives();
			} catch (IOException e) {
				Log.error("Error loading natives.", e);
			}
		}
		System.setProperty("org.lwjgl.librarypath", nativeDir.getAbsolutePath());
		System.setProperty("java.library.path", nativeDir.getAbsolutePath());
		try {
			// Workaround for "java.library.path" property being read-only.
			// http://stackoverflow.com/a/24988095
			Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			Log.warn("Failed to set 'sys_paths' field.", e);
		}

		// set the resource paths
		ResourceLoader.addResourceLocation(new FileSystemLocation(new File("./res/")));

		// start the game
		try {
			// loop until force exit
			while (true) {
				App game = new App("Potato");
				Container app = new Container(game);

				// basic game settings
				Options.setDisplayMode(app);
				String[] icons = { "icon16.png", "icon32.png" };
				app.setIcons(icons);
				app.setForceExit(true);

				app.start();
			}
		} catch (SlickException e) {
			errorAndExit(e, "An error occurred while creating the game container.");
		}
	}

	@Override
	public boolean closeRequested() {
		return true;
	}

	/**
	 * Closes all resources.
	 */
	public static void close() {
		// close server socket
		if (SERVER_SOCKET != null) {
			try {
				SERVER_SOCKET.close();
			} catch (IOException e) {
				ErrorHandler.error("Failed to close server socket.", e, false);
			}
		}
	}

	/**
	 * Throws an error and exits the application with the given message.
	 * @param e the exception that caused the crash
	 * @param message the message to display
	 */
	private static void errorAndExit(Throwable e, String message) {
		// JARs will not run properly inside directories containing '!'
		// http://bugs.java.com/view_bug.do?bug_id=4523159
		if (Utils.isJarRunning() && Utils.getRunningDirectory() != null &&
			Utils.getRunningDirectory().getAbsolutePath().indexOf('!') != -1)
			ErrorHandler.error("JARs cannot be run from some paths containing '!'. Please move or rename the file and try again.", null, false);
		else
			ErrorHandler.error(message, e, true);
		System.exit(1);
	}
}
