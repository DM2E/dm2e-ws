package eu.dm2e.ws;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import eu.dm2e.ws.grafeo.jaxrs.GrafeoMessageBodyWriter;

public class OmnomApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		final Set<Class<?>> classes = new HashSet<Class<?>>();
		// register resources and features
		classes.add(MultiPartFeature.class);
		classes.add(LoggingFilter.class);
		classes.add(SerializablePojoMessageBodyWriter.class);
		classes.add(SerializablePojoListMessageBodyWriter.class);
		classes.add(GrafeoMessageBodyWriter.class);
		return classes;
	}

}
