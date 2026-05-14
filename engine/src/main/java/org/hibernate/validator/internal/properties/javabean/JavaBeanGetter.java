/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.properties.javabean;

import java.lang.reflect.Method;

import org.hibernate.accessor.HibernateAccessorFactory;
import org.hibernate.accessor.HibernateAccessorValueReader;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.properties.Getter;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;

/**
 * @author Marko Bekhta
 */
public class JavaBeanGetter extends JavaBeanMethod implements Getter {

	private final String propertyName;
	private final String resolvedPropertyName;

	/**
	 * The class of the method for which the constraint was defined.
	 * <p>
	 * It is usually the same as the declaring class of the method itself, except in the XML case when a user could
	 * declare a constraint for a specific subclass.
	 */
	private final Class<?> declaringClass;

	private final HibernateAccessorFactory accessorFactory;

	public JavaBeanGetter(Class<?> declaringClass, Method method, String propertyName, String resolvedPropertyName,
			HibernateAccessorFactory accessorFactory) {
		super( method );
		Contracts.assertNotNull( propertyName, "Property name cannot be null." );

		this.declaringClass = declaringClass;
		this.propertyName = propertyName;
		this.resolvedPropertyName = resolvedPropertyName;
		this.accessorFactory = accessorFactory;
	}

	@Override
	public String getPropertyName() {
		return propertyName;
	}

	@Override
	public String getResolvedPropertyName() {
		return resolvedPropertyName;
	}

	@Override
	public boolean hasReturnValue() {
		// getters should always have a return value
		return true;
	}

	@Override
	public boolean hasParameters() {
		// getters should never have parameters
		return false;
	}

	@Override
	public String getParameterName(ExecutableParameterNameProvider parameterNameProvider, int parameterIndex) {
		throw new IllegalStateException( "Getters may not have parameters" );
	}

	@Override
	public Class<?> getDeclaringClass() {
		return declaringClass;
	}

	@Override
	public ConstrainedElementKind getConstrainedElementKind() {
		return ConstrainedElementKind.GETTER;
	}

	@Override
	public HibernateAccessorValueReader<?> createAccessor() {
		return accessorFactory.valueReader( executable );
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || this.getClass() != o.getClass() ) {
			return false;
		}
		if ( !super.equals( o ) ) {
			return false;
		}

		JavaBeanGetter that = (JavaBeanGetter) o;

		return this.propertyName.equals( that.propertyName );
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + this.propertyName.hashCode();
		return result;
	}

}
