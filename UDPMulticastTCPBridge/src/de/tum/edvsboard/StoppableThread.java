package de.tum.edvsboard;

public class StoppableThread extends Thread {
	protected boolean stopRequested;

	public boolean isStopRequested() {
		return stopRequested;
	}

	public void stopRequested() {
		this.stopRequested = true;
		interrupt();
	}
}
