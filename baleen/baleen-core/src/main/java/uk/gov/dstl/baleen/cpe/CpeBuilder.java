//Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.cpe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionProcessingEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.Resource;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.google.common.io.Files;

import uk.gov.dstl.baleen.core.history.BaleenHistory;
import uk.gov.dstl.baleen.core.history.logging.LoggingBaleenHistory;
import uk.gov.dstl.baleen.core.utils.YamlConfiguration;
import uk.gov.dstl.baleen.exceptions.BaleenException;
import uk.gov.dstl.baleen.exceptions.MissingParameterException;

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
public class CpeBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(CpeBuilder.class);

	private static final String CLASS = "class";
	public static final String PIPELINE_NAME = "__pipelineName";
	public static final String BALEEN_HISTORY = "__baleenHistory";

	public static final String MERGE_DISTINCT_ENTITIES = "history.mergeDistinctEntities";

	public static final String ANNOTATOR_DEFAULT_PACKAGE = "uk.gov.dstl.baleen.annotators";
	public static final String CONSUMER_DEFAULT_PACKAGE = "uk.gov.dstl.baleen.consumers";
	public static final String READER_DEFAULT_PACKAGE = "uk.gov.dstl.baleen.collectionreaders";

	private final Map<String, Object> globalConfig = new HashMap<>();
	private Object collectionReaderConfig = new HashMap<>();
	private List<Object> annotatorsConfig = new ArrayList<>();
	private List<Object> consumersConfig = new ArrayList<>();

	private final Map<String, ExternalResourceDescription> resourceDescriptors = new HashMap<>();

	private final List<String> ignoreParams = new ArrayList<>(Arrays.asList(CLASS));

	private CollectionProcessingEngine cpe = null;

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
		try {
			createPipeline(pipelineName, Files.toString(yamlFile, StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new BaleenException(e);
		}
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
		try {
			createPipeline(pipelineName, IOUtils.toString(inputStream));
		} catch (IOException e) {
			throw new BaleenException(e);
		}
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
		createPipeline(pipelineName, yamlString);
	}

	/**
	 * Create a pipeline from YAML string
	 *
	 * @param pipelineName
	 *            the pipeline name
	 * @param yamlString
	 *            the YAML containing configuration
	 * @throws BaleenException
	 */
	private void createPipeline(String pipelineName, String yamlString)
			throws BaleenException {
		Yaml yaml = new Yaml();
		String cleanYaml = YamlConfiguration.cleanTabs(yamlString);
		Map<String, Object> config = (Map<String, Object>) yaml.load(cleanYaml);
		createPipeline(pipelineName, config);
	}

	/**
	 * Get the built CPE
	 *
	 * @return The CollectionProcessingEngine that has been configured based on the current
	 *         configuration
	 */
	public CollectionProcessingEngine getCPE() {
		return cpe;
	}

	/**
	 * Load YAML file into a series of Maps for the different components
	 *
	 * @param config
	 *            The map, loaded from a YAML file, to extract into the separate variables
	 */
	private void yamlToMaps(Map<String, Object> config) throws MissingParameterException {
		if (config == null) {
			throw new MissingParameterException("No configuration provided");
		}

		Object cr = config.remove("collectionreader");
		if (cr == null) {
			throw new MissingParameterException("No collection reader specified");
		} else {
			collectionReaderConfig = cr;
		}

		Object a = config.remove("annotators");
		if (a != null) {
			annotatorsConfig = (List<Object>) a;
		}

		Object c = config.remove("consumers");
		if (c != null) {
			consumersConfig = (List<Object>) c;
		}

		for (String key : config.keySet()) {
			// Flatten global configuration
			Map<String, Object> subconfig = (Map<String, Object>) config.get(key);
			for (String subkey : subconfig.keySet()) {
				globalConfig.put(key + "." + subkey, subconfig.get(subkey));
			}
		}
	}

	/**
	 * Create a CollectionReaderDescription using the current configuration
	 *
	 * @return A configured CollectionReaderDescription
	 */
	private CollectionReaderDescription createCollectionReader() throws BaleenException {
		String crClassName = null;
		Map<String, Object> params = Collections.emptyMap();

		if (collectionReaderConfig instanceof String) {
			crClassName = (String) collectionReaderConfig;
		} else if (collectionReaderConfig instanceof Map) {
			Map<String, Object> reader = (Map<String, Object>) collectionReaderConfig;
			crClassName = (String) reader.get(CLASS);
			params = reader;
		}

		if (crClassName == null || crClassName.isEmpty()) {
			throw new MissingParameterException("No class specified for Collection Reader, or unable to parse");
		}

		Class<? extends CollectionReader> crClass = CpeBuilderUtils.getClassFromString(crClassName,
				READER_DEFAULT_PACKAGE);
		Map<String, ExternalResourceDescription> crResources = getResources(crClass);
		Object[] crParams = CpeBuilderUtils.mergeAndExtractParams(globalConfig, params, ignoreParams, crResources);

		try {
			return CollectionReaderFactory.createReaderDescription(crClass, crParams);
		} catch (ResourceInitializationException rie) {
			throw new BaleenException("Couldn't initialize collection reader", rie);
		}
	}

	/**
	 * Create a map of AnalysisEngineDescriptions for the annotators using the current configuration
	 *
	 * @return A map containing the Annotator Name as the key and the AnalysisEngineDescription as
	 *         the value
	 */
	private Map<String, AnalysisEngineDescription> createAnnotators() {
		Map<String, AnalysisEngineDescription> annotators = new LinkedHashMap<String, AnalysisEngineDescription>();

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

			if (aClassName == null || aClassName.isEmpty()) {
				LOGGER.warn(
						"No class name provided for annotator, or unable to parse list item - analysis engine will be skipped");
				continue;
			}

			try {
				Class<? extends AnalysisComponent> aClass = CpeBuilderUtils.getClassFromString(aClassName,
						ANNOTATOR_DEFAULT_PACKAGE);
				Map<String, ExternalResourceDescription> aResources = getResources(aClass);
				Object[] aParams = CpeBuilderUtils.mergeAndExtractParams(globalConfig, params, ignoreParams,
						aResources);

				AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(aClass, aParams);
				String name = CpeBuilderUtils.getComponentName(annotators.keySet(), "annotator:" + aClassName);

				annotators.put(name, ae);
			} catch (BaleenException | ResourceInitializationException e) {
				LOGGER.warn("Failed to build annotator description - analysis engine will be skipped", e);
			}
		}

		return annotators;
	}

	/**
	 * Create a map of AnalysisEngineDescriptions for the consumers using the current configuration
	 *
	 * @return A map containing the Consumer Name as the key and the AnalysisEngineDescription as
	 *         the value
	 */
	private Map<String, AnalysisEngineDescription> createConsumers() {
		Map<String, AnalysisEngineDescription> consumers = new LinkedHashMap<String, AnalysisEngineDescription>();

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

			if (cClassName == null || cClassName.isEmpty()) {
				LOGGER.warn(
						"No class name provided for consumer, or unable to parse list item - analysis engine will be skipped");
				continue;
			}

			try {
				Class<? extends AnalysisComponent> cClass = CpeBuilderUtils.getClassFromString(cClassName,
						CONSUMER_DEFAULT_PACKAGE);
				Map<String, ExternalResourceDescription> cResources = getResources(cClass);
				Object[] cParams = CpeBuilderUtils.mergeAndExtractParams(globalConfig, params, ignoreParams,
						cResources);

				AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(cClass, cParams);
				String name = CpeBuilderUtils.getComponentName(consumers.keySet(), "consumer:" + cClassName);

				consumers.put(name, ae);
			} catch (BaleenException | ResourceInitializationException e) {
				LOGGER.warn("Failed to build consumer description - analysis engine will be skipped", e);
			}
		}

		return consumers;
	}

	/**
	 * Take the components previously created and create a single pipeline from them
	 *
	 * @param collectionReader
	 *            Collection reader descriptor
	 * @param analysisEngines
	 *            Map of analysis engine names and descriptors
	 * @return A CollectionProcessingEngine containing the specified components
	 */
	private CollectionProcessingEngine buildCPE(CollectionReaderDescription collectionReader,
			Map<String, AnalysisEngineDescription> analysisEngines) throws BaleenException {
		// Build aggregate engine to contain all annotators
		AnalysisEngineDescription cpeAEs = null;
		try {
			List<String> names = new ArrayList<>();
			names.addAll(analysisEngines.keySet());

			List<AnalysisEngineDescription> engines = new ArrayList<>();
			engines.addAll(analysisEngines.values());

			cpeAEs = AnalysisEngineFactory.createEngineDescription(engines, names, null, null, null);
		} catch (ResourceInitializationException rie) {
			throw new BaleenException("Couldn't create aggregate analysis engine", rie);
		}

		// Build Collection Processing Engine
		org.apache.uima.fit.cpe.CpeBuilder builder = new org.apache.uima.fit.cpe.CpeBuilder();

		try {
			builder.setReader(collectionReader);
			builder.setAnalysisEngine(cpeAEs);
		} catch (Exception e) {
			throw new BaleenException("Couldn't build Collection Processing Engine", e);
		}

		CollectionProcessingEngine ret;
		try {
			ret = builder.createCpe(null);
		} catch (Exception e) {
			throw new BaleenException("Couldn't create CPE", e);
		}

		return ret;
	}

	/**
	 * Create pipeline from a configuration map
	 *
	 * @param pipelineName
	 *            The name of the pipeline
	 * @param config
	 *            A map of configuration keys to objects
	 */
	private void createPipeline(String pipelineName, Map<String, Object> config) throws BaleenException {
		// Load configuration
		yamlToMaps(config);
		globalConfig.put(PIPELINE_NAME, pipelineName);

		configureHistory();

		// Create CollectionReader
		CollectionReaderDescription collectionReader = createCollectionReader();

		// Create Annotators
		Map<String, AnalysisEngineDescription> annotators = createAnnotators();

		// Create Consumers
		Map<String, AnalysisEngineDescription> consumers = createConsumers();

		Map<String, AnalysisEngineDescription> analysisEngines = new LinkedHashMap<>();
		analysisEngines.putAll(annotators);
		analysisEngines.putAll(consumers);

		if (analysisEngines.isEmpty()) {
			throw new BaleenException("You must have at least one valid annotator or consumer");
		}

		cpe = buildCPE(collectionReader, analysisEngines);
	}

	private void configureHistory() {
		String historyClass = (String) globalConfig.get("history.class");

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

		Object[] params = CpeBuilderUtils.extractParams(globalConfig, ignoreParams, getResources(clazz));
		ExternalResourceDescription erd = ExternalResourceFactory.createExternalResourceDescription(BALEEN_HISTORY,
				clazz, params);
		resourceDescriptors.put(BALEEN_HISTORY, erd);
	}

	/**
	 * Look at the specified class and identify any resources that need including. If resources are
	 * found, then first try to use an existing instance of that resource before creating a new one
	 *
	 * @param clazz
	 *            The class to test
	 * @return A map of all the ExternalResourceDescriptions needed by the class
	 */
	private Map<String, ExternalResourceDescription> getResources(Class<?> clazz) {
		Map<String, ExternalResourceDescription> ret = new HashMap<>();

		List<Field> fields = new ArrayList<>();

		Class<?> c = clazz;
		while (c != null && c != Object.class) {
			fields.addAll(Arrays.asList(c.getDeclaredFields()));
			c = c.getSuperclass();
		}

		for (Field f : fields) {
			if (f.isAnnotationPresent(ExternalResource.class) && Resource.class.isAssignableFrom(f.getType())) {
				ExternalResource annotation = f.getAnnotation(ExternalResource.class);
				String key = annotation.key();

				ExternalResourceDescription erd;
				if (resourceDescriptors.containsKey(key)) {
					erd = resourceDescriptors.get(key);
				} else {
					Map<String, ExternalResourceDescription> erds = getResources(f.getType());
					Object[] params = CpeBuilderUtils.extractParams(globalConfig, ignoreParams, erds);
					erd = ExternalResourceFactory.createExternalResourceDescription(key,
							(Class<? extends Resource>) f.getType(), params);
					resourceDescriptors.put(key, erd);
				}

				ret.put(key, erd);
			}
		}

		return ret;
	}
}
