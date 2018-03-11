/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.properties.GetterPropertyMatcher;

/**
 * @author Marko Bekhta
 */
public class DefaultGetterPropertyMatcher implements GetterPropertyMatcher {

	private static final String PROPERTY_ACCESSOR_PREFIX_GET = "get";
	private static final String PROPERTY_ACCESSOR_PREFIX_IS = "is";
	private static final String PROPERTY_ACCESSOR_PREFIX_HAS = "has";
	public static final String[] PROPERTY_ACCESSOR_PREFIXES = {
			PROPERTY_ACCESSOR_PREFIX_GET,
			PROPERTY_ACCESSOR_PREFIX_IS,
			PROPERTY_ACCESSOR_PREFIX_HAS
	};

	/**
	 * Checks whether the given executable is a valid JavaBean getter method, which
	 * is the case if
	 * <ul>
	 * <li>its name starts with "get" and it has a return type but no parameter or</li>
	 * <li>its name starts with "is", it has no parameter and is returning
	 * {@code boolean} or</li>
	 * <li>its name starts with "has", it has no parameter and is returning
	 * {@code boolean} (HV-specific, not mandated by JavaBeans spec).</li>
	 * </ul>
	 *
	 * @param executable The executable of interest.
	 *
	 * @return {@code true}, if the given executable is a JavaBean getter method,
	 * 		{@code false} otherwise.
	 */
	@Override
	public boolean isProperty(Executable executable) {
		if ( executable instanceof Constructor ) {
			return false;
		}
		if ( executable.getParameterTypes().length != 0 ) {
			return false;
		}
		Method method = (Method) executable;

		String methodName = method.getName();

		//<PropertyType> get<PropertyName>()
		if ( methodName.startsWith( PROPERTY_ACCESSOR_PREFIX_GET ) && method.getReturnType() != void.class ) {
			return true;
		}
		//boolean is<PropertyName>()
		else if ( methodName.startsWith( PROPERTY_ACCESSOR_PREFIX_IS ) && method.getReturnType() == boolean.class ) {
			return true;
		}
		//boolean has<PropertyName>()
		else if ( methodName.startsWith( PROPERTY_ACCESSOR_PREFIX_HAS ) && method.getReturnType() == boolean.class ) {
			return true;
		}

		return false;
	}

	@Override
	public String getPropertyName(Method method) {
		String name = null;
		String methodName = method.getName();
		for ( String prefix : PROPERTY_ACCESSOR_PREFIXES ) {
			if ( methodName.startsWith( prefix ) ) {
				name = StringHelper.decapitalize( methodName.substring( prefix.length() ) );
			}
		}
		return name;
	}

	@Override
	public Set<String> getPossibleMethodNamesForProperty(String propertyName) {
		char[] chars = propertyName.toCharArray();
		chars[0] = Character.toUpperCase( chars[0] );
		String propertyMethodEnding = new String( chars );
		return Arrays.stream( PROPERTY_ACCESSOR_PREFIXES )
				.map( prefix -> prefix + propertyMethodEnding )
				.collect( Collectors.toSet() );
	}
}
