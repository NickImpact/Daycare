package com.nickimpact.daycare.stats;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
public class Statistics {
	private int numEggsCollected;
	private int numEggsDeleted;

	private int numGainedLevels;

	public void incrementStat(Stats stat) {
		switch (stat) {
			case EGGS_COLLECTED:
				++numEggsCollected;
				break;
			case EGGS_DELETED:
				++numEggsDeleted;
				break;
			case NUM_GAINED_LVLS:
				++numGainedLevels;
				break;
		}
	}

	public int getStat(Stats stat) {
		switch (stat) {
			case EGGS_COLLECTED:
				return numEggsCollected;
			case EGGS_DELETED:
				return numEggsDeleted;
			case NUM_GAINED_LVLS:
				return numGainedLevels;
		}

		return -1;
	}

	public double getEggCollectionRatio() {
		if(numEggsCollected + numEggsDeleted == 0) {
			return 0.0;
		}

		return ((double) numEggsCollected) / (numEggsCollected + numEggsDeleted) * 100;
	}

	public enum Stats {
		EGGS_PRODUCED,
		EGGS_COLLECTED,
		EGGS_DELETED,
		NUM_GAINED_LVLS,
	}
}
