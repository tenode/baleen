package uk.gov.dstl.baleen.uima.jobs.dummy;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import uk.gov.dstl.baleen.uima.jobs.BaleenTask;
import uk.gov.dstl.baleen.uima.jobs.JobSettings;

public class CounterTask extends BaleenTask {

	private static int executedCount = 0;

	private static JobSettings lastSettings = null;

	@Override
	protected void execute(JobSettings settings) throws AnalysisEngineProcessException {
		executedCount++;

		// NOTE: You should never hold onto this and expect that it'll be changed / static / etc
		// Uima might to anything to this or the underlying JCAS
		// We just want this for checking the tests
		lastSettings = settings;
	}

	public static JobSettings getLastSettings() {
		return lastSettings;
	}

	public static int getExecutedCount() {
		return executedCount;
	}

	public static void reset() {
		lastSettings = null;
		executedCount = 0;
	}

}
