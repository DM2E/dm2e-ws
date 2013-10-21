package eu.dm2e.grafeo.jaxrs;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dm2e.logback.LogbackMarkers;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.grafeo.jena.GrafeoImpl;

/**
 * JAX-RS MessageBodyWriter for Grafeo.
 * <p>
 * Allows consuming applications to return a Grafeo as an entity in responses.
 * </p>
 */
public class GrafeoMessageBodyWriter implements MessageBodyWriter<Grafeo> {

	Logger log = LoggerFactory.getLogger(getClass().getName());

	@Override
	public boolean isWriteable(Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType) {
		return DM2E_MediaType.isRdfMediaType(mediaType);
	}

	@Override
	public long getSize(Grafeo t,
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType) {
		// DEPRECATED dummy value
		return 0;
	}

	@Override
	public void writeTo(Grafeo t,
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException, WebApplicationException {
		GrafeoImpl g = (GrafeoImpl) t;
		final String jenaLang = DM2E_MediaType.getJenaLanguageForMediaType(mediaType);
		log.debug(LogbackMarkers.DATA_DUMP, "Serializing to RDF ({}), as terse turtle: {}",
				jenaLang, g.getTerseTurtle());
		g.getModel().write(entityStream, jenaLang);
	}

}
