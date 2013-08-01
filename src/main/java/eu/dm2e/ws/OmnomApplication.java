package eu.dm2e.ws;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import eu.dm2e.ws.grafeo.jaxrs.GrafeoMessageBodyWriter;

/**
 * Jersey application with custom features enabled.
 *
 * <p>
 * This is the servlet used in the production Tomcat/JOSSO setup.
 * The test server is defined in {@link eu.dm2e.ws.wsmanager.ManageService}.
 * When changing features, remember to change them in {@link eu.dm2e.ws.wsmanager.ManageService} as well.
 * </p>
 *
 * <p>Enables these features:<ul>
 * <li>{@link org.glassfish.jersey.media.multipart.MultiPartFeature org.glassfish.jersey.media.multipart.MultiPartFeature}
 * <li>{@link org.glassfish.jersey.filter.LoggingFilter org.glassfish.jersey.filter.LoggingFilter}
 * <li>{@link SerializablePojoProvider}
 * <li>{@link SerializablePojoListMessageBodyWriter}
 * <li>{@link GrafeoMessageBodyWriter}
 * </ul></p>
 *
 * @see eu.dm2e.ws.wsmanager.ManageService
 * @see org.glassfish.jersey.media.multipart.MultiPartFeature org.glassfish.jersey.media.multipart.MultiPartFeature}
 * @see org.glassfish.jersey.filter.LoggingFilter org.glassfish.jersey.filter.LoggingFilter}
 * @see SerializablePojoProvider
 * @see SerializablePojoListMessageBodyWriter
 * @see GrafeoMessageBodyWriter
 *
 * @author Konstantin Baierer
 */
public class OmnomApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		final Set<Class<?>> classes = new HashSet<Class<?>>();
		// register resources and features
		classes.add(MultiPartFeature.class);
		classes.add(LoggingFilter.class);
		classes.add(SerializablePojoProvider.class);
		classes.add(SerializablePojoListMessageBodyWriter.class);
		classes.add(GrafeoMessageBodyWriter.class);
		return classes;
	}

}
