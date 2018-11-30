/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.reflect.Type;

import org.hibernate.validator.engine.HibernateConstrainedType;
import org.hibernate.validator.internal.engine.constrainedtype.JavaBeanConstrainedType;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.TypeHelper;

/**
 * Bean constraint location (i.e. for a class-level constraint).
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
class BeanConstraintLocation implements ConstraintLocation {

	/**
	 * The type hosting this location.
	 */
	private final HibernateConstrainedType<?> declaringConstrainedType;

	/**
	 * The type to be used for validator resolution for constraints at this location.
	 */
	private final Type typeForValidatorResolution;

	BeanConstraintLocation(Class<?> declaringClass) {
		this.declaringConstrainedType = new JavaBeanConstrainedType<>( declaringClass );

		// HV-623 - create a ParameterizedType in case the class has type parameters. Needed for constraint validator
		// resolution (HF)
		typeForValidatorResolution = declaringClass.getTypeParameters().length == 0 ?
				declaringClass :
				TypeHelper.parameterizedType( declaringClass, declaringClass.getTypeParameters() );
	}

	@Override
	public HibernateConstrainedType<?> getDeclaringConstrainedType() {
		return declaringConstrainedType;
	}

	@Override
	public Constrainable getConstrainable() {
		return null;
	}

	@Override
	public Type getTypeForValidatorResolution() {
		return typeForValidatorResolution;
	}

	@Override
	public void appendTo(ExecutableParameterNameProvider parameterNameProvider, PathImpl path) {
		path.addBeanNode();
	}

	@Override
	public Object getValue(Object parent) {
		return parent;
	}

	@Override
	public ConstraintLocationKind getKind() {
		return ConstraintLocationKind.TYPE;
	}

	@Override
	public String toString() {
		return "BeanConstraintLocation [declaringClass=" + declaringConstrainedType + ", typeForValidatorResolution=" + typeForValidatorResolution + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( declaringConstrainedType == null ) ? 0 : declaringConstrainedType.hashCode() );
		result = prime * result + ( ( typeForValidatorResolution == null ) ? 0 : typeForValidatorResolution.hashCode() );
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		BeanConstraintLocation other = (BeanConstraintLocation) obj;
		if ( declaringConstrainedType == null ) {
			if ( other.declaringConstrainedType != null ) {
				return false;
			}
		}
		else if ( !declaringConstrainedType.equals( other.declaringConstrainedType ) ) {
			return false;
		}
		if ( typeForValidatorResolution == null ) {
			if ( other.typeForValidatorResolution != null ) {
				return false;
			}
		}
		else if ( !typeForValidatorResolution.equals( other.typeForValidatorResolution ) ) {
			return false;
		}
		return true;
	}
}
