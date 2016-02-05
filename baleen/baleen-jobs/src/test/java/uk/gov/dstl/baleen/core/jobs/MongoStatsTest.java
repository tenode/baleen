package uk.gov.dstl.baleen.core.jobs;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import uk.gov.dstl.baleen.resources.SharedFongoResource;
import uk.gov.dstl.baleen.uima.jobs.AbstractBaleenTaskTest;

public class MongoStatsTest extends AbstractBaleenTaskTest {

	private static final List<DBObject> DATA = Lists.newArrayList(
			new BasicDBObject("fake", "doc1"),
			new BasicDBObject("fake", "doc2"),
			new BasicDBObject("fake", "doc3"));

	@Test
	public void test() throws ResourceInitializationException, AnalysisEngineProcessException, IOException {
		// Due to limitations in the shared fongo resource we only test document count here!
		ExternalResourceDescription erd = ExternalResourceFactory.createExternalResourceDescription("mongo",
				SharedFongoResource.class, SharedFongoResource.PARAM_FONGO_COLLECTION, "documents",
				SharedFongoResource.PARAM_FONGO_DATA, JSON.serialize(DATA));

		File tempFile = File.createTempFile("test", "mongostats");
		try {

			AnalysisEngine task = create(MongoStats.class, "mongo", erd, "file", tempFile.getAbsolutePath());
			execute(task);

			List<String> lines = Files.readAllLines(tempFile.toPath());
			assertEquals(2, lines.size());
			assertEquals("timestamp,documents,entities,relations", lines.get(0));

			String[] split = lines.get(1).split(",");
			assertEquals("3", split[1]);
			assertEquals("0", split[2]);
			assertEquals("0", split[3]);
		} finally {
			tempFile.delete();
		}
	}

}
