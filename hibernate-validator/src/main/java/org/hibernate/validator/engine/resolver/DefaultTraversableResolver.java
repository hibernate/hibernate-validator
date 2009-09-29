// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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
import java.security.AccessController;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;

import org.slf4j.Logger;

import org.hibernate.validator.util.LoggerFactory;
import org.hibernate.validator.util.NewInstance;
import org.hibernate.validator.util.LoadClass;

/**
 * A JPA 2 aware <code>TraversableResolver</code>.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class DefaultTraversableResolver implements TraversableResolver {

	private static final Logger log = LoggerFactory.make();

	/**
	 * Class to load to check whether JPA 2 is on the classpath.
	 */
	private static final String PERSISTENCE_UTIL_CLASS_NAME = "javax.persistence.PersistenceUtil";

	/**
	 * Class to instantiate in case JPA 2 is on the classpath.
	 */
	private static final String JPA_AWARE_TRAVERSABLE_RESOLVER_CLASS_NAME = "org.hibernate.validator.engine.resolver.JPATraversableResolver";

	/**
	 * A JPA 2 aware traversable resolver.
	 */
	private TraversableResolver jpaTraversableResolver;


	public DefaultTraversableResolver() {
		detectJPA();
	}

	/**
	 * Tries to load detect and load JPA.
	 */
	private void detectJPA() {
		try {
			loadClass( PERSISTENCE_UTIL_CLASS_NAME, this.getClass() );
			log.debug( "Found {} on classpath.", PERSISTENCE_UTIL_CLASS_NAME );
		}
		catch ( ValidationException e ) {
			log.debug(
					"Cannot find {} on classpath. All properties will per default be traversable.",
					PERSISTENCE_UTIL_CLASS_NAME
			);
			return;
		}

		try {
			@SuppressWarnings( "unchecked" )
			Class<? extends TraversableResolver> jpaAwareResolverClass = (Class<? extends TraversableResolver>)
					loadClass(JPA_AWARE_TRAVERSABLE_RESOLVER_CLASS_NAME, this.getClass() );
			NewInstance<? extends TraversableResolver> newInstance = NewInstance.action( jpaAwareResolverClass, "" );
			if ( System.getSecurityManager() != null ) {
				jpaTraversableResolver = AccessController.doPrivileged( newInstance );
			}
			else {
				jpaTraversableResolver = newInstance.run();
			}
			log.info(
					"Instantiated an instance of {}.", JPA_AWARE_TRAVERSABLE_RESOLVER_CLASS_NAME
			);
		}
		catch ( ValidationException e ) {
			log.info(
					"Unable to load or instanciate JPA aware resolver {}. All properties will per default be traversable.",
					JPA_AWARE_TRAVERSABLE_RESOLVER_CLASS_NAME
			);
		}
	}

	private Class<?> loadClass(String className, Class<?> caller) {
		LoadClass action = LoadClass.action( className, caller );
		if (System.getSecurityManager() != null) {
			return AccessController.doPrivileged( action );
		}
		else {
			return action.run();
		}
	}

	public boolean isReachable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
		return jpaTraversableResolver == null || jpaTraversableResolver.isReachable(
				traversableObject, traversableProperty, rootBeanType, pathToTraversableObject, elementType
		);
	}

	public boolean isCascadable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
		return jpaTraversableResolver == null || jpaTraversableResolver.isCascadable(
				traversableObject, traversableProperty, rootBeanType, pathToTraversableObject, elementType
		);
	}
}
