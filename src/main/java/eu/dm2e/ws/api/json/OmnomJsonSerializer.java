package eu.dm2e.ws.api.json;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.reflections.Reflections;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import eu.dm2e.ws.api.AbstractPersistentPojo;
import eu.dm2e.ws.api.SerializablePojo;
import eu.dm2e.ws.grafeo.annotations.RDFClass;

public class OmnomJsonSerializer {
	
	private transient static Logger log = Logger.getLogger(OmnomJsonSerializer.class.getName());
	
	private static Gson gson;
	
	static {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setPrettyPrinting();
		gsonBuilder.registerTypeAdapter(SerializablePojo.class, new SerializablePojoJsonSerializer());
		gsonBuilder.registerTypeAdapter(AbstractPersistentPojo.class, new SerializablePojoJsonSerializer());
		
		Reflections reflections = new Reflections("eu.dm2e.ws.api");
		Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(RDFClass.class);
		for (Class<?> clazz : annotated) {
			log.info("Class : " + clazz);
			gsonBuilder.registerTypeAdapter(clazz, new SerializablePojoJsonSerializer());
		}
		
		gsonBuilder.registerTypeAdapter(DateTime.class, new JodaDateTimeSerializer());
		gson = gsonBuilder.create();
	}
	
	public static <T> String serializeToJSON(SerializablePojo pojo, Type T) {
		 JsonElement jsonElem = gson.toJsonTree(pojo, T);
		 if (! jsonElem.isJsonObject()) {
			 throw new RuntimeException(pojo + " was serialized to something other than a JSON object.");
		 }
		 JsonObject json = jsonElem.getAsJsonObject();
		 if (pojo.hasId())
			 json.addProperty(SerializablePojo.JSON_FIELD_ID, pojo.getId());
		 if (null != pojo.getRDFClassUri())
			 json.addProperty(SerializablePojo.JSON_FIELD_RDF_TYPE, pojo.getRDFClassUri());
		 return gson.toJson(json);
	}
	public static <T> T deserializeFromJSON(String jsonStr, Class T) {
		return (T) gson.fromJson(jsonStr, T);
	}

}
