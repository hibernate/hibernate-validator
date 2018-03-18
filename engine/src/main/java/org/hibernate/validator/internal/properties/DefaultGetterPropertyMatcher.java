/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties;

import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.spi.properties.ConstrainableExecutable;
import org.hibernate.validator.spi.properties.GetterPropertyMatcher;

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
	 * {@code boolean} (HV-specific, not mandated by the JavaBeans spec).</li>
	 * </ul>
	 *
	 * @param executable The executable of interest.
	 *
	 * @return {@code true}, if the given executable is a JavaBean getter method,
	 * 		{@code false} otherwise.
	 */
	@Override
	public boolean isProperty(ConstrainableExecutable executable) {
		if ( executable.getParameterTypes().length != 0 ) {
			return false;
		}

		String methodName = executable.getName();

		//<PropertyType> get<PropertyName>()
		if ( methodName.startsWith( PROPERTY_ACCESSOR_PREFIX_GET ) && executable.getReturnType() != void.class ) {
			return true;
		}
		//boolean is<PropertyName>()
		else if ( methodName.startsWith( PROPERTY_ACCESSOR_PREFIX_IS ) && executable.getReturnType() == boolean.class ) {
			return true;
		}
		//boolean has<PropertyName>()
		else if ( methodName.startsWith( PROPERTY_ACCESSOR_PREFIX_HAS ) && executable.getReturnType() == boolean.class ) {
			return true;
		}

		return false;
	}

	@Override
	public String getPropertyName(ConstrainableExecutable executable) {
		String name = null;
		String methodName = executable.getName();
		for ( String prefix : PROPERTY_ACCESSOR_PREFIXES ) {
			if ( methodName.startsWith( prefix ) ) {
				name = StringHelper.decapitalize( methodName.substring( prefix.length() ) );
			}
		}
		return name;
	}
}
