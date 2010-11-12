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
package org.hibernate.validator.engine.resolver;

import java.lang.annotation.ElementType;
import javax.persistence.Persistence;
import javax.validation.Path;
import javax.validation.TraversableResolver;

import org.slf4j.Logger;

import org.hibernate.validator.util.LoggerFactory;

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
	private static final Logger log = LoggerFactory.make();

	public final boolean isReachable(Object traversableObject,
							   Path.Node traversableProperty,
							   Class<?> rootBeanType,
							   Path pathToTraversableObject,
							   ElementType elementType) {
		if ( log.isTraceEnabled() ) {
			log.trace(
					"Calling isReachable on object {} with node name {}",
					traversableObject,
					traversableProperty.getName()
			);
		}

		// we have to check traversableProperty.getName() against null to check the root gets validated (see HV-266)
		// also check the element type, if it is ElementType.TYPE then we don't have to call is reachable since we have
		// a class level constraint (HV-305)
		if ( traversableObject == null || traversableProperty.getName() == null
				|| ElementType.TYPE.equals( elementType ) ) {
			return true;
		}
		else {
			return Persistence.getPersistenceUtil().isLoaded( traversableObject, traversableProperty.getName() );
		}
	}

	public final boolean isCascadable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
		return true;
	}
}
