package eu.dm2e.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;


/**
 * Wrapper class for BeanUtils
 *
 * @see SparsePojoCopier
 * @see org.apache.commons.beanutils.BeanUtils org.apache.commons.beanutils.BeanUtils
 *
 * @author Konstantin Baierer
 */
public class PojoUtils extends BeanUtils {
	
	public static void copyProperties(Object dest, Object orig)
	    throws IllegalAccessException, InvocationTargetException {
	
	    new SparsePojoCopier().copyProperties(dest, orig);
	}

	/**
	 * Lists non-synthetic fields of this class and it's superclasses with annotations.
	 * 
	 * @param type The class for which to find all possibly inherited fields.
	 * @return List of fields
	 */
	public static List<Field> getAllFields(Class<?> type) {
		return getAllFields(null, type);
	}

	private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
		boolean baseOfRecursion = false;
		if (fields == null) {
			baseOfRecursion = true;
			fields = new ArrayList<Field>();
		}
		fields.addAll(Arrays.asList(type.getDeclaredFields()));
	
	    if (type.getSuperclass() != null) {
	        fields = getAllFields(fields, type.getSuperclass());
	    }
	    if (baseOfRecursion) {
	    	Iterator<Field> fieldIter = fields.iterator();
	    	while (fieldIter.hasNext()) {
	    		Field field = fieldIter.next();
	    		// Skip synthetic fields, such as those added by JaCoCo at runtime
	    		if (field.isSynthetic()) {
	    			fieldIter.remove();
	    			continue;
	    		}
	    		// Skip all fields without annotations
	    		if (field.getAnnotations().length == 0) {
	    			fieldIter.remove();
	    			continue;
	    		}
	    	}
	    }
	
	    return fields;
	}

	/**
	 * @param field
	 * @return
	 */
	public static Class<?> subtypeClassOfGenericField(Field field) {
		ParameterizedType subtype;
		try {
			 subtype = (ParameterizedType) field.getGenericType();
		} catch (ClassCastException e) {
			return null;
		}
		Class<?> subtypeClass = (Class<?>) subtype.getActualTypeArguments()[0];
		return subtypeClass;
	}

}
