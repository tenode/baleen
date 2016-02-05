package uk.gov.dstl.baleen.core.jobs;

import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import uk.gov.dstl.baleen.core.jobs.dummy.CounterTask;
import uk.gov.dstl.baleen.core.jobs.dummy.DummyTaskWithParams;

public class BaleenTaskTest extends AbstractBaleenTaskTest {

	@Test
	public void test() throws ResourceInitializationException, AnalysisEngineProcessException {
		AnalysisEngine task = create(CounterTask.class);
		assertEquals(0, CounterTask.getExecutedCount());
		execute(task);
		assertEquals(1, CounterTask.getExecutedCount());
		task.destroy();
	}

	@Test
	public void testWithDefaultParams() throws ResourceInitializationException, AnalysisEngineProcessException {
		AnalysisEngine task = create(DummyTaskWithParams.class);
		JobSettings settings = execute(task);
		assertEquals("value", settings.get("key").get());
	}

	@Test
	public void testWithParams() throws ResourceInitializationException, AnalysisEngineProcessException {
		AnalysisEngine task = create(DummyTaskWithParams.class, "value", "different");
		JobSettings settings = execute(task);
		assertEquals("different", settings.get("key").get());
	}

}
