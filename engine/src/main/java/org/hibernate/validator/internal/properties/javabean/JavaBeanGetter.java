/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.javabean;

import static org.hibernate.validator.internal.util.TypeHelper.isHibernateValidatorEnhancedBean;

import java.lang.reflect.Method;

import org.hibernate.validator.engine.HibernateValidatorEnhancedBean;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.properties.Getter;
import org.hibernate.validator.internal.properties.PropertyAccessor;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.actions.GetDeclaredMethod;

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

	public JavaBeanGetter(Class<?> declaringClass, Method method, String propertyName, String resolvedPropertyName) {
		super( method );
		Contracts.assertNotNull( propertyName, "Property name cannot be null." );

		this.declaringClass = declaringClass;
		this.propertyName = propertyName;
		this.resolvedPropertyName = resolvedPropertyName;
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
	public PropertyAccessor createAccessor() {
		if ( isHibernateValidatorEnhancedBean( executable.getDeclaringClass() ) ) {
			return new EnhancedBeanGetterAccessor( executable.getName() );
		}
		else {
			return new GetterAccessor( executable );
		}

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

	private static class EnhancedBeanGetterAccessor implements PropertyAccessor {
		private final String getterFullName;

		private EnhancedBeanGetterAccessor(final String getterFullName) {
			this.getterFullName = getterFullName;
		}

		@Override
		public Object getValueFrom(Object bean) {
			// we don't do an instanceof check here as it should already be applied when the accessor was created.
			return ( (HibernateValidatorEnhancedBean) bean ).$$_hibernateValidator_getGetterValue( getterFullName );
		}
	}

	private static class GetterAccessor implements PropertyAccessor {

		private Method accessibleGetter;

		private GetterAccessor(Method getter) {
			this.accessibleGetter = getAccessible( getter );
		}

		@Override
		public Object getValueFrom(Object bean) {
			return ReflectionHelper.getValue( accessibleGetter, bean );
		}
	}

	/**
	 * Returns an accessible copy of the given method.
	 */
	private static Method getAccessible(Method original) {
		Class<?> clazz = original.getDeclaringClass();

		return GetDeclaredMethod.andMakeAccessible( clazz, original.getName() );
	}

}
