package itdelatrisu.potato.map;

/**
 * Data type representing a parsed hit object.
 */
public class HitObject {
	/** Hit sound types. */
	public static final int
		SOUND_NORMAL  = 0,
		SOUND_CLAP    = 1;

	/** Position. */
	private int position;

	/** Start time (in ms). */
	private int time;

	/** Hit sound type. */
	private int sound;

	/**
	 * Constructor.
	 * @param line the line to be parsed
	 */
	public HitObject(String line) {
		// Format: time,position,sound
		String tokens[] = line.split(",");
		this.time = Integer.parseInt(tokens[0]);
		this.position = Integer.parseInt(tokens[1]);
		this.sound = Integer.parseInt(tokens[2]);
	}
	
	/*
	 * Constructor
	 * @param time the sound track time at which to hit this object
	 * @param position the grid position to hit
	 * @param sound the sound to be played on hit
	 */
	public HitObject(int time, int position, int sound) {
		this.time = time;
		this.position = position;
		this.sound = sound;
	}

	/**
	 * Returns the start time.
	 * @return the start time (in ms)
	 */
	public int getTime() { return time; }

	/**
	 * Returns the position.
	 */
	public float getPosition() { return position; }

	/**
	 * Returns the hit sound type.
	 * @return the sound type
	 */
	public int getSound() { return sound; }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(time); sb.append(',');
		sb.append(position); sb.append(',');
		sb.append(sound);
		return sb.toString();
	}
}
