package eu.dm2e.ws.api.json;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
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
import eu.dm2e.grafeo.annotations.RDFProperty;

/**
 * Serialize / Deserialize SerializablePojos to/from JSON
 *
 * <p>
 * Smelly, slow, but working.
 * </p>
 * 
 * @author Konstantin Baierer
 *
 * @param <T>	The runtime type of the SerializablePojo
 */
public class SerializablePojoJsonSerializer<T> implements JsonSerializer<SerializablePojo<T>>,
		JsonDeserializer<SerializablePojo<T>>
{

	// private static final String BLANK_NODE_PREFIX = "_:";
	public static final int MAX_RECURSION_DEPTH = 10;
	// public static final String FLAG_IS_REFERRING_OBJECT = "__is_referer";

	private transient Logger log = LoggerFactory.getLogger(getClass().getName());

	@Override
	public JsonElement serialize(SerializablePojo src,
			Type typeOfSrc,
			JsonSerializationContext context) {
		return doSerialize(src, context, new HashMap<String, JsonObject>(), 0);
	}

	public static JsonObject findOrCreate(Map<String, JsonObject> cachedPojos,
			SerializablePojo pojo) {
		if (cachedPojos.keySet().contains(pojo.getUuid())) {
			return cachedPojos.get(pojo.getUuid());
		}
		// put it in the cache
		JsonObject flatObj = new JsonObject();
		if (pojo.hasId())
			flatObj.add(SerializablePojo.JSON_FIELD_ID, new JsonPrimitive(pojo.getId()));
		flatObj.addProperty(SerializablePojo.JSON_FIELD_UUID, pojo.getUuid());
		// flatObj.addProperty(FLAG_IS_REFERRING_OBJECT, true);
		cachedPojos.put(pojo.getUuid(), flatObj);
		return flatObj;
	}

	public JsonElement doSerialize(SerializablePojo src,
			JsonSerializationContext context,
			Map<String, JsonObject> cachedPojos,
			int recDepth
			) {
		log.debug("Serializing " + src);
		log.debug("Recursion level: " + recDepth);

		// if (cachedPojos.keySet().contains(src.getUuid())) {
		// return cachedPojos.get(src.getUuid());
		// } else {
		// findOrCreate(cachedPojos, src);
		// }

		// Stop at max recursion level
		JsonObject jsonObj = new JsonObject();
		if (recDepth >= src.getMaximumJsonDepth()) {
			return findOrCreate(cachedPojos, src);
		}

		if (src.hasId())
			jsonObj.add(SerializablePojo.JSON_FIELD_ID, new JsonPrimitive(src.getId()));
		
		// TODO FIXME remove else or not?
		else
			jsonObj.addProperty(SerializablePojo.JSON_FIELD_UUID, src.getUuid());

		for (Field field : PojoUtils.getAllFields(src.getClass())) {
			if (!field.isAnnotationPresent(RDFProperty.class))
				continue;

			boolean serializeAsURI = field.getAnnotation(RDFProperty.class).serializeAsURI();

			Class<?> subtypeClass = PojoUtils.subtypeClassOfGenericField(field);
			Object value;
			try {
				value = PropertyUtils.getProperty(src, field.getName());
			} catch (NoSuchMethodException e) {
				log.error(src.getClass().getName() + ": No getter/setters for " + field + " property: " + e);
				throw new RuntimeException(src.getClass().getName() + ": No getter/setters for " + field + " property: " + e);
			} catch (InvocationTargetException | IllegalAccessException e) {
				throw new RuntimeException("An exception occurred: " + e, e);
			}

			log.debug(src + " : Field " + field.getName() + " : " + value);

			/*
			 * Set the output name
			 */
//			final String jsonFieldName = field.getName();
			final String jsonFieldName = field.getAnnotation(RDFProperty.class).value();

			if (null == value) {
				if (Collection.class.isAssignableFrom(field.getClass())) {
					// save empty collection
					jsonObj.add(jsonFieldName, new JsonArray());
					continue;
				} else {
					// skip null values;
					continue;
				}
			}

			// skip empty collections
			// if (Collection.class.isAssignableFrom(value.getClass())
			// &&
			// ((Collection) value).isEmpty()) {
			// continue;
			// }

			/*
			 * Do the recursive data stuff
			 */
			if (SerializablePojo.class.isAssignableFrom(value.getClass())) {
				SerializablePojo valueAsSP = (SerializablePojo) value;
				if (valueAsSP.hasId() && serializeAsURI) {
//					jsonObj.add(jsonFieldName, new JsonPrimitive(valueAsSP.getId()));
					jsonObj.add(jsonFieldName, findOrCreate(cachedPojos, valueAsSP));
				} else if ( recDepth > 1) { // FIXME FIXME 
					// NON-RECURSION
					jsonObj.add(jsonFieldName, findOrCreate(cachedPojos, valueAsSP));
				} else {
					// RECURSION
					jsonObj.add(jsonFieldName,
							doSerialize(valueAsSP, context, cachedPojos, recDepth + 1));
				}

				// List<SerializablePojo>
				// Set<SerializablePojo>
			} else if (subtypeClass != null
					&& SerializablePojo.class.isAssignableFrom(subtypeClass)) {
				Collection<SerializablePojo> valueCollection;
				if (value instanceof java.util.List) { valueCollection = (List<SerializablePojo>) value;
				} else if (value instanceof java.util.Set) { valueCollection = (Set<SerializablePojo>) value;
				} else { throw new RuntimeException("Unserializable field " + field.getName() + " in " + src); }

				JsonArray jsonArr = new JsonArray();
				for (SerializablePojo subPojo : valueCollection) {
					if (subPojo.hasId() && serializeAsURI) {
//						jsonArr.add(new JsonPrimitive(subPojo.getId()));
						jsonArr.add(findOrCreate(cachedPojos, subPojo));
					} else if ( recDepth > 1) {
						// FIXME FIXME
						// ARRAY NON-RECURSION
						jsonArr.add(findOrCreate(cachedPojos, subPojo));
					} else {
						// ARRAY RECURSION
						jsonArr.add(doSerialize(subPojo, context, cachedPojos, recDepth + 1));
					}
				}
				jsonObj.add(jsonFieldName, jsonArr);

				// should be serialized
			} else {
				// Default RECURSION
				jsonObj.add(jsonFieldName, context.serialize(value));
			}
//			log.debug("JSON so far: " + jsonObj.toString());
		}

		// if (src.getId().startsWith(BLANK_NODE_PREFIX)) {
		// src.resetId();
		// }
		//
		// // Iterate to remove all default values set for references
		// // cleanDefaultValues(jsonObj);
		// jsonObj.addProperty(SerializablePojo.JSON_FIELD_UUID, src.getUuid());

		return jsonObj;
		// return jsonObj.entrySet().size() > 0 ? jsonObj : null;
	}

	@Override
	public SerializablePojo<T> deserialize(JsonElement json,
			Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		
		return (SerializablePojo<T>) this.doDeserialize(json.getAsJsonObject(), typeOfT, context, new HashMap<String,SerializablePojo>());
	}

	private T doDeserialize(JsonElement jsonElem,
			Type typeOfT,
			JsonDeserializationContext context,
			HashMap<String, SerializablePojo> pojoCache) {
		
		T pojo;
		Class<T> pojoClass = ((Class<T>) typeOfT);
		log.debug("DESERIALIZING {} as {}", typeOfT, pojoClass );
		
		if (!jsonElem.isJsonObject()) {
			// Assume that the field is serializeURI
			if (jsonElem.isJsonPrimitive()) {
				log.warn("Coercing flat ID to nested object.");
				JsonObject nestedIdObj = new JsonObject();
				nestedIdObj.addProperty(SerializablePojo.JSON_FIELD_ID, jsonElem.getAsString());
				jsonElem = nestedIdObj;
			} else {
				throw new RuntimeException("Can't deserialize a non-JSON-object/non-JSON-primitve to a SerializablePojo<T>: " + jsonElem.toString());
			}
		}
		JsonObject json = jsonElem.getAsJsonObject();
		
		String uuid;
		if (null != json.get(SerializablePojo.JSON_FIELD_UUID)) {
			uuid = json.get(SerializablePojo.JSON_FIELD_UUID).getAsJsonPrimitive().getAsString();
		} else {
			log.warn("Bad Bad JSON. Bad. No UUID."); 
			uuid = UUID.randomUUID().toString();
		}

		if (pojoCache.containsKey(uuid)) pojo = (T) pojoCache.get(uuid);
		else try { 
			pojo = pojoClass.newInstance(); 
			((SerializablePojo)pojo).setUuid(uuid);
			} catch (InstantiationException | IllegalAccessException e) { throw new RuntimeException(e); }
		pojoCache.put(uuid, (SerializablePojo)pojo);
		
		log.debug("Object we're de-serializing: " + uuid);
		log.debug("POJO class: " + pojoClass);
		
		JsonElement pojoIdJson = json.get(SerializablePojo.JSON_FIELD_ID);
		if (null != pojoIdJson) {
			try {
				PropertyUtils.setProperty(pojo, SerializablePojo.JSON_FIELD_ID, pojoIdJson.getAsString());
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
		
		for (Field field : PojoUtils.getAllFields(pojoClass)) {
			if (! field.isAnnotationPresent(RDFProperty.class)) continue;
			
			log.debug("Deserializing field {}", field);

			String rdfProp = field.getAnnotation(RDFProperty.class).value();

			JsonElement jsonChild = json.get(rdfProp);
			if (null == jsonChild) {
				log.info("No JSON entry for " + rdfProp + " (" + field + ").");
				try {
					if (List.class.isAssignableFrom(field.getClass())) {
						PropertyUtils.setProperty(pojo, field.getName(), new ArrayList<>());
					} else if (Set.class.isAssignableFrom(field.getClass())) {
						PropertyUtils.setProperty(pojo, field.getName(), new HashSet<>());
					} 
				} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
					throw new RuntimeException(e);
				}
				continue;
			}

			log.debug("JSON object element: " + jsonChild);
			final Class<?> pojoChildClass = field.getType();
			Class<?> pojoChildSubtypeClass = PojoUtils.subtypeClassOfGenericField(field);
			log.debug("De-serializing {} of POJO {}.", field.getName(), pojo);
			if (SerializablePojo.class.isAssignableFrom(pojoChildClass)) {
				log.debug("It's a serializable Pojo, recurse, recurse.");
			    try {
					PropertyUtils.setProperty(pojo, field.getName(), doDeserialize(jsonChild, pojoChildClass, context, pojoCache));
				} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
					throw new RuntimeException("MORP");
				}
			} else if (Collection.class.isAssignableFrom(pojoChildClass) && SerializablePojo.class.isAssignableFrom(pojoChildSubtypeClass)) {
				Collection resColl;
				if (List.class.isAssignableFrom(pojoChildClass)) { resColl = new ArrayList<SerializablePojo>();
				} else if (Set.class.isAssignableFrom(pojoChildClass)) { resColl = new HashSet<SerializablePojo>();
				} else { throw new RuntimeException("Unserializable collection field " + field.getName());}
				log.debug("It's a collection of serializable Pojos, recurse, recurse.");
				for (JsonElement jsonChildChild : jsonChild.getAsJsonArray()) {
					log.debug("Recurse on " + pojoChildSubtypeClass +" "+ jsonChildChild);
					resColl.add(doDeserialize(jsonChildChild, pojoChildSubtypeClass, context, pojoCache));
				}
				try {
					PropertyUtils.setProperty(pojo, field.getName(), resColl);
				} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
					throw new RuntimeException(e);
				}
			} else {
			    try {
					PropertyUtils.setProperty(pojo, field.getName(), context.deserialize(jsonChild, pojoChildClass));
				} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | JsonParseException e) {
					throw new RuntimeException(e);
				}
			}
		}
		log.warn("FINAL POJO After JSON deserialization: {}", ((SerializablePojo<T>) pojo).getTerseTurtle());
		return pojo;
	}
}

