/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
