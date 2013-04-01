package eu.dm2e.ws.services.job;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;

import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.GLiteral;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.grafeo.jena.SparqlConstruct;
import eu.dm2e.ws.grafeo.jena.SparqlSelect;
import eu.dm2e.ws.grafeo.jena.SparqlUpdate;
import eu.dm2e.ws.model.JobStatusConstants;
import eu.dm2e.ws.services.AbstractRDFService;

//import java.util.ArrayList;

@Path("/job")
public class JobRdfService extends AbstractRDFService {

	private Logger log = Logger.getLogger(getClass().getName());
	// TODO there must be an easier way
	private static Level[] logLevels = { Level.FINEST, Level.FINER, Level.INFO, Level.WARNING,
			Level.SEVERE, };
	//@formatter:off
	private static final String
			JOB_STATUS_PROP = NS.DM2E + "status",
			JOB_LOGENTRY_PROP = NS.DM2E + "hasLogEntry";
	//@formatter:on

	@GET
	@Path("/{resourceID}")
	@Consumes(MediaType.WILDCARD)
	public Response getJob(@PathParam("resourceID") String resourceID) {
		// kb: need to use Jena model
		GrafeoImpl g = new GrafeoImpl();
		g.readFromEndpoint(NS.ENDPOINT, getRequestUriWithoutQuery());
		Model jenaModel = g.getModel();
		NodeIterator iter = jenaModel.listObjectsOfProperty(jenaModel.createProperty(NS.DM2E
				+ "status"));
		if (null == iter || !iter.hasNext()) {
			return throwServiceError("No Job Status in this one. Not good.");
		}
		String jobStatus = iter.next().toString();
		int httpStatus;
		if (jobStatus.equals(JobStatusConstants.NOT_STARTED.toString())) httpStatus = 202;
		else if (jobStatus.equals(JobStatusConstants.NOT_STARTED.toString())) httpStatus = 202;
		else if (jobStatus.equals(JobStatusConstants.FAILED.toString())) httpStatus = 409;
		else if (jobStatus.equals(JobStatusConstants.FINISHED.toString())) httpStatus = 200;
		else httpStatus = 400;

		return Response.status(httpStatus).entity(getResponseEntity(g)).build();
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	public Response newJob(File bodyAsFile) {
		log.info("Config posted.");
		// TODO use Exception to return proper HTTP response if input can not be
		// parsed as RDF
		Grafeo g;
		try {
			g = new GrafeoImpl(bodyAsFile);
			// } catch (MalformedURLException e) {
		} catch (Exception e) {
			return throwServiceError(e);
		}
		GResource blank = g.findTopBlank();
		if (blank == null) {
			return throwServiceError("No top blank node found. Check your job description.");
		}
		String id = "" + new Date().getTime();
		String uri = uriInfo.getRequestUri() + "/" + id;
		blank.rename(uri);
		g.addTriple(uri, "rdf:type", NS.DM2E + "Job");
		g.addTriple(uri, NS.DM2E + "status", g.literal(JobStatusConstants.NOT_STARTED.toString()));
		g.writeToEndpoint(NS.ENDPOINT_STATEMENTS, uri);
		return Response.created(URI.create(uri)).entity(getResponseEntity(g)).build();
	}

	@PUT
	@Consumes(MediaType.WILDCARD)
	@Path("/{id}")
	public Response replaceJob(@PathParam("id") String id, String body) {
		return null;
	}

	public JobStatusConstants getJobStatusInternal(String jobUriStr)
			throws Exception {
		GrafeoImpl g = new GrafeoImpl();
		g.readFromEndpoint(NS.ENDPOINT, jobUriStr);
		Model jenaModel = g.getModel();
		NodeIterator iter = jenaModel.listObjectsOfProperty(jenaModel.createProperty(NS.DM2E
				+ "status"));
		if (null == iter || !iter.hasNext()) {
			throw new Exception("No Job Status in this one. Not good.");
		}
		String jobStatus = iter.next().toString();
		return Enum.valueOf(JobStatusConstants.class, jobStatus);
	}

	@GET
	@Path("/{id}/status")
	public Response getJobStatus(@PathParam("id") String id) {
		String resourceUriStr = getRequestUriWithoutQuery().toString().replaceAll("/status$", "");
		try {
			return Response.ok().entity(getJobStatusInternal(resourceUriStr).toString()).build();
		} catch (Exception e) {
			return throwServiceError(e);
		}
	}

	@PUT
	@Path("/{id}/status")
	@Consumes(MediaType.WILDCARD)
	public Response updateJobStatus(@PathParam("id") String id, String newStatus) {
		String resourceUriStr = getRequestUriWithoutQuery().toString().replaceAll("/status$", "");

		// validate if this is a valid status
		try {
			if (null == newStatus) return throwServiceError("No status sent.");
			Enum.valueOf(JobStatusConstants.class, newStatus);
		} catch (Exception e) {
			return throwServiceError("Invalid status type: " + newStatus);
		}

		// get the old status
		String oldStatus;
		try {
			oldStatus = getJobStatusInternal(resourceUriStr).toString();
			if (null == oldStatus) return throwServiceError("No status for this job found. Bad.");
		} catch (Exception e) {
			return throwServiceError(e);
		}

		// replace the job status
		String clauseDelete = String.format("<%s> <%s> ?old_status.", resourceUriStr,
				JOB_STATUS_PROP);
		String clauseInsert = String.format("<%s> <%s> \"%s\".", resourceUriStr, JOB_STATUS_PROP,
				newStatus);
		// @formatter:off
		new SparqlUpdate.Builder()
			.graph(resourceUriStr)
			.delete(clauseDelete)
			.insert(clauseInsert)
			.where(clauseDelete)
			.endpoint(NS.ENDPOINT_STATEMENTS).build().execute();
		// @formatter:on

		// return the new status live
		return getJobStatus(id);
	}

	//@formatter:off
	@POST
	@Path("/{id}/log")
	@Consumes({ DM2E_MediaType.APPLICATION_RDF_TRIPLES,
				DM2E_MediaType.APPLICATION_RDF_XML,
				DM2E_MediaType.TEXT_RDF_N3, 
				DM2E_MediaType.TEXT_TURTLE, })
	public Response addLogEntryAsRDF(File logRdfStr)  {
	//@formatter:on

		String resourceUriStr = getRequestUriWithoutQuery().toString().replaceAll("/log$", "");

		Grafeo g = new GrafeoImpl();
		long timestamp = new Date().getTime();
		GLiteral timestampLiteral = g.date(timestamp);
		log.info(new Long(timestamp).toString());
		try {
			g = new GrafeoImpl(logRdfStr);
			// TODO validate if this is a valid log entry
		} catch (Exception e) {
			return throwServiceError(e);
		}
		GResource blank = g.findTopBlank();
		if (null == blank) return throwServiceError("Must contain blank node");
		String logEntryUriStr = getRequestUriWithoutQuery() + "/log/" + timestamp;
		g.addTriple(resourceUriStr, JOB_LOGENTRY_PROP, logEntryUriStr);
		g.addTriple(logEntryUriStr, "rdf:type", NS.DM2ELOG + "LogEntry");
		g.addTriple(logEntryUriStr, NS.DM2ELOG + "timestamp", timestampLiteral);
		g.writeToEndpoint(NS.ENDPOINT_STATEMENTS, resourceUriStr);

		blank.rename(logEntryUriStr);

		return getResponse(g);
	}

	@POST
	@Path("/{id}/log")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response addLogEntryAsText(String logString) {

		// String resourceUri = getRequestUriWithoutQuery();
		String resourceUriStr = getRequestUriWithoutQuery().toString().replaceAll("/log$", "");

		Grafeo g = new GrafeoImpl();

		// Add timestamp, create uris and type them
		log.info("Adding timestamp");
		long timestamp = new Date().getTime();
		GLiteral timestampLiteral = g.date(timestamp);
		String logEntryUriStr = getRequestUriWithoutQuery().toString() + "/" + timestamp;
		g.addTriple(resourceUriStr, JOB_LOGENTRY_PROP, logEntryUriStr);
		g.addTriple(logEntryUriStr, "rdf:type", NS.DM2ELOG + "LogEntry");
		g.addTriple(logEntryUriStr, NS.DM2ELOG + "timestamp", timestampLiteral);

		// split up messages of the "DEBUG: foo bar!" variety if applicable
		log.info("Splitting up messages");
		String[] logStringParts = logString.split("\\s*:\\s*", 2);
		if (logStringParts.length == 2) {
			g.addTriple(logEntryUriStr, NS.DM2ELOG + "level", g.literal(logStringParts[0]));
			g.addTriple(logEntryUriStr, NS.DM2ELOG + "message", g.literal(logStringParts[1]));
		} else {
			g.addTriple(logEntryUriStr, NS.DM2ELOG + "message", g.literal(logString));
		}

		// if the "Referer" HTTP field is set, use that for setting the context
		log.info("Try the referer dance");
		List<String> referers = headers.getRequestHeader("Referer");
		if (null != referers) {
			g.addTriple(logEntryUriStr, NS.DM2ELOG + "context", referers.get(0));
		}

		// write the data
		log.info("Write out log message");
		g.writeToEndpoint(NS.ENDPOINT_STATEMENTS, resourceUriStr);

		log.info("Return the result");
		return getResponse(g);
		// return getJobStatus(uriInfo, id);
	}

	@GET
	@Path("/{id}/log")
	// @Consumes({
	// DM2E_MediaType.TEXT_TURTLE,
	// DM2E_MediaType.TEXT_RDF_N3,
	// DM2E_MediaType.APPLICATION_RDF_TRIPLES,
	// DM2E_MediaType.APPLICATION_RDF_XML,
	// })
	public Response listLogEntries(@QueryParam("minLevel") String minLevelStr) {

		String resourceUriStr = getRequestUriWithoutQuery().toString().replaceAll("/log$", "");
		String whereClause = "?s ?p ?o.\n ?s a <" + NS.DM2ELOG + "LogEntry>. \n";
		GrafeoImpl g = new GrafeoImpl();

		Level minLevel;
		if (minLevelStr != null) {
			minLevelStr = minLevelStr.replace("TRACE", "FINE");
			minLevelStr = minLevelStr.replace("DEBUG", "FINE");
			try {
				minLevel = Level.parse(minLevelStr);
				StringBuilder levelRegexSb = new StringBuilder(minLevelStr);
				for (Level l : logLevels) {
					log.info("" + l.intValue());
					if (l.intValue() < minLevel.intValue()) continue;
					levelRegexSb.append("|");
					levelRegexSb.append(l.toString());
				}
				whereClause = String.format("\n%s ?s <%s> ?level.\n FILTER regex(?level,\"%s\")",
						whereClause, NS.DM2ELOG + "level", levelRegexSb.toString());
			} catch (NullPointerException | IllegalArgumentException e) {
				return throwServiceError("Invalid 'minLevel' URI parameter.");
			}
		}

		log.info(whereClause);
		//@formatter:off
		try { new SparqlConstruct.Builder()
				.endpoint(NS.ENDPOINT)
				.graph(resourceUriStr)
				.construct("?s ?p ?o")
				.where(whereClause)
				.build()
				.execute(g);

		} catch (Exception e) { return throwServiceError(e); }
		//@formatter:on
		return getResponse(g);
	}

	@GET
	@Path("/{id}/log")
	@Produces({ "text/x-log" })
	public Response listLogEntriesAsLogfile(@QueryParam("minLevel") String minLevelStr) {

		String resourceUriStr = getRequestUriWithoutQuery().toString().replaceAll("/log$", "");
		StringBuilder whereClauseBuilder = new StringBuilder();
		whereClauseBuilder.append(String.format("?s a <%s>. ", NS.DM2ELOG + "LogEntry"));
		whereClauseBuilder.append(String.format("?s <%s> ?level.", NS.DM2ELOG + "level"));
		whereClauseBuilder.append(String.format("?s <%s> ?msg.", NS.DM2ELOG + "message"));
		whereClauseBuilder.append(String.format("?s <%s> ?date.", NS.DM2ELOG + "timestamp"));
		whereClauseBuilder.append(String.format("?s <%s> ?context.", NS.DM2ELOG + "context"));
		String whereClause = whereClauseBuilder.toString();
		String selectedVars = "?msg ?date ?level ?context";

		Level minLevel;
		if (minLevelStr != null) {
			minLevelStr = minLevelStr.replace("TRACE", "FINE");
			minLevelStr = minLevelStr.replace("DEBUG", "FINE");
			try {
				minLevel = Level.parse(minLevelStr);
				StringBuilder levelRegexSb = new StringBuilder(minLevelStr);
				for (Level l : logLevels) {
					if (l.intValue() < minLevel.intValue()) continue;
					levelRegexSb.append("|");
					levelRegexSb.append(l.toString());
				}
				whereClause = String.format("\n%s ?s <%s> ?level.\n FILTER regex(?level,\"%s\")",
						whereClause, NS.DM2ELOG + "level", levelRegexSb.toString());
			} catch (NullPointerException | IllegalArgumentException e) {
				return throwServiceError("Invalid 'minLevel' URI parameter.");
			}
		}

		log.info(whereClause);
		StringBuilder outputBuilder = new StringBuilder();
		try {
			//@formatter:off
			SparqlSelect sparqlSelect = new SparqlSelect.Builder()
				.endpoint(NS.ENDPOINT)
				.graph(resourceUriStr)
				.select(selectedVars)
				.where(whereClause)
				.orderBy("?date")
				.build();
			//@formatter:on
			log.info(sparqlSelect.toString());
			ResultSet iter = sparqlSelect.execute();
			while (iter.hasNext()) {
				QuerySolution row = iter.next();
				log.info(row.toString());
				outputBuilder.append("[");
				outputBuilder.append(row.get("level"));
				outputBuilder.append("] ");
				outputBuilder.append(row.get("date").asLiteral().getValue());
				outputBuilder.append(": ");
				outputBuilder.append(row.get("msg"));
				// outputBuilder.append("\t<");
				// outputBuilder.append(row.get("context"));
				// outputBuilder.append(">");
				outputBuilder.append("\n");
			}
		} catch (Exception e) {
			return throwServiceError(e);
		}
		return Response.ok().entity(outputBuilder.toString()).build();
	}
}