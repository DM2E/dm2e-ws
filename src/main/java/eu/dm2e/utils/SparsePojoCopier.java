package eu.dm2e.utils;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Drop-In for BeanUtils to copy only those properties that are not null.
 * 
 * largely taken from http://stackoverflow.com/questions/1301697/helper-in-order-to-copy-non-null-properties-from-object-to-another-java
 * 
 * @author Konstantin Baierer
 *
 */
public class SparsePojoCopier extends BeanUtilsBean {
	
	/**
	 * @see org.apache.commons.beanutils.BeanUtilsBean#copyProperty(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	@Override
	public void copyProperty(Object dest, String name, Object value) throws IllegalAccessException, InvocationTargetException {
		// Don't copy empty properties
		if (value == null) 
			return;
		super.copyProperty(dest, name, value);
	}

}
