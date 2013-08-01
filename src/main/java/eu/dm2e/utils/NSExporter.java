package eu.dm2e.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.google.gson.JsonObject;

/**
 * Utility class for exposing class constants and enum values to JSON.
 *
 * @see eu.dm2e.ws.NS
 *
 * @author Konstantin Baierer
 */
public class NSExporter {
	
	public static String exportInnerClassConstantsToJSON(Class clazz) {
		
		JsonObject nsObj = new JsonObject();
		
		Class[] listOfInnerClasses = clazz.getDeclaredClasses();
		
		for (Class innerClass : listOfInnerClasses) {
			JsonObject innerClassObj = new JsonObject();
			nsObj.add(innerClass.getSimpleName(), innerClassObj);
			Field[] listOfFields = innerClass.getFields();
			for (Field field : listOfFields) {
				if (! Modifier.isStatic(field.getModifiers())
//						||
//					! Modifier.isStatic(field.getModifiers())
////						||
//					field.getName().equals("BASE")
//						||
//					! (
//						field.getName().startsWith("PROP_")
//							||
//						field.getName().startsWith("CLASS_")
//					  )
					)
					continue;
				try {
					innerClassObj.addProperty(field.getName(), (String)field.get(null));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return nsObj.toString();
	}
	
	public static void main(String[] args) {
//		String x = exportInnerClassConstantsToJSON(NS.class);
//		String x = exportEnumToJSON(JobStatus.class);
//		System.out.println(x);
//		System.out.println(
				
	}

	public static String exportEnumToJSON(Class clazz) {
		if (! clazz.isEnum()) {
			return "{}";
		}
		JsonObject enumObj = new JsonObject();
		for (Field field : clazz.getDeclaredFields()) {
			if (! field.isEnumConstant()) {
				continue;
			}
			enumObj.addProperty(field.getName(), field.getName());
		}
		return enumObj.toString();
	}

}
