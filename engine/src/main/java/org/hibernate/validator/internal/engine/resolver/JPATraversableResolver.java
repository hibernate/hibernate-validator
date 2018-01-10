/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.resolver;

import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandles;

import javax.persistence.Persistence;
import javax.validation.Path;
import javax.validation.TraversableResolver;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * An implementation of {@code TraversableResolver} which is aware of JPA 2 and utilizes {@code PersistenceUtil} to
 * query the reachability of a property.
 * This resolver will be automatically enabled if JPA 2 is on the classpath and the default {@code TraversableResolver} is
 * used.
 *
 * @author Hardy Ferentschik
 * @author Emmanuel Bernard
 */
class JPATraversableResolver implements TraversableResolver {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	@Override
	public final boolean isReachable(Object traversableObject,
									 Path.Node traversableProperty,
									 Class<?> rootBeanType,
									 Path pathToTraversableObject,
									 ElementType elementType) {
		if ( LOG.isTraceEnabled() ) {
			LOG.tracef(
					"Calling isReachable on object %s with node name %s.",
					traversableObject,
					traversableProperty.getName()
			);
		}

		if ( traversableObject == null ) {
			return true;
		}

		return Persistence.getPersistenceUtil().isLoaded( traversableObject, traversableProperty.getName() );
	}

	@Override
	public final boolean isCascadable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
		return true;
	}
}
