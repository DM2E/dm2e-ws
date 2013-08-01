package eu.dm2e.ws.services.xslt;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;

import eu.dm2e.ws.api.JobPojo;

/**
 * ErrorListener for XSLT transformations.
 *
 * TODO move this somewhere else
 * @author Konstantin Baierer
 */
public class OmnomErrorListener implements ErrorListener {

	private Logger logger;
	private JobPojo job;

	public OmnomErrorListener(Logger logger) {
		this.logger = logger;
	}

	public OmnomErrorListener(JobPojo job) {
		this.job = job;
	}

	public void warning(TransformerException exception) {

		if (null != job) job.warn(exception.getMessage() + exception);
		if (null != logger) logger.warn(exception.getMessage() + exception);

		// Don't throw an exception and stop the processor
		// just for a warning; but do log the problem
	}

	public void error(TransformerException exception)
			throws TransformerException {

		if (null != job) job.fatal("SEVERE XSLT ERROR, VERY BAD: " + exception);
		if (null != logger ) logger.error("SEVERE XSLT ERROR, VERY BAD: " + exception);
//		logger.error("SEVERE XSLT ERROR, VERY BAD: " + exception);
		// XSLT is not as draconian as XML. There are numerous errors
		// which the processor may but does not have to recover from;
		// e.g. multiple templates that match a node with the same
		// priority. I do not want to allow that so I throw this
		// exception here.
		// throw exception;

	}

	public void fatalError(TransformerException exception)
			throws TransformerException {

		if (null != job) job.fatal("FATAL XSLT ERROR, VERY BAD: " + exception);
		if (null != logger ) logger.error("FATAL XSLT ERROR, VERY BAD: " + exception);

		// This is an error which the processor cannot recover from;
		// e.g. a malformed stylesheet or input document
		// so I must throw this exception here.
		// throw exception;

	}

}
