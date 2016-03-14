package uk.gov.dstl.baleen.uima.jobs;

// TODO: Auto-generated Javadoc
/**
 * A scheduler which will run a job once, and then stop it.
 */
public class DefaultScheduler extends BaleenScheduler {

	/** The has run. */
	private boolean hasRun = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.gov.dstl.baleen.uima.jobs.BaleenScheduler#await()
	 */
	@Override
	protected boolean await() {
		if (hasRun) {
			return false;
		} else {
			hasRun = true;
			return true;
		}
	}

}
