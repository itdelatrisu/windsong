package itdelatrisu.potato;

import itdelatrisu.potato.map.HitObject;

import java.util.ArrayList;

public class ScoreData {
		
	// TODO: finalize these numbers
	private static final int PERFECT_TIME = 50;
	private static final int GOOD_TIME = 150;
	public static final int OKAY_TIME = 375;
	
	private static final int PERFECT_SCORE = 100;
	private static final int GOOD_SCORE = 50;
	private static final int OKAY_SCORE = 20;
	private static final int MISS = 0;
	
	public static final int HIT_OBJECT_FADEIN_TIME = 750;
	
	private int score;
	private ArrayList<HitObject> toHit;
	
	public ScoreData() {
		score = 0;
		toHit = new ArrayList<HitObject>();
	}
	
	public int getScore() {
		return score;
	}
	
	public void sendMapObject(HitObject h) {
		toHit.add(h);
	}
	
	public int sendHit(int pos, int time) {
		for (HitObject h : toHit) {
			if (h.getPosition() == pos) {
				// TODO: animation graphic magic stuff
				int t = Math.abs(h.getTime() - time);
				int points;
				if (t < PERFECT_TIME)
					points = PERFECT_SCORE;
				else if (t < GOOD_TIME)
					points = GOOD_SCORE;
				else if (t < OKAY_TIME)
					points = OKAY_SCORE;
				else
					points = MISS;

				score += points;
				toHit.remove(h);
				return score;
			}
		}

		return MISS;
	}

	public void update(int delta, int time) {
		ArrayList<HitObject> toRemove = new ArrayList<HitObject>();
		for (HitObject h : toHit) {
			if (h.getTime() - time < -OKAY_TIME)
				toRemove.add(h);
		}
		for (HitObject r : toRemove) {
			toHit.remove(r);
		}
	}
	
}