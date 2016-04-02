package itdelatrisu.potato.map;

import java.io.File;

/**
 * Structure storing data parsed from map files.
 */
public class PotatoMap implements Comparable<PotatoMap> {
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
	public PotatoMap(File file) {
		this.file = file;
	}

	/**
	 * Returns the associated file object.
	 * @return the File object
	 */
	public File getFile() { return file; }

	/**
	 * Compares two PotatoMap objects based on difficulty.
	 */
	@Override
	public int compareTo(PotatoMap that) { return Integer.compare(this.difficulty, that.difficulty); }

	/**
	 * Returns a formatted string: "Artist - Title [Version]"
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s - %s [%s]", artist, title, creator);
	}
}