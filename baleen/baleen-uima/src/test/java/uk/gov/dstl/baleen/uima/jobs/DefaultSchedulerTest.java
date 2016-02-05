package uk.gov.dstl.baleen.uima.jobs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;

public class DefaultSchedulerTest {

	private DefaultScheduler scheduler;

	@Before
	public void before() throws ResourceInitializationException {
		scheduler = (DefaultScheduler) CollectionReaderFactory.createReader(DefaultScheduler.class);

	}

	@Test
	public void test() throws CollectionException, IOException, ResourceInitializationException {
		assertTrue(scheduler.hasNext());

		assertFalse(scheduler.hasNext());
		assertFalse(scheduler.hasNext());
	}

	@Test
	public void testLifecycle() throws IOException, UIMAException {

		assertTrue(scheduler.hasNext());

		assertTrue(scheduler.getProgress() != null);

		JCas jCas = JCasFactory.createJCas();
		scheduler.getNext(jCas);

		scheduler.destroy();

	}

}
