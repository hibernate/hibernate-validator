package org.hibernate.validation.util;

import java.security.PrivilegedAction;
import javax.validation.ValidationException;

/**
 * @author Emmanuel Bernard
 */
public class NewInstance<T> implements PrivilegedAction<T> {
	private final Class<T> clazz;
	private final String message;

	public static <T> NewInstance<T> action(Class<T> clazz, String message) {
		return new NewInstance<T>( clazz, message );
	}

	private NewInstance(Class<T> clazz, String message) {
		this.clazz = clazz;
		this.message = message;
	}

	public T run() {
		try {
			return clazz.newInstance();
		}
		catch ( InstantiationException e ) {
			throw new ValidationException( "Unable to instanciate " + message + ": " + clazz, e );
		}
		catch ( IllegalAccessException e ) {
			throw new ValidationException( "Unable to instanciate " + clazz, e );
		}
		catch ( RuntimeException e ) {
			throw new ValidationException( "Unable to instanciate " + clazz, e );
		}
	}
}
