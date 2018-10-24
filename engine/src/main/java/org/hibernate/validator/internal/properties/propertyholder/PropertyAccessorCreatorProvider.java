/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.propertyholder;

import java.lang.invoke.MethodHandles;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.validator.internal.properties.propertyholder.map.MapPropertyAccessorCreator;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.GetInstancesFromServiceLoader;
import org.hibernate.validator.spi.propertyholder.PropertyAccessorCreator;

/**
 * @author Marko Bekhta
 */
public class PropertyAccessorCreatorProvider {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Set<PropertyAccessorCreator<?>> configuredPropertyCreators = new HashSet<>();

	public PropertyAccessorCreatorProvider() {
		//add default property creator for a Map
		configuredPropertyCreators.add( new MapPropertyAccessorCreator() );

		List<PropertyAccessorCreator> propertyAccessorCreators = run( GetInstancesFromServiceLoader.action(
				run( GetClassLoader.fromContext() ),
				PropertyAccessorCreator.class
		) );
		for ( PropertyAccessorCreator propertyAccessorCreator : propertyAccessorCreators ) {
			configuredPropertyCreators.add( propertyAccessorCreator );
		}
	}

	@SuppressWarnings("unchecked")
	public <T> PropertyAccessorCreator<T> getPropertyAccessorCreatorFor(Class<T> propertyHolderType) {

		Set<PropertyAccessorCreator> possibleCreators = configuredPropertyCreators
				.stream()
				.filter( el -> TypeHelper.isAssignable( el.getPropertyHolderType(), propertyHolderType ) )
				.collect( Collectors.toSet() );

		Set<PropertyAccessorCreator> creators = getMaximallySpecificPropertyAccessorCreators( possibleCreators );

		if ( creators.isEmpty() ) {
			throw LOG.getUnableToFindPropertyCreatorException( propertyHolderType );
		}
		else if ( creators.size() > 1 ) {
			throw LOG.getUnableToFinUniquedPropertyCreatorException( propertyHolderType );
		}
		else {
			return creators.iterator().next();
		}
	}

	private Set<PropertyAccessorCreator> getMaximallySpecificPropertyAccessorCreators(Set<PropertyAccessorCreator> possiblePropertyAccessorCreators) {
		Set<PropertyAccessorCreator> propertyAccessorCreators = CollectionHelper.newHashSet( possiblePropertyAccessorCreators.size() );

		for ( PropertyAccessorCreator creator : possiblePropertyAccessorCreators ) {
			if ( propertyAccessorCreators.isEmpty() ) {
				propertyAccessorCreators.add( creator );
				continue;
			}
			Iterator<PropertyAccessorCreator> candidatesIterator = propertyAccessorCreators.iterator();
			boolean isNewRoot = true;
			while ( candidatesIterator.hasNext() ) {
				PropertyAccessorCreator candidate = candidatesIterator.next();

				// we consider the strictly more specific value extractor so 2 value extractors for the same container
				// type should throw an error in the end if no other more specific value extractor is found.
				if ( candidate.getPropertyHolderType().equals( creator.getPropertyHolderType() ) ) {
					continue;
				}

				if ( TypeHelper.isAssignable( candidate.getPropertyHolderType(), creator.getPropertyHolderType() ) ) {
					candidatesIterator.remove();
				}
				else if ( TypeHelper.isAssignable( creator.getPropertyHolderType(), candidate.getPropertyHolderType() ) ) {
					isNewRoot = false;
				}
			}
			if ( isNewRoot ) {
				propertyAccessorCreators.add( creator );
			}
		}
		return propertyAccessorCreators;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 *
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}

}
