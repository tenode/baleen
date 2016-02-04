//Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.cpe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.dstl.baleen.core.history.BaleenHistory;
import uk.gov.dstl.baleen.core.history.logging.LoggingBaleenHistory;
import uk.gov.dstl.baleen.exceptions.BaleenException;

/**
 * This class provides methods to convert a Baleen YAML configuration file into a
 * {@link org.apache.uima.collection.CollectionProcessingEngine} that can be executed by Baleen.
 * <p>
 * The YAML configuration file should contain a single <i>collectionreader</i> 'object' and a list
 * of <i>annotators</i> and <i>consumers</i> objects. Each analysis engine should have a
 * <i>class</i> property, which refers to the class of the annotator. If the class cannot be found
 * as specified, then the default Baleen package for that type is searched instead (e.g.
 * uk.gov.dstl.baleen.annotators). If an collection reader, annotator or consumer has no properties
 * then the class property prefix is optional (i.e. the list item can consist solely of the
 * annotator class).
 * <p>
 * Any additional properties on the analysis engine are passed as Params to the analysis engine.
 * Additionally, any top level objects that aren't expected are assumed to be global parameters that
 * are passed to all analysis engines. Where locally specified parameters have the same name as
 * global ones, the local versions take precedent.
 * <p>
 * Analysis engines are added to the CPE in the same order that they are listed, with annotators
 * listed before consumers.
 * <p>
 * For example:
 *
 * <pre>
 * shape:
 *   color: red
 *   size: large
 *
 * # See comments below
 * history:
 *   class: uk.gov.dstl.baleen.core.history.memory.InMemoryBaleenHistory
 *   mergeDistinctEntities: true
 *
 * collectionreader:
 *   class: DummyReader
 *   inputdirectory: \data\input
 *
 * annotators:
 *   - DummyAnnotator1
 *   - class: DummyAnnotatorWithParams
 *     min: 20
 *     max: 200
 *   - DummyAnnotator2
 *
 * consumers:
 *   - class: DummyConsumer
 *     shape.color: green
 * </pre>
 *
 * Here, the pipeline would run as follows with provided parameters listed in brackets:
 * <ul>
 * <li>DummyReader (shape.color: red, shape.size: large, inputdirectory: \data\input)</li>
 * <li>DummyAnnotator1 (shape.color: red, shape.size: large)</li>
 * <li>DummyAnnotatorWithParams (shape.color: red, shape.size: large, min: 20, max: 20)</li>
 * <li>DummyAnnotator2 (shape.color: red, shape.size: large)</li>
 * <li>DummyConsumer (shape.color: green, shape.size: large)</li>
 * </ul>
 * <p>
 * Resources are automatically detected (assuming the analysis engine has used the @ExternalResource
 * annotation) and created. Resources should use global parameters (e.g. shape.color in the above
 * example) to initialise themselves, as these are the only ones that will be passed to them.
 * <p>
 * The following history configuration parameters can be provided:
 * <ul>
 * <li>history.class - Defaults to uk.gov.dstl.baleen.core.history.logging.LoggingBaleenHistory
 * <br />
 * Provide the implementation class of the Baleen history components, which will collect the change
 * events for entities and documents.</li>
 *
 * <li>history.mergeDistinctEntities - Defaults to false.<br />
 * This determines if entities with different referent targets will be merged (true). If set to
 * false then even if two entities are requested to be merged the request will be ignored if they
 * have different referent targets. False is the safe default for loss of entities, but the right
 * value will depend on the pipeline annotator. This setting can be used at the global level or on
 * individual annotators.</li>
 * </ul>
 *
 *
 */
@SuppressWarnings("unchecked")
public class CpeBuilder extends AbstractCpeBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(CpeBuilder.class);

	public static final String BALEEN_HISTORY = "__baleenHistory";

	public static final String MERGE_DISTINCT_ENTITIES = "history.mergeDistinctEntities";

	/**
	 * Initiate a CpeBuilder with a YAML configuration file
	 *
	 * @param pipelineName
	 *            The name of the pipeline
	 * @param yamlFile
	 *            The file containing the configuration
	 * @throws IOException
	 */
	public CpeBuilder(String pipelineName, File yamlFile) throws BaleenException {
		super(pipelineName, yamlFile);
	}

	/**
	 * Initiate a CpeBuilder with an input stream
	 *
	 * @param pipelineName
	 *            The name of the pipeline
	 * @param inputStream
	 *            The input stream containing the YAML configuration
	 * @throws IOException
	 *             if the input stream can not be read
	 */
	public CpeBuilder(String pipelineName, InputStream inputStream) throws BaleenException {
		super(pipelineName, inputStream);
	}

	/**
	 * Initiate a CpeBuilder with a YAML string
	 *
	 * @param pipelineName
	 *            The name of the pipeline
	 * @param yamlString
	 *            A string containing the configuration
	 */
	public CpeBuilder(String pipelineName, String yamlString) throws BaleenException {
		super(pipelineName, yamlString);
	}

	@Override
	protected void configure(String pipelineName, Map<String, Object> config) throws BaleenException {
		configureHistory();

		Object cr = config.remove("collectionreader");
		if (cr != null) {
			createCollectionReader(cr);
		}

		Object a = config.remove("annotators");
		if (a != null && a instanceof List) {
			createAnnotators((List<Object>) a);
		}

		Object c = config.remove("consumers");
		if (c != null && c instanceof List) {
			createConsumers((List<Object>) c);
		}

	}

	/**
	 * Create a CollectionReaderDescription using the current configuration
	 *
	 * @return A configured CollectionReaderDescription
	 */
	private void createCollectionReader(Object collectionReaderConfig) throws BaleenException {
		String crClassName = null;
		Map<String, Object> params = Collections.emptyMap();

		if (collectionReaderConfig instanceof String) {
			crClassName = (String) collectionReaderConfig;
		} else if (collectionReaderConfig instanceof Map) {
			Map<String, Object> reader = (Map<String, Object>) collectionReaderConfig;
			crClassName = (String) reader.get(CLASS);
			params = reader;
		}

		// This is also done by the createCollectionReader but this is backward compatible with the
		// tests

		if (crClassName == null || crClassName.isEmpty()) {
			throw new BaleenException("No class specified for Collection Reader, or unable to parse");
		}

		Optional<CollectionReaderDescription> desc = createCollectionReader(crClassName, params);
		if (desc.isPresent()) {
			setCollectorReader(desc.get());
		} else {
			// Whilst this would be caught by the build() process throwing an exception here is
			// compatible with existing Baleen tests.
			throw new BaleenException(String.format("Could not find or instantiate analysis engine %s", crClassName));
		}
	}

	/**
	 * Create a map of AnalysisEngineDescriptions for the annotators using the current configuration
	 *
	 * @return A map containing the Annotator Name as the key and the AnalysisEngineDescription as
	 *         the value
	 */
	private void createAnnotators(List<Object> annotatorsConfig) {
		for (Object objAnnotator : annotatorsConfig) {
			String aClassName = null;
			Map<String, Object> params = Collections.emptyMap();

			if (objAnnotator instanceof String) {
				aClassName = (String) objAnnotator;
			} else if (objAnnotator instanceof Map) {
				Map<String, Object> annotator = (Map<String, Object>) objAnnotator;
				aClassName = (String) annotator.get(CLASS);
				params = annotator;
			}

			Optional<AnalysisEngineDescription> desc = createAnnotator(aClassName, params);
			if (desc.isPresent()) {
				String name = CpeBuilderUtils.getComponentName(getAnnotatorNames(), "annotator:" + aClassName);
				addAnnotator(name, desc.get());
			}

		}

	}

	/**
	 * Create a map of AnalysisEngineDescriptions for the consumers using the current configuration
	 *
	 * @return A map containing the Consumer Name as the key and the AnalysisEngineDescription as
	 *         the value
	 */
	private void createConsumers(List<Object> consumersConfig) {
		for (Object objConsumer : consumersConfig) {
			String cClassName = null;
			Map<String, Object> params = Collections.emptyMap();

			if (objConsumer instanceof String) {
				cClassName = (String) objConsumer;
			} else if (objConsumer instanceof Map) {
				Map<String, Object> consumer = (Map<String, Object>) objConsumer;
				cClassName = (String) consumer.get(CLASS);
				params = consumer;
			}

			Optional<AnalysisEngineDescription> desc = createConsumer(cClassName, params);
			if (desc.isPresent()) {
				String name = CpeBuilderUtils.getComponentName(getConsumerNames(), "consumer:" + cClassName);
				addConsumer(name, desc.get());
			}

		}
	}

	private void configureHistory() {
		String historyClass = (String) getGlobalConfig("history.class");

		Class<? extends BaleenHistory> clazz = null;

		if (historyClass != null) {
			try {
				clazz = (Class<? extends BaleenHistory>) Class.forName(historyClass);
			} catch (ClassNotFoundException | ClassCastException e) {
				LOGGER.warn("Unable to find perferred history implementation {}", historyClass, e);
			}
		} else {
			LOGGER.warn("No history implementation specified");
		}

		if (clazz == null) {
			clazz = LoggingBaleenHistory.class;
			LOGGER.info("Using the default history implementation {}", clazz.getCanonicalName());
		}

		Object[] params = CpeBuilderUtils.extractParams(getGlobalConfig(), getIgnoreParams(),
				getOrCreateResources(clazz));
		ExternalResourceDescription erd = ExternalResourceFactory.createExternalResourceDescription(BALEEN_HISTORY,
				clazz, params);
		addResource(BALEEN_HISTORY, erd);
	}

}
