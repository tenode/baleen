//Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.consumers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

import uk.gov.dstl.baleen.types.common.Buzzword;
import uk.gov.dstl.baleen.types.common.Organisation;
import uk.gov.dstl.baleen.types.common.Person;
import uk.gov.dstl.baleen.types.common.Quantity;
import uk.gov.dstl.baleen.types.metadata.Metadata;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.temporal.DateType;
import uk.gov.dstl.baleen.uima.utils.UimaTypesUtils;

/**
 *
 */
public class Html5Test {
	private File outputFolder;
	private JCas jCas;

	@Before
	public void beforeTest() throws UIMAException {
		outputFolder = Files.createTempDir();
		jCas = JCasFactory.createJCas();
	}

	@After
	public void afterTest() throws IOException {
		FileUtils.deleteDirectory(outputFolder);
	}

	@Test
	public void testCreateFile() throws UIMAException {
		AnalysisEngine consumer = AnalysisEngineFactory.createEngine(Html5.class, Html5.PARAM_OUTPUT_FOLDER,
				outputFolder.getPath());

		jCas.setDocumentText("Hello World!");
		DocumentAnnotation da = (DocumentAnnotation) jCas.getDocumentAnnotationFs();
		da.setSourceUri("hello.txt");

		consumer.process(jCas);

		File f = new File(outputFolder, "hello.txt.html");
		assertTrue(f.exists());
	}

	@Test
	public void testCreateExistingFile() throws UIMAException, IOException {
		AnalysisEngine consumer = AnalysisEngineFactory.createEngine(Html5.class, Html5.PARAM_OUTPUT_FOLDER,
				outputFolder.getPath());

		jCas.setDocumentText("Hello World!");
		DocumentAnnotation da = (DocumentAnnotation) jCas.getDocumentAnnotationFs();
		da.setSourceUri("hello.txt");

		File fExisting = new File(outputFolder, "hello.txt.html");
		fExisting.createNewFile();

		consumer.process(jCas);

		File f = new File(outputFolder, "hello.txt.1.html");
		assertTrue(f.exists());
	}

	@Test
	public void testCreateExternalIdFile() throws UIMAException {
		AnalysisEngine consumer = AnalysisEngineFactory.createEngine(Html5.class, Html5.PARAM_OUTPUT_FOLDER,
				outputFolder.getPath(), Html5.PARAM_USE_EXTERNAL_ID, true, Html5.PARAM_CONTENT_HASH_AS_ID, false);

		jCas.setDocumentText("Hello World!");
		DocumentAnnotation da = (DocumentAnnotation) jCas.getDocumentAnnotationFs();
		da.setSourceUri("hello.txt");

		consumer.process(jCas);

		File f = new File(outputFolder, "7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069.html");
		assertTrue(f.exists());
	}

	@Test
	public void testCreateOutputDir() throws UIMAException {
		File newFolder = new File(outputFolder, "test");
		AnalysisEngine consumer = AnalysisEngineFactory.createEngine(Html5.class, Html5.PARAM_OUTPUT_FOLDER,
				newFolder.getPath());

		jCas.setDocumentText("Hello World!");
		DocumentAnnotation da = (DocumentAnnotation) jCas.getDocumentAnnotationFs();
		da.setSourceUri("hello.txt");

		consumer.process(jCas);

		File f = new File(newFolder, "hello.txt.html");
		assertTrue(f.exists());
	}

	@Test
	public void testCSS() throws UIMAException, IOException {
		AnalysisEngine consumer = AnalysisEngineFactory.createEngine(Html5.class, Html5.PARAM_OUTPUT_FOLDER,
				outputFolder.getPath(), Html5.PARAM_CSS, "test.css");

		jCas.setDocumentText("This is a test document.");

		consumer.process(jCas);

		DocumentAnnotation da = (DocumentAnnotation) jCas.getDocumentAnnotationFs();

		File f = new File(outputFolder, da.getHash() + ".html");
		assertTrue(f.exists());

		Document doc = Jsoup.parse(f, "UTF-8");

		Elements links = doc.select("link");
		assertEquals(1, links.size());
		Element link = links.get(0);
		assertEquals("stylesheet", link.attr("rel"));
		assertEquals("test.css", link.attr("href"));
	}

	@Test
	public void testDocument() throws UIMAException, IOException {
		AnalysisEngine consumer = AnalysisEngineFactory.createEngine(Html5.class, Html5.PARAM_OUTPUT_FOLDER,
				outputFolder.getPath());

		jCas.setDocumentText(
				"This is a test document, that contains a number of test entities.\n\nOn 30th June, this test was written by James.\nJames wrote this test on 30 June 2015.\n\n3kg of Sugar, 2kg of Spice");

		DocumentAnnotation da = (DocumentAnnotation) jCas.getDocumentAnnotationFs();
		da.setSourceUri("test.txt");

		da.setDocumentClassification("UK OFFICIAL");
		da.setDocumentCaveats(UimaTypesUtils.toArray(jCas, Arrays.asList("Test", "Caveats")));

		Metadata m1 = new Metadata(jCas);
		m1.setKey("author");
		m1.setValue("bakerj");
		m1.addToIndexes();

		Metadata m2 = new Metadata(jCas);
		m2.setKey("test-key");
		m2.setValue("test value");
		m2.addToIndexes();

		Metadata m3 = new Metadata(jCas);
		m3.setKey("documentTitle");
		m3.setValue("Test Document");
		m3.addToIndexes();

		Person p1 = new Person(jCas, 106, 111);
		p1.addToIndexes();

		Person p2 = new Person(jCas, 113, 118);
		p2.addToIndexes();

		DateType d1 = new DateType(jCas, 70, 79);
		d1.addToIndexes();

		DateType d2 = new DateType(jCas, 138, 145);
		d2.addToIndexes();

		DateType d3 = new DateType(jCas, 138, 150);
		d3.addToIndexes();

		Quantity q1 = new Quantity(jCas, 153, 156);
		q1.addToIndexes();

		Quantity q2 = new Quantity(jCas, 167, 170);
		q2.addToIndexes();

		Buzzword b1 = new Buzzword(jCas, 160, 165);
		b1.addToIndexes();

		Buzzword b2 = new Buzzword(jCas, 174, 179);
		b2.addToIndexes();

		Entity e1 = new Entity(jCas, 153, 165);
		e1.addToIndexes();

		Entity e2 = new Entity(jCas, 167, 179);
		e2.addToIndexes();

		Organisation o = new Organisation(jCas, 153, 179); // Abusing the entity type just so we can
															// differentiate in the tests
		o.addToIndexes();

		consumer.process(jCas);

		File f = new File(outputFolder, "test.txt.html");
		assertTrue(f.exists());

		Document doc = Jsoup.parse(f, "UTF-8");

		assertEquals("Test Document", doc.title());

		Elements metas = doc.select("meta");
		assertEquals(8, metas.size()); // 3 defined elements, charset, external ID, and source URI,
										// classification, caveats
		Element charset = metas.get(0);
		assertEquals("utf-8", charset.attr("charset"));
		Element sourceUri = metas.get(1);
		assertEquals("document.sourceUri", sourceUri.attr("name"));
		assertEquals("test.txt", sourceUri.attr("content"));
		Element externalId = metas.get(2);
		assertEquals("externalId", externalId.attr("name"));
		assertNotNull(sourceUri.attr("content"));
		Element classification = metas.get(3);
		assertEquals("document.classification", classification.attr("name"));
		assertEquals("UK OFFICIAL", classification.attr("content"));
		Element caveats = metas.get(4);
		assertEquals("document.caveats", caveats.attr("name"));
		assertEquals("Test,Caveats", caveats.attr("content"));
		Element meta1 = metas.get(5);
		assertEquals("author", meta1.attr("name"));
		assertEquals("bakerj", meta1.attr("content"));
		Element meta2 = metas.get(7);
		assertEquals("test-key", meta2.attr("name"));
		assertEquals("test value", meta2.attr("content"));
		Element meta3 = metas.get(6);
		assertEquals("documentTitle", meta3.attr("name"));
		assertEquals("Test Document", meta3.attr("content"));

		Elements spans = doc.select("span");
		assertEquals(12, spans.size());

		Elements people = doc.select(".Person");
		assertEquals(2, people.size());
		Element person1 = people.get(0);
		assertEquals("James", person1.text());
		assertNotNull(person1.attr("id"));
		Element person2 = people.get(1);
		assertEquals("James", person2.text());
		assertNotNull(person2.attr("id"));

		Elements datetypes = doc.select(".DateType");
		assertEquals(3, datetypes.size());
		Element datetype1 = datetypes.get(0);
		assertEquals("30th June", datetype1.text());
		assertNotNull(datetype1.attr("id"));
		Element datetype2 = datetypes.get(2);
		assertEquals("30 June", datetype2.text());
		assertNotNull(datetype2.attr("id"));
		Element datetype3 = datetypes.get(1);
		assertEquals("30 June 2015", datetype3.text());
		assertNotNull(datetype3.attr("id"));

		Elements quantities = doc.select(".Quantity");
		assertEquals(2, quantities.size());
		Element quantity1 = quantities.get(0);
		assertEquals("3kg", quantity1.text());
		assertNotNull(quantity1.attr("id"));
		Element quantity2 = quantities.get(1);
		assertEquals("2kg", quantity2.text());
		assertNotNull(quantity2.attr("id"));

		Elements buzzwords = doc.select(".Buzzword");
		assertEquals(2, buzzwords.size());
		Element buzzword1 = buzzwords.get(0);
		assertEquals("Sugar", buzzword1.text());
		assertNotNull(buzzword1.attr("id"));
		Element buzzword2 = buzzwords.get(1);
		assertEquals("Spice", buzzword2.text());
		assertNotNull(buzzword2.attr("id"));

		Elements entities = doc.select(".Entity");
		assertEquals(2, entities.size());
		Element entity1 = entities.get(0);
		assertEquals("3kg of Sugar", entity1.text());
		assertNotNull(entity1.attr("id"));
		Element entity2 = entities.get(1);
		assertEquals("2kg of Spice", entity2.text());
		assertNotNull(entity2.attr("id"));

		Elements organisations = doc.select(".Organisation");
		assertEquals(1, organisations.size());
		Element org1 = organisations.get(0);
		assertEquals("3kg of Sugar, 2kg of Spice", org1.text());
		assertNotNull(org1.attr("id"));
	}
}
