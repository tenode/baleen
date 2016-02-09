//Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.core.web.servlets;

import uk.gov.dstl.baleen.core.pipelines.BaleenJob;
import uk.gov.dstl.baleen.core.pipelines.BaleenPipelineManager;
import uk.gov.dstl.baleen.cpe.AbstractCpeManager;

/**
 * Create, deletes, start and stops pipelines in a {@link BaleenPipelineManager} .
 *
 * @see AbstractCpeManagerApiServlet for more details of usage.
 *
 */
public class PipelineManagerServlet extends AbstractCpeManagerApiServlet<BaleenJob> {

	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new pipeline manager servlet.
	 *
	 * @param manager
	 *            the manager
	 */
	public PipelineManagerServlet(AbstractCpeManager<BaleenJob> manager) {
		super("pipelines", manager, PipelineManagerServlet.class);
	}
}