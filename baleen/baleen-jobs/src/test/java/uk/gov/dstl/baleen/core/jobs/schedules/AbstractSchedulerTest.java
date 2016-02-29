package uk.gov.dstl.baleen.core.jobs.schedules;

import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;

import uk.gov.dstl.baleen.core.jobs.BaleenScheduler;

public class AbstractSchedulerTest<T extends BaleenScheduler> {

	private final Class<T> clazz;

	public AbstractSchedulerTest(Class<T> clazz) {
		this.clazz = clazz;
	}

	public T create() throws ResourceInitializationException {
		return (T) CollectionReaderFactory.createReader(clazz);
	}

	public T create(Object... args) throws ResourceInitializationException {
		return (T) CollectionReaderFactory.createReader(clazz, args);
	}

}
