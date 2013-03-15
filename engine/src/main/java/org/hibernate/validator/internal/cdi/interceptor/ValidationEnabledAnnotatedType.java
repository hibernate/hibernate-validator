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
import java.util.Set;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

import org.hibernate.validator.internal.util.CollectionHelper;

/**
 * @author Hardy Ferentschik
 */
public class ValidationEnabledAnnotatedType<T> implements AnnotatedType<T> {
	private final AnnotatedType<T> wrappedType;
	private final Set<AnnotatedMethod<? super T>> wrappedMethods;
	private final Set<AnnotatedConstructor<T>> wrappedConstructors;

	public ValidationEnabledAnnotatedType(AnnotatedType<T> type, Set<AnnotatedCallable<? super T>> constrainedCallables) {
		this.wrappedType = type;
		this.wrappedMethods = CollectionHelper.newHashSet();
		this.wrappedConstructors = CollectionHelper.newHashSet();
		buildWrappedCallable( constrainedCallables );
	}

	@Override
	public Class<T> getJavaClass() {
		return wrappedType.getJavaClass();
	}

	@Override
	public Set<AnnotatedConstructor<T>> getConstructors() {
		return wrappedConstructors;
	}

	@Override
	public Set<AnnotatedMethod<? super T>> getMethods() {
		return wrappedMethods;
	}

	@Override
	public Set<AnnotatedField<? super T>> getFields() {
		return wrappedType.getFields();
	}

	@Override
	public Type getBaseType() {
		return wrappedType.getBaseType();
	}

	@Override
	public Set<Type> getTypeClosure() {
		return wrappedType.getTypeClosure();
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		return wrappedType.getAnnotation( annotationType );
	}

	@Override
	public Set<Annotation> getAnnotations() {
		return wrappedType.getAnnotations();
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
		return wrappedType.isAnnotationPresent( annotationType );
	}

	private void buildWrappedCallable(Set<AnnotatedCallable<? super T>> constrainedCallables) {
		for ( AnnotatedConstructor<T> constructor : wrappedType.getConstructors() ) {
			if ( constrainedCallables.contains( constructor ) ) {
				ValidationEnabledAnnotatedConstructor<T> wrappedConstructor = new ValidationEnabledAnnotatedConstructor<T>(
						constructor
				);
				wrappedConstructors.add( wrappedConstructor );
			}
			else {
				wrappedConstructors.add( constructor );
			}
		}

		for ( AnnotatedMethod<? super T> method : wrappedType.getMethods() ) {
			if ( constrainedCallables.contains( method ) ) {
				ValidationEnabledAnnotatedMethod<? super T> wrappedMethod = wrap( method );
				wrappedMethods.add( wrappedMethod );
			}
			else {
				wrappedMethods.add( method );
			}
		}
	}

	private <U> ValidationEnabledAnnotatedMethod<U> wrap(AnnotatedMethod<U> method) {
		return new ValidationEnabledAnnotatedMethod<U>( method );
	}
}
