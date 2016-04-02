package itdelatrisu.potato;

import java.util.ArrayList;

public class ScoreData {
	
	private class MapObject {
		private int pos;
		private int timeLeft;
		
		public MapObject(int pos, int timeLeft) {
			this.pos = pos;
			this.timeLeft = timeLeft;
		}
	}
	
	// TODO: finalize these numbers
	private final int PERFECT_TIME = 50;
	private final int GOOD_TIME = 150;
	private final int OKAY_TIME = 375;
	
	private final int PERFECT_SCORE = 100;
	private final int GOOD_SCORE = 50;
	private final int OKAY_SCORE = 20;
	private final int MISS = 0;
	
	private int score;
	private ArrayList<MapObject> toHit;
	
	public ScoreData() {
		score = 0;
		toHit = new ArrayList<MapObject>();
	}
	
	public int getScore() {
		return score;
	}
	
	public void sendMapObject(int pos, int timeLeft) {
		MapObject m = new MapObject(pos, timeLeft);
		toHit.add(m);
	}
	
	public int sendHit(int pos) {
		for ( MapObject m : toHit ) {
			if (m.pos == pos) {
				// TODO: animation graphic magic stuff
				int t = Math.abs(m.timeLeft);
				int points;
				if ( t < PERFECT_TIME )
					points = PERFECT_SCORE;
				else if ( t < GOOD_TIME )
					points = GOOD_SCORE;
				else if ( t < OKAY_TIME )
					points = OKAY_SCORE;
				else 
					points = MISS;
				
				score += points;
				toHit.remove(m);
				return score;
			}
		}
		
		return MISS;
	}
	
	public void update(int delta) {
		ArrayList<MapObject> toRemove = new ArrayList<MapObject>();
		for ( MapObject m : toHit ) {
			m.timeLeft -= delta;
			if ( m.timeLeft < -OKAY_TIME )
				toRemove.add(m);
		}
		for ( MapObject r : toRemove ) {
			toHit.remove(r);
		}
	}
	
}
