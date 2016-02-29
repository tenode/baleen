//Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.core.web.servlets;

import java.util.Arrays;

import uk.gov.dstl.baleen.core.web.security.WebPermission;
import uk.gov.dstl.baleen.cpe.PipelineCpeBuilder;

/**
 * List all annotators (inheriting from BaleenAnnotator) on the class path
 * 
 * 
 */
public class AnnotatorsServlet extends AbstractComponentApiServlet{
	private static final long serialVersionUID = 1L;
	private static final String ROLES = "annotators";
	
	public static final String ANNOTATOR_CLASS = "uk.gov.dstl.baleen.uima.BaleenAnnotator";
	public static final String CONSUMER_CLASS = "uk.gov.dstl.baleen.uima.BaleenConsumer";

	/**
	 * Constructor
	 */
	public AnnotatorsServlet(){
		super(ANNOTATOR_CLASS,
				PipelineCpeBuilder.ANNOTATOR_DEFAULT_PACKAGE,
				Arrays.asList(CONSUMER_CLASS),
				Arrays.asList(".*\\.internals", ".*\\.helpers"),
				AnnotatorsServlet.class);
	}
	
	@Override
	public WebPermission[] getPermissions() {
		return new WebPermission[] { new WebPermission("Access Annotators", ROLES) };
	}
}
