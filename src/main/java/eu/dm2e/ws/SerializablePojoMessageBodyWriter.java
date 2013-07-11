package eu.dm2e.ws;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dm2e.logback.LogbackMarkers;
import eu.dm2e.ws.api.SerializablePojo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

//@Singleton
@Provider
@Produces({
	MediaType.WILDCARD,
	MediaType.APPLICATION_JSON
})
public class SerializablePojoMessageBodyWriter implements MessageBodyWriter<SerializablePojo> {
	
	Logger log = LoggerFactory.getLogger(getClass().getName());
	
	@Override
	public boolean isWriteable(Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType) {
//		return true;
//		return false;
		return (
				SerializablePojo.class.isAssignableFrom(type)
				&&
				DM2E_MediaType.expectsMetadataResponse(mediaType)
				);
	}

	@Override
	public long getSize(SerializablePojo t,
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType) {
		// This is deprecated
		return -1;
	}

	@Override
	public void writeTo(SerializablePojo t,
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream)
			throws IOException, WebApplicationException {
		if (DM2E_MediaType.expectsJsonResponse(mediaType)) {
			log.debug(LogbackMarkers.DATA_DUMP, "Serializing to JSON: {}", t.toJson());
			entityStream.write(t.toJson().getBytes("UTF-8"));
		} else if (DM2E_MediaType.expectsRdfResponse(mediaType)) {
			GrafeoImpl g = (GrafeoImpl) t.getGrafeo();
			final String jenaLng = DM2E_MediaType.getJenaLanguageForMediaType(mediaType);
			log.debug(LogbackMarkers.DATA_DUMP, "Serializing to RDF ({}), as terse turtle: {}",
					jenaLng, g.getTerseTurtle());
			g.getModel().write(entityStream, jenaLng);
		}
		log.debug("Finished writing to entityStream");
	}

}