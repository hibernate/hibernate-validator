/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cdi.internal.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.enterprise.inject.spi.AnnotatedCallable;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.util.AnnotationLiteral;

/**
 * @author Hardy Ferentschik
 */
public abstract class ValidationEnabledAnnotatedCallable<T> implements AnnotatedCallable<T> {
	private final AnnotatedCallable<T> wrappedCallable;
	private final AnnotationLiteral<MethodValidated> methodValidationAnnotation;

	public ValidationEnabledAnnotatedCallable(AnnotatedCallable<T> callable) {
		this.wrappedCallable = callable;
		this.methodValidationAnnotation = new AnnotationLiteral<MethodValidated>() {
		};
	}

	@Override
	public boolean isStatic() {
		return wrappedCallable.isStatic();
	}

	@Override
	public AnnotatedType<T> getDeclaringType() {
		return wrappedCallable.getDeclaringType();
	}

	@Override
	public List<AnnotatedParameter<T>> getParameters() {
		return wrappedCallable.getParameters();
	}

	@Override
	public Type getBaseType() {
		return wrappedCallable.getBaseType();
	}

	@Override
	public Set<Type> getTypeClosure() {
		return wrappedCallable.getTypeClosure();
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		if ( MethodValidated.class.equals( annotationType ) ) {
			@SuppressWarnings("unchecked")
			A annotation = (A) methodValidationAnnotation;
			return annotation;
		}
		else {
			return wrappedCallable.getAnnotation( annotationType );
		}
	}

	@Override
	public Set<Annotation> getAnnotations() {
		Set<Annotation> annotations = new HashSet<Annotation>( wrappedCallable.getAnnotations() );
		annotations.add( methodValidationAnnotation );
		return annotations;
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
		if ( MethodValidated.class.equals( annotationType ) ) {
			return true;
		}
		else {
			return wrappedCallable.isAnnotationPresent( annotationType );
		}
	}

	AnnotatedCallable<T> getWrappedCallable() {
		return wrappedCallable;
	}
}
