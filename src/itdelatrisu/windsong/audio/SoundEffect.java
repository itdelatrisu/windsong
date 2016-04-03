package itdelatrisu.windsong.audio;

/**
 * Sound effects.
 */
public enum SoundEffect implements SoundController.SoundComponent {
	APPLAUSE ("applause"),
	COMBOBREAK ("combobreak"),
	FAIL ("failsound"),
	HIT_CLAP ("hitclap"),
	HIT_NORMAL ("hitnormal"),
	MENUBACK ("menuback"),
	MENUCLICK ("menuclick"),
	MENUHIT ("menuhit"),
	SHUTTER ("shutter");

	/** The file name. */
	private final String filename;

	/** The Clip associated with the sound effect. */
	private MultiClip clip;

	/** Total number of sound effects. */
	public static final int SIZE = values().length;

	/**
	 * Constructor.
	 * @param filename the sound file name
	 */
	SoundEffect(String filename) {
		this.filename = filename;
	}

	/**
	 * Returns the file name.
	 * @return the file name
	 */
	public String getFileName() { return filename; }

	@Override
	public MultiClip getClip() { return clip; }

	/**
	 * Sets the Clip for the sound.
	 * @param clip the clip
	 */
	public void setClip(MultiClip clip) { this.clip = clip; }
}
