package uk.gov.dstl.baleen.core.jobs.schedules;

import uk.gov.dstl.baleen.core.jobs.BaleenScheduler;

/**
 * A scheduler which will run a job once, and then stop it.
 */
public class Once extends BaleenScheduler {

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
