package org.hibernate.validation.util;

import java.lang.annotation.Annotation;
import java.security.PrivilegedAction;

import org.hibernate.validation.util.ReflectionHelper;

/**
 * @author Emmanuel Bernard
 */
public class GetAnnotationParameter<T> implements PrivilegedAction<T> {
	private final Annotation annotation;
	private final String parameterName;
	private final Class<T> type;


	public static <T> GetAnnotationParameter<T> action(Annotation annotation, String parameterName, Class<T> type) {
		return new GetAnnotationParameter<T>( annotation, parameterName, type );
	}

	private GetAnnotationParameter(Annotation annotation, String parameterName, Class<T> type) {
		this.annotation = annotation;
		this.parameterName = parameterName;
		this.type = type;
	}

	public T run() {
		return ReflectionHelper.getAnnotationParameter( annotation, parameterName, type );
	}
}
