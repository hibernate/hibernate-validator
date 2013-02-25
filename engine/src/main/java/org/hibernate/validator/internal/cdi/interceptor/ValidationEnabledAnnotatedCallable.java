/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.cdi.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.util.AnnotationLiteral;

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
