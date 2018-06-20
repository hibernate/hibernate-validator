/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.javabean;

import java.lang.reflect.Method;

import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.properties.Getter;
import org.hibernate.validator.internal.properties.PropertyAccessor;
import org.hibernate.validator.internal.properties.javabean.accessors.JavaBeanPropertyAccessorFactory;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * @author Marko Bekhta
 */
public class JavaBeanGetter extends JavaBeanMethod implements Getter {

	private final JavaBeanPropertyAccessorFactory propertyAccessorFactory;

	private final String name;

	/**
	 * The class of the method for which the constraint was defined.
	 * <p>
	 * It is usually the same as the declaring class of the method itself, except in the XML case when a user could
	 * declare a constraint for a specific subclass.
	 */
	private final Class<?> declaringClass;

	public JavaBeanGetter(JavaBeanPropertyAccessorFactory propertyAccessorFactory, Method method) {
		this( propertyAccessorFactory, method.getDeclaringClass(), method );
	}

	public JavaBeanGetter(JavaBeanPropertyAccessorFactory propertyAccessorFactory, Class<?> declaringClass, Method method) {
		super( method );

		this.propertyAccessorFactory = propertyAccessorFactory;

		this.name = ReflectionHelper.getPropertyName( method );
		this.declaringClass = declaringClass;
	}

	@Override
	public String getPropertyName() {
		return name;
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
	public PropertyAccessor createAccessor() {
		return propertyAccessorFactory.forGetter( executable );
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

		return this.name.equals( that.name );
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + this.name.hashCode();
		return result;
	}
}
