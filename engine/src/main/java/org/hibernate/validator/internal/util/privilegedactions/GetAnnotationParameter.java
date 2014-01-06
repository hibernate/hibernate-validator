/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.util.privilegedactions;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public final class GetAnnotationParameter<T> implements PrivilegedAction<T> {

	private static final Log log = LoggerFactory.make();

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

	@Override
	public T run() {
		try {
			Method m = annotation.getClass().getMethod( parameterName );
			m.setAccessible( true );
			Object o = m.invoke( annotation );
			if ( type.isAssignableFrom( o.getClass() ) ) {
				return (T) o;
			}
			else {
				throw log.getWrongParameterTypeException( type.getName(), o.getClass().getName() );
			}
		}
		catch ( NoSuchMethodException e ) {
			throw log.getUnableToFindAnnotationParameterException( parameterName, e );
		}
		catch ( IllegalAccessException e ) {
			throw log.getUnableToGetAnnotationParameterException( parameterName, annotation.getClass().getName(), e );
		}
		catch ( InvocationTargetException e ) {
			throw log.getUnableToGetAnnotationParameterException( parameterName, annotation.getClass().getName(), e );
		}
	}
}
