/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties;

import java.lang.invoke.MethodHandles;
import java.util.Set;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.properties.ConstrainableExecutable;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;

/**
 * @author Marko Bekhta
 */
public class DefaultGetterPropertySelectionStrategy implements GetterPropertySelectionStrategy {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final String GETTER_PREFIX_GET = "get";
	private static final String GETTER_PREFIX_IS = "is";
	private static final String GETTER_PREFIX_HAS = "has";
	private static final String[] GETTER_PREFIXES = {
			GETTER_PREFIX_GET,
			GETTER_PREFIX_IS,
			GETTER_PREFIX_HAS
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
	 * {@code false} otherwise.
	 */
	@Override
	public boolean isGetter(ConstrainableExecutable executable) {
		if ( executable.getParameterTypes().length != 0 ) {
			return false;
		}

		String methodName = executable.getName();

		//<PropertyType> get<PropertyName>()
		if ( methodName.startsWith( GETTER_PREFIX_GET ) && executable.getReturnType() != void.class ) {
			return true;
		}
		//boolean is<PropertyName>()
		else if ( methodName.startsWith( GETTER_PREFIX_IS ) && executable.getReturnType() == boolean.class ) {
			return true;
		}
		//boolean has<PropertyName>()
		else if ( methodName.startsWith( GETTER_PREFIX_HAS ) && executable.getReturnType() == boolean.class ) {
			return true;
		}

		return false;
	}

	@Override
	public String getPropertyName(ConstrainableExecutable executable) {
		Contracts.assertNotNull( executable, "executable cannot be null" );

		String methodName = executable.getName();
		for ( String prefix : GETTER_PREFIXES ) {
			if ( methodName.startsWith( prefix ) ) {
				return StringHelper.decapitalize( methodName.substring( prefix.length() ) );
			}
		}
		throw LOG.getIllegalArgumentException( "Property name cannot be constructed for a given method " + executable.getName() );
	}

	@Override
	public Set<String> getGetterMethodNameCandidates(String propertyName) {
		Contracts.assertNotEmpty( propertyName, "Name of a property must not be empty" );

		Set<String> nameCandidates = CollectionHelper.newHashSet( GETTER_PREFIXES.length );
		for ( String prefix : GETTER_PREFIXES ) {
			nameCandidates.add( prefix + Character.toUpperCase( propertyName.charAt( 0 ) ) + propertyName.substring( 1 ) );
		}
		return nameCandidates;
	}
}
