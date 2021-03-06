package eu.dm2e.ws;

import eu.dm2e.grafeo.gom.SerializablePojo;
import eu.dm2e.grafeo.jena.GrafeoImpl;
import eu.dm2e.grafeo.json.GrafeoJsonSerializer;
import eu.dm2e.logback.LogbackMarkers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * JAX-RS MessageBodyWriter for lists of SerializablePojos.
 * <p>
 * Allows JAX-RS applications with this feature to return a {@code List<SerializablePojo>} as the entity.
 * The list is serialized either as a JSON array of JSON objects or a serialized RDF graph.
 * </p>
 *
 * @see SerializablePojoProvider
 * @author Konstantin Baierer
 */
//@Singleton
@Provider
@Produces({
	MediaType.WILDCARD,
	MediaType.APPLICATION_JSON
})
public class SerializablePojoListMessageBodyWriter implements MessageBodyWriter<List<SerializablePojo>> {
	
	Logger log = LoggerFactory.getLogger(getClass().getName());
	
//	private JsonParser jsonParser = new JsonParser();
	
	@Override
	public boolean isWriteable(Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType) {
		return DM2E_MediaType.expectsMetadataResponse(mediaType);
//		return MediaType.APPLICATION_XML_TYPE.equals(mediaType) 
//                && List.class.isAssignableFrom(type);
	}

	@Override
	public long getSize(List<SerializablePojo> t,
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType) {
		// This is deprecated
		return -1;
	}

	@Override
	public void writeTo(List<SerializablePojo> t,
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream)
			throws IOException, WebApplicationException {
		if (DM2E_MediaType.isJsonMediaType(mediaType)) {
			String jsonStr = GrafeoJsonSerializer.serializeToJSON(t);
			log.debug(LogbackMarkers.DATA_DUMP, "Serializing to JSON: {}", jsonStr);
			entityStream.write(jsonStr.getBytes("UTF-8"));
		} else if (DM2E_MediaType.isRdfMediaType(mediaType)) {
			// TODO FIXME this is duplicated in GrafeoMessageBodyWriter
			GrafeoImpl g = new GrafeoImpl();
			for (SerializablePojo pojo : t) {
				g.merge(pojo.getGrafeo());
			}

			final String jenaLng = DM2E_MediaType.getJenaLanguageForMediaType(mediaType);
			log.debug(LogbackMarkers.DATA_DUMP, "Serializing to RDF ({}), as terse turtle: {}",
					jenaLng, g.getTerseTurtle());
			g.getModel().write(entityStream, jenaLng);
		}
		log.debug("Finished writing to entityStream");
	}

}
