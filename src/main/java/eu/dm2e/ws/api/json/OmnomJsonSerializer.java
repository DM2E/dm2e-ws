package eu.dm2e.ws.api.json;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.joda.time.DateTime;
import org.reflections.Reflections;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import eu.dm2e.ws.api.AbstractPersistentPojo;
import eu.dm2e.ws.api.SerializablePojo;
import eu.dm2e.ws.grafeo.annotations.RDFClass;

public class OmnomJsonSerializer {
	
	private transient static Logger log = LoggerFactory.getLogger(OmnomJsonSerializer.class.getName());
	
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
	
	public static <T> String serializeToJSON(List<? extends SerializablePojo<T>> pojoList, Type T) {
		JsonArray retArray = new JsonArray();
		for (SerializablePojo<T> pojo : pojoList) {
			retArray.add(serializeToJsonObject(pojo, T));
		}
		return gson.toJson(retArray);
	}
	
	private static <T> JsonObject serializeToJsonObject(SerializablePojo<T> pojo, Type T) {
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
