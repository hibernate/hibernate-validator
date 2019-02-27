/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.reflect.Type;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.properties.PropertyAccessor;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;

/**
 * An abstract property constraint location.
 *
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public abstract class AbstractPropertyConstraintLocation<T extends Property> implements ConstraintLocation {

	/**
	 * The property the constraint was defined on.
	 */
	private final T property;

	private final PropertyAccessor propertyAccessor;

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
	public void appendTo(ExecutableParameterNameProvider parameterNameProvider, PathImpl path) {
		path.addPropertyNode( property.getResolvedPropertyName() );
	}

	@Override
	public Object getValue(Object parent) {
		return propertyAccessor.getValueFrom( parent );
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
