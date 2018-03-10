/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.reflect.Type;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;

/**
 * Property constraint location.
 *
 * @author Marko Bekhta
 */
public class PropertyConstraintLocation implements ConstraintLocation {

	/**
	 * The member the constraint was defined on.
	 */
	private final Property property;

	PropertyConstraintLocation(Property property) {
		this.property = property;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return property.getDeclaringClass();
	}

	@Override
	public Constrainable getConstrainable() {
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
		path.addPropertyNode( property.getPropertyName() );
	}

	@Override
	public Object getValue(Object parent) {
		return property.getValueFrom( parent );
	}

	@Override
	public String toString() {
		return "PropertyConstraintLocation [property=" + property + "]";
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		PropertyConstraintLocation that = (PropertyConstraintLocation) o;

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
