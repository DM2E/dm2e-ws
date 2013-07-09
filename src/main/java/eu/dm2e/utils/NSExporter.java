package eu.dm2e.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.google.gson.JsonObject;

import eu.dm2e.ws.NS;

public class NSExporter {
	
	public static String exportToJSON(Class clazz) {
		
		JsonObject nsObj = new JsonObject();
		
		Class[] listOfInnerClasses = clazz.getDeclaredClasses();
		
		for (Class innerClass : listOfInnerClasses) {
			JsonObject innerClassObj = new JsonObject();
			nsObj.add(innerClass.getSimpleName(), innerClassObj);
			Field[] listOfFields = innerClass.getFields();
			for (Field field : listOfFields) {
				if (! Modifier.isStatic(field.getModifiers())
						||
					! Modifier.isStatic(field.getModifiers())
						||
					field.getName().equals("BASE")
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
		String x = exportToJSON(NS.class);
		System.out.println(x);
//		System.out.println(
				
	}

}
