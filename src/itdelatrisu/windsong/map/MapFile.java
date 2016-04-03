package itdelatrisu.windsong.map;

import java.io.File;

/**
 * Structure storing data parsed from map files.
 */
public class MapFile implements Comparable<MapFile> {
	/** The File object associated with this map. */
	private File file;

	/** Audio file object. */
	public File audioFilename;

	/** Song title. */
	public String title = "";

	/** Song artist. */
	public String artist = "";

	/** Mmap creator. */
	public String creator = "";

	/** Map difficulty. */
	public int difficulty = 1;

	/** All hit objects. */
	public HitObject[] objects;

	/**
	 * Constructor.
	 * @param file the file associated with this map
	 */
	public MapFile(File file) {
		this.file = file;
	}

	/**
	 * Returns the associated file object.
	 * @return the File object
	 */
	public File getFile() { return file; }

	/**
	 * Returns the track end time (i.e. last hit object time).
	 * @return the time, in ms
	 */
	public int getEndTime() { return objects[objects.length - 1].getTime(); }

	/**
	 * Returns the difficulty as a string.
	 * @return the difficulty
	 */
	public String getDifficulty() {
		if      (difficulty <= 3) return "Easy";
		else if (difficulty <= 5) return "Standard";
		else if (difficulty <= 7) return "Difficulty";
		else                      return "Insane";
	}

	/**
	 * Compares two MapFile objects based on difficulty.
	 */
	@Override
	public int compareTo(MapFile that) { return Integer.compare(this.difficulty, that.difficulty); }

	/**
	 * Returns a formatted string: "Artist - Title [Version]"
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s - %s [%s]", artist, title, creator);
	}
}