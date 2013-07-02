package eu.dm2e.ws.api.json;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.NotImplementedException;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import eu.dm2e.utils.PojoUtils;
import eu.dm2e.ws.api.SerializablePojo;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;

public class SerializablePojoJsonSerializer implements JsonSerializer<SerializablePojo>, JsonDeserializer<SerializablePojo>{
	
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
				jsonObj.add(field.getName(), context.serialize(value));
			}
			log.finer("JSON so far: " + jsonObj.toString());
		}
		return jsonObj;
	}

	@Override
	public SerializablePojo deserialize(JsonElement json,
			Type typeOfT,
			JsonDeserializationContext context)
			throws JsonParseException {
		log.info("Type: " + typeOfT);
		throw new NotImplementedException();
	}

}





















