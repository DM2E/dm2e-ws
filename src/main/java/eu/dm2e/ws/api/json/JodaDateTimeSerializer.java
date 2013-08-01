package eu.dm2e.ws.api.json;

import java.lang.reflect.Type;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Serialize and Deserialize Joda DateTime to/from JSON
 */
public class JodaDateTimeSerializer implements JsonSerializer<DateTime>, JsonDeserializer<DateTime>{
	
	  @Override
	  public JsonElement serialize(DateTime src, Type srcType, JsonSerializationContext context) {
		  DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		  return new JsonPrimitive(fmt.print(src));
	  }
	  
	  @Override
	  public DateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
	    return new DateTime(json.getAsString());
	  }

}
