/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.cfg.context;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Map;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Represents a programmatically configured constraint and meta-data
 * related to its location (bean type etc.).
 *
 * @author Gunnar Morling
 */
class ConfiguredConstraint<A extends Annotation> {

	private static final Log log = LoggerFactory.make();

	private final ConstraintDefAccessor<A> constraint;
	private final ConstraintLocation location;
	private final ElementType elementType;

	private ConfiguredConstraint(ConstraintDef<?, A> constraint, ConstraintLocation location, ElementType elementType) {

		this.constraint = new ConstraintDefAccessor<A>( constraint );
		this.location = location;
		this.elementType = elementType;
	}

	static <A extends Annotation> ConfiguredConstraint<A> forType(ConstraintDef<?, A> constraint, Class<?> beanType) {
		return new ConfiguredConstraint<A>( constraint, ConstraintLocation.forClass( beanType ), ElementType.TYPE );
	}

	static <A extends Annotation> ConfiguredConstraint<A> forProperty(ConstraintDef<?, A> constraint, Member member) {
		return new ConfiguredConstraint<A>(
				constraint,
				ConstraintLocation.forProperty( member ),
				( member instanceof Field ) ? ElementType.FIELD : ElementType.METHOD
		);
	}

	public static <A extends Annotation> ConfiguredConstraint<A> forParameter(ConstraintDef<?, A> constraint, ExecutableElement executable, int parameterIndex) {
		return new ConfiguredConstraint<A>(
				constraint, ConstraintLocation.forParameter( executable, parameterIndex ), executable.getElementType()
		);
	}

	public static <A extends Annotation> ConfiguredConstraint<A> forExecutable(ConstraintDef<?, A> constraint, ExecutableElement executable) {
		return new ConfiguredConstraint<A>(
				constraint, ConstraintLocation.forReturnValue( executable ), executable.getElementType()
		);
	}

	public ConstraintDef<?, A> getConstraint() {
		return constraint;
	}

	public ConstraintLocation getLocation() {
		return location;
	}

	public Class<A> getConstraintType() {
		return constraint.getConstraintType();
	}

	public Map<String, Object> getParameters() {
		return constraint.getParameters();
	}

	public A createAnnotationProxy() {

		AnnotationDescriptor<A> annotationDescriptor = new AnnotationDescriptor<A>( getConstraintType() );
		for ( Map.Entry<String, Object> parameter : getParameters().entrySet() ) {
			annotationDescriptor.setValue( parameter.getKey(), parameter.getValue() );
		}

		try {
			return AnnotationFactory.create( annotationDescriptor );
		}
		catch ( RuntimeException e ) {
			throw log.getUnableToCreateAnnotationForConfiguredConstraintException( e );
		}
	}

	@Override
	public String toString() {
		return constraint.toString();
	}

	/**
	 * Provides access to the members of a {@link ConstraintDef}.
	 */
	private static class ConstraintDefAccessor<A extends Annotation>
			extends ConstraintDef<ConstraintDefAccessor<A>, A> {

		private ConstraintDefAccessor(ConstraintDef<?, A> original) {
			super( original );
		}

		private Class<A> getConstraintType() {
			return constraintType;
		}

		private Map<String, Object> getParameters() {
			return parameters;
		}
	}

	public ElementType getElementType() {
		return elementType;
	}
}
