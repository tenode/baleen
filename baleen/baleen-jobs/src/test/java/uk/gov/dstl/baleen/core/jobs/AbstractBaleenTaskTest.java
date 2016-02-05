package uk.gov.dstl.baleen.core.jobs;

import java.util.Map;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.After;
import org.junit.Before;

import uk.gov.dstl.baleen.exceptions.BaleenException;

public class AbstractBaleenTaskTest {

	private BaleenJobManager jobManager;
	private JCas jCas;

	@Before
	public void beforeAbstractBaleenTaskTest() throws UIMAException {
		jobManager = new BaleenJobManager();
		jCas = JCasFactory.createJCas();
	}

	@After
	public void afterAbstractBaleenTaskTest() {
		jobManager.stopAll();
		jCas.release();

	}

	protected AnalysisEngine create(Class<? extends BaleenTask> taskClass, Object... args)
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngine(taskClass, args);
	}

	protected AnalysisEngine create(Class<? extends BaleenTask> taskClass)
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngine(taskClass);
	}

	private String getYaml(Class<? extends BaleenTask> taskClass) {
		return String.format("job:\n  schedule: Once\n  tasks:\n  - class: %s\n", taskClass.getName());
	}

	private String getYaml(Class<? extends BaleenTask> taskClass, Map<String, String> params) {
		StringBuilder sb = new StringBuilder(getYaml(taskClass));
		if (params != null) {
			for (Map.Entry<String, String> e : params.entrySet()) {
				sb.append(String.format("    %s: %s\n", e.getKey(), e.getValue()));
			}
		}
		return params.toString();
	}

	protected BaleenJob wrapInJob(Class<? extends BaleenTask> taskClass) throws BaleenException {
		String yaml = getYaml(taskClass);
		return jobManager.create("testjob", yaml);
	}

	protected BaleenJob wrapInJob(Class<? extends BaleenTask> taskClass, Map<String, String> params)
			throws BaleenException {
		String yaml = getYaml(taskClass, params);
		return jobManager.create("testjob", yaml);
	}

	protected JobSettings execute(AnalysisEngine... analysisEngines) throws AnalysisEngineProcessException {
		jCas.reset();
		for (AnalysisEngine ae : analysisEngines) {
			ae.process(jCas);
		}
		return new JobSettings(jCas);
	}

}
