package org.hibernate.validation.util;

import java.security.PrivilegedAction;

/**
 * @author Emmanuel Bernard
 */
public class ContainsField implements PrivilegedAction<Boolean> {
	private final Class<?> clazz;
	private final String property;

	public static ContainsField action(Class<?> clazz, String property) {
		return new ContainsField( clazz, property );
	}

	private ContainsField(Class<?> clazz, String property) {
		this.clazz = clazz;
		this.property = property;
	}

	public Boolean run() {
		try {
			clazz.getDeclaredField( property );
			return true;
		}
		catch ( NoSuchFieldException e ) {
			return false;
		}
	}
}