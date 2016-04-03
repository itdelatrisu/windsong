package itdelatrisu.windsong.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.newdawn.slick.util.Log;

import itdelatrisu.windsong.ErrorHandler;
import itdelatrisu.windsong.Options;

/**
 * Parser for maps.
 */
public class MapParser {
	/** List of all parsed maps. */
	private static List<MapFile> mapList = new ArrayList<MapFile>();

	/** Returns the list of maps. */
	public static List<MapFile> getMaps() { return mapList; }

	/** The string lookup database. */
	private static HashMap<String, String> stringdb = new HashMap<String, String>();

	/** The current file being parsed. */
	private static File currentFile;

	/** The current directory number while parsing. */
	private static int currentDirectoryIndex = -1;

	/** The total number of directories to parse. */
	private static int totalDirectories = -1;

	// This class should not be instantiated.
	private MapParser() {}

	/**
	 * Invokes parser for each map file in a root directory.
	 * @param root the root directory (search has depth 1)
	 */
	public static void parseAllFiles(File root) {
		// create a new map list
		mapList = new ArrayList<MapFile>();

		// parse all directories
		parseDirectories(root.listFiles());
	}

	/**
	 * Invokes parser for each directory in the given array and
	 * adds the maps to the existing map list.
	 * @param dirs the array of directories to parse
	 */
	public static void parseDirectories(File[] dirs) {
		if (dirs == null)
			return;

		// progress tracking
		currentDirectoryIndex = 0;
		totalDirectories = dirs.length;

		// parse directories
		for (File dir : dirs) {
			currentDirectoryIndex++;
			if (!dir.isDirectory())
				continue;

			// find all map files
			File[] files = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(Options.MAP_FILE_EXT);
				}
			});
			if (files == null || files.length < 1)
				continue;

			// create a new group entry
			for (File file : files) {
				currentFile = file;
				MapFile map = parseFile(file, dir);
				if (map != null)
					mapList.add(map);
			}

			// stop parsing files (interrupted)
			if (Thread.interrupted())
				break;
		}

		// sort list
		Collections.sort(mapList);

		// clear string DB
		stringdb = new HashMap<String, String>();

		currentFile = null;
		currentDirectoryIndex = -1;
		totalDirectories = -1;
	}

	/**
	 * Parses a map.
	 * @param file the file to parse
	 * @param dir the directory containing the map
	 * @return the new map
	 */
	private static MapFile parseFile(File file, File dir) {
		MapFile map = new MapFile(file);
		try (BufferedReader in = new BufferedReader(new FileReader(file))) {
			String line = in.readLine();
			String tokens[] = null;
			while (line != null) {
				line = line.trim();
				if (!isValidLine(line)) {
					line = in.readLine();
					continue;
				}
				switch (line) {
				case "[Metadata]":
					while ((line = in.readLine()) != null) {
						line = line.trim();
						if (!isValidLine(line))
							continue;
						if (line.charAt(0) == '[')
							break;
						if ((tokens = tokenize(line)) == null)
							continue;
						try {
							switch (tokens[0]) {
							case "AudioFilename":
								File audioFileName = new File(dir, tokens[1]);
								if (!audioFileName.isFile()) {
									// try to find the file with a case-insensitive match
									boolean match = false;
									for (String s : dir.list()) {
										if (s.equalsIgnoreCase(tokens[1])) {
											audioFileName = new File(dir, s);
											match = true;
											break;
										}
									}
									if (!match) {
										Log.error(String.format("Audio file '%s' not found in directory '%s'.", tokens[1], dir.getName()));
										return null;
									}
								}
								map.audioFilename = audioFileName;
								break;
							case "Title":
								map.title = getDBString(tokens[1]);
								break;
							case "Artist":
								map.artist = getDBString(tokens[1]);
								break;
							case "Creator":
								map.creator = getDBString(tokens[1]);
								break;
							case "Difficulty":
								map.difficulty = Integer.parseInt(tokens[1]);
								break;
							}
						} catch (Exception e) {
							Log.warn(String.format("Failed to read metadata '%s' for file '%s'.",
									line, file.getAbsolutePath()), e);
						}
					}
					break;
				case "[HitObjects]":
					List<HitObject> hitObjects = new ArrayList<HitObject>();
					while ((line = in.readLine()) != null) {
						line = line.trim();
						if (!isValidLine(line))
							continue;
						if (line.charAt(0) == '[')
							break;
						try {
							hitObjects.add(new HitObject(line));
						} catch (Exception e) {
							Log.warn(String.format("Failed to read hit object '%s' for file '%s'.",
									line, file.getAbsolutePath()), e);
						}
					}
					map.objects = hitObjects.toArray(new HitObject[hitObjects.size()]);
					break;
				default:
					line = in.readLine();
					break;
				}
			}
		} catch (IOException e) {
			ErrorHandler.error(String.format("Failed to read file '%s'.", file.getAbsolutePath()), e, false);
		}

		// sanity checks
		if (map.audioFilename == null)
			return null;
		if (map.objects == null) {
			Log.warn(String.format("No hit objects found in MapFile '%s'.", map.toString()));
			return null;
		}

		return map;
	}

	/**
	 * Returns false if the line is too short or commented.
	 */
	private static boolean isValidLine(String line) {
		return (line.length() > 1 && !line.startsWith("//"));
	}

	/**
	 * Splits line into two strings: tag, value.
	 * If no ':' character is present, null will be returned.
	 */
	private static String[] tokenize(String line) {
		int index = line.indexOf(':');
		if (index == -1) {
			Log.debug(String.format("Failed to tokenize line: '%s'.", line));
			return null;
		}

		String[] tokens = new String[2];
		tokens[0] = line.substring(0, index).trim();
		tokens[1] = line.substring(index + 1).trim();
		return tokens;
	}

	/**
	 * Returns the name of the current file being parsed, or null if none.
	 */
	public static String getCurrentFileName() {
		return (currentFile != null) ? currentFile.getName() : null;
	}

	/**
	 * Returns the progress of file parsing, or -1 if not parsing.
	 * @return the completion percent [0, 100] or -1
	 */
	public static int getParserProgress() {
		if (currentDirectoryIndex == -1 || totalDirectories == -1)
			return -1;

		return currentDirectoryIndex * 100 / totalDirectories;
	}

	/**
	 * Returns the String object in the database for the given String.
	 * If none, insert the String into the database and return the original String.
	 * @param s the string to retrieve
	 * @return the string object
	 */
	public static String getDBString(String s) {
		String DBString = stringdb.get(s);
		if (DBString == null) {
			stringdb.put(s, s);
			return s;
		} else
			return DBString;
	}
}