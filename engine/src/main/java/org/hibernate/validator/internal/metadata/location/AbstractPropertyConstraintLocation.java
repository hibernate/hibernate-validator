/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;

import org.hibernate.accessor.HibernateAccessorException;
import org.hibernate.accessor.HibernateAccessorValueReader;
import org.hibernate.validator.internal.engine.path.MutablePath;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * An abstract property constraint location.
 *
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public abstract class AbstractPropertyConstraintLocation<T extends Property> implements ConstraintLocation {
	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	/**
	 * The property the constraint was defined on.
	 */
	private final T property;

	private final HibernateAccessorValueReader<?> propertyAccessor;

	AbstractPropertyConstraintLocation(T property) {
		this.property = property;
		this.propertyAccessor = property.createAccessor();
	}

	@Override
	public Class<?> getDeclaringClass() {
		return property.getDeclaringClass();
	}

	@Override
	public T getConstrainable() {
		return property;
	}

	public String getPropertyName() {
		return property.getPropertyName();
	}

	@Override
	public Type getTypeForValidatorResolution() {
		return property.getTypeForValidatorResolution();
	}

	@Override
	public void appendTo(ExecutableParameterNameProvider parameterNameProvider, MutablePath path) {
		path.addPropertyNode( property.getResolvedPropertyName() );
	}

	@Override
	public void applyTo(ExecutableParameterNameProvider parameterNameProvider, MutablePath path) {
		path.getLeafNode().resetAsProperty( property.getResolvedPropertyName() );
	}

	@Override
	public Object getValue(Object parent) {
		try {
			return propertyAccessor.get( parent );
		}
		catch (HibernateAccessorException e) {
			throw LOG.getUnexpectedExceptionAccessingBean( e );
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [property=" + property + "]";
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		AbstractPropertyConstraintLocation<?> that = (AbstractPropertyConstraintLocation<?>) o;

		if ( !property.equals( that.property ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return property.hashCode();
	}
}
