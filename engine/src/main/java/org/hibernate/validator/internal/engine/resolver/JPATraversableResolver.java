/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.resolver;

import java.lang.annotation.ElementType;
import javax.persistence.Persistence;
import javax.validation.Path;
import javax.validation.TraversableResolver;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * An implementation of {@code TraversableResolver} which is aware of JPA 2 and utilizes {@code PersistenceUtil} to get
 * query the reachability of a property.
 * This resolver will be automatically enabled if JPA 2 is on the classpath and the {@code DefaultTraversableResolver} is
 * used.
 *
 * @author Hardy Ferentschik
 * @author Emmanuel Bernard
 */
public class JPATraversableResolver implements TraversableResolver {
	private static final Log log = LoggerFactory.make();

	public final boolean isReachable(Object traversableObject,
									 Path.Node traversableProperty,
									 Class<?> rootBeanType,
									 Path pathToTraversableObject,
									 ElementType elementType) {
		if ( log.isTraceEnabled() ) {
			log.tracef(
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

	public final boolean isCascadable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
		return true;
	}
}
