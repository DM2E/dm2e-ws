package eu.dm2e.ws.api.json;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.apache.commons.beanutils.PropertyUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import eu.dm2e.utils.PojoUtils;
import eu.dm2e.ws.api.SerializablePojo;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

public class SerializablePojoJsonSerializer 
	implements JsonSerializer<SerializablePojo>
//				, JsonDeserializer<SerializablePojo>
{
	
	private transient Logger log = Logger.getLogger(getClass().getName());
	
	@Override
	public JsonElement serialize(SerializablePojo src, Type typeOfSrc, JsonSerializationContext context) {
		log.info("Serializing " + src);
		JsonObject jsonObj = new JsonObject();
		if (src.hasId()) {
			jsonObj.add(SerializablePojo.JSON_FIELD_ID, new JsonPrimitive(src.getId()));
//			 might be smarter this way
//			return new JsonPrimitive(src.getId());
		}
		for (Field field : PojoUtils.getAllFields(src.getClass())) {
			if (!field.isAnnotationPresent(RDFProperty.class)) 
				continue;
			
			Object value;
			try {
				value = PropertyUtils.getProperty(src, field.getName());
			} catch (NoSuchMethodException e) {
				log.severe(src.getClass().getName() +": No getter/setters for " + field.getName() + " property: " + e);
				throw new RuntimeException(src.getClass().getName() +": No getter/setters for " + field.getName() + " property: " + e);
			} catch (InvocationTargetException | IllegalAccessException e) {
				throw new RuntimeException("An exception occurred: " + e, e);
			}
			
			
			if (null == value)
				continue;
			
			log.fine(src + " : Field " + field.getName() + " : " + value);
			
			if (SerializablePojo.class.isAssignableFrom(value.getClass())) {
				SerializablePojo valueAsSP = (SerializablePojo) value;
				if (valueAsSP.hasId()) {
					JsonObject subObj = new JsonObject();
					subObj.addProperty(SerializablePojo.JSON_FIELD_ID, valueAsSP.getId());
					jsonObj.add(field.getName(), subObj);
				}
			} else {
				// RECURSION
				jsonObj.add(field.getName(), context.serialize(value));
			}
			log.finer("JSON so far: " + jsonObj.toString());
		}
		return jsonObj;
//		return jsonObj.entrySet().size() > 0 ? jsonObj : null;
	}

//	@Override
//	public SerializablePojo deserialize(JsonElement json,
//			Type typeOfT,
//			JsonDeserializationContext context)
//			throws JsonParseException {
//		log.info("Type: " + typeOfT);
//		Set<? extends SerializablePojo> loadedResources = new HashSet<>();
//		return deserializeAndLoad(json, typeOfT, context, loadedResources);
//	}
//	
//	public SerializablePojo deserializeAndLoad(JsonElement json,
//			Type typeOfT,
//			JsonDeserializationContext context,
//			Set<? extends SerializablePojo> loadedResources
//			)
//			throws JsonParseException {
//		log.info("Deserializing Type: " + typeOfT);
//		JsonObject jsonObj = json.getAsJsonObject();
//		try {
//			typeOfT pojo = (typeOfT) typeOfT.getClass().newInstance();
//		} catch (InstantiationException | IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		for (Field field : PojoUtils.getAllFields(typeOfT.getClass())) {
//			if (!field.isAnnotationPresent(RDFProperty.class)) 
//				continue;
//			final JsonElement value = jsonObj.get(field.getName());
//			if (null == value)
//				continue;
//			if (SerializablePojo.class.isAssignableFrom(typeOfT.getClass())) {
//				// TODO recursion
//			} else if (List.class.isAssignableFrom(typeOfT.getClass())){
//				// TODO List
//			} else if (Set.class.isAssignableFrom(typeOfT.getClass())){
//				// TODO Set
//			} else {
//				// TODO literal
//			}
//		}
//		return null;
//	}
	

}





















