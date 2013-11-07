package eu.dm2e.ws;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import eu.dm2e.grafeo.jaxrs.GrafeoMessageBodyWriter;
import eu.dm2e.grafeo.json.GrafeoJsonSerializer;
import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.LogEntryPojo;
import eu.dm2e.ws.api.ParameterAssignmentPojo;
import eu.dm2e.ws.api.ParameterConnectorPojo;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.UserPojo;
import eu.dm2e.ws.api.VersionedDatasetPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.api.WorkflowPojo;
import eu.dm2e.ws.api.WorkflowPositionPojo;

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

	static {
        GrafeoJsonSerializer.registerType(JobPojo.class);
        GrafeoJsonSerializer.registerType(FilePojo.class);
        GrafeoJsonSerializer.registerType(LogEntryPojo.class);
        GrafeoJsonSerializer.registerType(ParameterAssignmentPojo.class);
        GrafeoJsonSerializer.registerType(ParameterConnectorPojo.class);
        GrafeoJsonSerializer.registerType(ParameterPojo.class);
        GrafeoJsonSerializer.registerType(UserPojo.class);
        GrafeoJsonSerializer.registerType(VersionedDatasetPojo.class);
        GrafeoJsonSerializer.registerType(WebserviceConfigPojo.class);
        GrafeoJsonSerializer.registerType(WebservicePojo.class);
        GrafeoJsonSerializer.registerType(WorkflowPojo.class);
        GrafeoJsonSerializer.registerType(WorkflowPositionPojo.class);
	}

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
