package eu.dm2e.utils;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

public class PojoUtils extends BeanUtils {
	
	public static void copyProperties(Object dest, Object orig)
	    throws IllegalAccessException, InvocationTargetException {
	    
	    new SparsePojoCopier().copyProperties(dest, orig);
	}


}
