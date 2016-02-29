package uk.gov.dstl.baleen.uima.jobs;

import uk.gov.dstl.baleen.uima.jobs.BaleenScheduler;

/**
 * A scheduler which will run a job once, and then stop it.
 */
public class DefaultScheduler extends BaleenScheduler {

	private boolean hasRun = false;

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
