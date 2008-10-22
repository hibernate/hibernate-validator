// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.ConstraintDescriptor;
import javax.validation.ValidationException;
import javax.validation.ReportAsSingleInvalidConstraint;

/**
 * Describe a single constraint.
 *
 * @author Emmanuel Bernard
 */
public class ConstraintDescriptorImpl implements ConstraintDescriptor {
	private final Annotation annotation;
	private final Constraint constraintImplementation;
	private final Class<? extends Constraint> constraintClass;
	private final Set<String> contexts;
	private final Map<String, Object> parameters;
	private final boolean isReportAsSingleInvalidConstraint;

	public ConstraintDescriptorImpl(Annotation annotation, String[] contexts, Constraint validator, Class<? extends Constraint> constraintClass) {
		this.annotation = annotation;
		if ( contexts.length == 0 ) {
			contexts = new String[] { "default" };
		}
		this.contexts = new HashSet<String>();
		this.contexts.addAll( Arrays.asList( contexts ) );
		this.constraintImplementation = validator;
		this.parameters = getAnnotationParameters( annotation );
		this.isReportAsSingleInvalidConstraint = annotation.annotationType().isAnnotationPresent(
				ReportAsSingleInvalidConstraint.class
		);
		this.constraintClass = constraintClass;
	}

	/**
	 * Constraint declaration annotation
	 */
	public Annotation getAnnotation() {
		return annotation;
	}

	/**
	 * What are the contexts the constraint is applied on
	 */
	public Set<String> getGroups() {
		return contexts;
	}

	public Class<? extends Constraint> getContstraintClass() {
		return constraintClass;
	}

	public boolean isInGroups(String group) {
		return contexts.contains( group );
	}

	/**
	 * Return the constraint implementation routine
	 * //FIXME should we get rid of that and call the ConstraintFactory each time??
	 */
	public Constraint getConstraintImplementation() {
		return constraintImplementation;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public Set<ConstraintDescriptor> getComposingConstraints() {
		//FIXME implement
		return Collections.emptySet();
	}

	public boolean isReportAsSingleInvalidConstraint() {
		return isReportAsSingleInvalidConstraint;
	}

	private Map<String, Object> getAnnotationParameters(Annotation annotation) {
		Method[] declaredMethods = annotation.annotationType().getDeclaredMethods();
		Map<String, Object> parameters = new HashMap<String, Object>( declaredMethods.length );
		for ( Method m : declaredMethods ) {
			try {
				parameters.put( m.getName(), m.invoke( annotation ) );
			}
			catch ( IllegalAccessException e ) {
				throw new ValidationException( "Unable to read annotation parameters: " + annotation.getClass(), e );
			}
			catch ( InvocationTargetException e ) {
				throw new ValidationException( "Unable to read annotation parameters: " + annotation.getClass(), e );
			}
		}
		return Collections.unmodifiableMap( parameters );
	}
}
