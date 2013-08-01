package eu.dm2e.ws.api.json;

import java.lang.reflect.Type;
import java.util.List;

import org.joda.time.DateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import eu.dm2e.ws.api.FilePojo;
import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.api.LogEntryPojo;
import eu.dm2e.ws.api.ParameterAssignmentPojo;
import eu.dm2e.ws.api.ParameterConnectorPojo;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.SerializablePojo;
import eu.dm2e.ws.api.UserPojo;
import eu.dm2e.ws.api.VersionedDatasetPojo;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.api.WorkflowConfigPojo;
import eu.dm2e.ws.api.WorkflowJobPojo;
import eu.dm2e.ws.api.WorkflowPojo;
import eu.dm2e.ws.api.WorkflowPositionPojo;

/**
 * Convenience class wrapping JSON serialization/deserialization functionality for easy reuse.
 *
 * @author Konstantin Baierer
 */
public class OmnomJsonSerializer {
	
//	private transient static Logger log = LoggerFactory.getLogger(OmnomJsonSerializer.class.getName());

//	private static class SpecificClassExclusionStrategy implements ExclusionStrategy {
//		private final Class<?> excludedThisClass;
//
//		public SpecificClassExclusionStrategy(Class<?> excludedThisClass) {
//			this.excludedThisClass = excludedThisClass;
//		}
//
//		@Override
//		public boolean shouldSkipClass(Class<?> clazz) {
//			return excludedThisClass.equals(clazz);
//		}
//
//		@Override
//		public boolean shouldSkipField(FieldAttributes f) {
//			return excludedThisClass.equals(f.getDeclaredClass());
//		}
//	}
	
	private static final Gson gson;
	
	static {
		
//		exclude
		
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setPrettyPrinting();
//		gsonBuilder.registerTypeAdapter(SerializablePojo.class, new SerializablePojoJsonSerializer());
//		gsonBuilder.registerTypeAdapter(AbstractPersistentPojo.class, new SerializablePojoJsonSerializer());
		
		// FIXME this is the generic solution but when used like that, no type
		// parameter can be passed on to SerializablePojoJsonSerializer<T>()
//		Reflections reflections = new Reflections("eu.dm2e.ws.api");
//		Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(RDFClass.class);
//		for (Class<?> clazz : annotated) {
//			log.debug("Registering JSON serializer for class : " + clazz);
//			gsonBuilder.registerTypeAdapter(clazz, new SerializablePojoJsonSerializer());
//		}
		gsonBuilder.registerTypeAdapter(DateTime.class, new JodaDateTimeSerializer());
		gsonBuilder.registerTypeAdapter(FilePojo.class, new SerializablePojoJsonSerializer<FilePojo>());
		gsonBuilder.registerTypeAdapter(JobPojo.class, new SerializablePojoJsonSerializer<JobPojo>());
		gsonBuilder.registerTypeAdapter(LogEntryPojo.class, new SerializablePojoJsonSerializer<LogEntryPojo>());
		gsonBuilder.registerTypeAdapter(ParameterAssignmentPojo.class, new SerializablePojoJsonSerializer<ParameterAssignmentPojo>());
		gsonBuilder.registerTypeAdapter(ParameterConnectorPojo.class, new SerializablePojoJsonSerializer<ParameterConnectorPojo>());
		gsonBuilder.registerTypeAdapter(ParameterPojo.class, new SerializablePojoJsonSerializer<ParameterPojo>());
		gsonBuilder.registerTypeAdapter(UserPojo.class, new SerializablePojoJsonSerializer<UserPojo>());
		gsonBuilder.registerTypeAdapter(VersionedDatasetPojo.class, new SerializablePojoJsonSerializer<VersionedDatasetPojo>());
		gsonBuilder.registerTypeAdapter(WebserviceConfigPojo.class, new SerializablePojoJsonSerializer<WebserviceConfigPojo>());
		gsonBuilder.registerTypeAdapter(WebservicePojo.class, new SerializablePojoJsonSerializer<WebservicePojo>());
		gsonBuilder.registerTypeAdapter(WorkflowConfigPojo.class, new SerializablePojoJsonSerializer<WorkflowConfigPojo>());
		gsonBuilder.registerTypeAdapter(WorkflowJobPojo.class, new SerializablePojoJsonSerializer<WorkflowJobPojo>());
		gsonBuilder.registerTypeAdapter(WorkflowPojo.class, new SerializablePojoJsonSerializer<WorkflowPojo>());
		gsonBuilder.registerTypeAdapter(WorkflowPositionPojo.class, new SerializablePojoJsonSerializer<WorkflowPositionPojo>());


		gsonBuilder.registerTypeAdapter(DateTime.class, new JodaDateTimeSerializer());
		gson = gsonBuilder.create();
	}
	
	public static String serializeToJSON(List<? extends SerializablePojo> pojoList) {
		JsonArray retArray = new JsonArray();
		for (SerializablePojo pojo : pojoList) {
			retArray.add(pojo.toJsonObject());
		}
		return gson.toJson(retArray);
	}
	
	public static <T> String serializeToJSON(List<? extends SerializablePojo<T>> pojoList, Type T) {
		JsonArray retArray = new JsonArray();
		for (SerializablePojo<T> pojo : pojoList) {
			retArray.add(serializeToJsonObject(pojo, T));
		}
		return gson.toJson(retArray);
	}
	
	public static <T> JsonObject serializeToJsonObject(SerializablePojo<T> pojo, Type T) {
		 JsonElement jsonElem = gson.toJsonTree(pojo, T);
		 if (! jsonElem.isJsonObject()) {
			 throw new RuntimeException(pojo + " was serialized to something other than a JSON object: " + jsonElem.getClass());
		 }
		 JsonObject json = jsonElem.getAsJsonObject();
		 if (pojo.hasId())
			 json.addProperty(SerializablePojo.JSON_FIELD_ID, pojo.getId());
		 if (null != pojo.getRDFClassUri())
			 json.addProperty(SerializablePojo.JSON_FIELD_RDF_TYPE, pojo.getRDFClassUri());
		 return json;
	}
				 
	
	public static <T> String serializeToJSON(SerializablePojo<T> pojo, Type T) {
		JsonObject json = serializeToJsonObject(pojo, T);
		return gson.toJson(json);
	}
	public static <T> T deserializeFromJSON(String jsonStr, Class<T> T) {
		return (T) gson.fromJson(jsonStr, T);
	}
	
}
