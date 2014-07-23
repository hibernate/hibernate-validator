/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;

import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetMethod;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.hibernate.validator.internal.util.privilegedactions.NewInstance;

/**
 * A JPA 2 aware {@code TraversableResolver}.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class DefaultTraversableResolver implements TraversableResolver {

	private static final Log log = LoggerFactory.make();

	/**
	 * Class to load to check whether JPA is on the classpath.
	 */
	private static final String PERSISTENCE_CLASS_NAME = "javax.persistence.Persistence";

	/**
	 * Method to check whether the found {@code Persistence} class is of the version 2
	 */
	private static final String PERSISTENCE_UTIL_METHOD = "getPersistenceUtil";

	/**
	 * Class to instantiate in case JPA 2 is on the classpath.
	 */
	private static final String JPA_AWARE_TRAVERSABLE_RESOLVER_CLASS_NAME = "org.hibernate.validator.internal.engine.resolver.JPATraversableResolver";

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
		// check whether we have Persistence on the classpath
		Class<?> persistenceClass;
		try {
			persistenceClass = run( LoadClass.action( PERSISTENCE_CLASS_NAME, this.getClass() ) );
		}
		catch ( ValidationException e ) {
			log.debugf(
					"Cannot find %s on classpath. Assuming non JPA 2 environment. All properties will per default be traversable.",
					PERSISTENCE_CLASS_NAME
			);
			return;
		}

		// check whether Persistence contains getPersistenceUtil
		Method persistenceUtilGetter = run( GetMethod.action( persistenceClass, PERSISTENCE_UTIL_METHOD ) );
		if ( persistenceUtilGetter == null ) {
			log.debugf(
					"Found %s on classpath, but no method '%s'. Assuming JPA 1 environment. All properties will per default be traversable.",
					PERSISTENCE_CLASS_NAME,
					PERSISTENCE_UTIL_METHOD
			);
			return;
		}

		// try to invoke the method to make sure that we are dealing with a complete JPA2 implementation
		// unfortunately there are several incomplete implementations out there (see HV-374)
		try {
			Object persistence = run( NewInstance.action( persistenceClass, "persistence provider" ) );
			ReflectionHelper.getValue( persistenceUtilGetter, persistence );
		}
		catch ( Exception e ) {
			log.debugf(
					"Unable to invoke %s.%s. Inconsistent JPA environment. All properties will per default be traversable.",
					PERSISTENCE_CLASS_NAME,
					PERSISTENCE_UTIL_METHOD
			);
		}

		log.debugf(
				"Found %s on classpath containing '%s'. Assuming JPA 2 environment. Trying to instantiate JPA aware TraversableResolver",
				PERSISTENCE_CLASS_NAME,
				PERSISTENCE_UTIL_METHOD
		);

		try {
			@SuppressWarnings("unchecked")
			Class<? extends TraversableResolver> jpaAwareResolverClass = (Class<? extends TraversableResolver>)
					run( LoadClass.action( JPA_AWARE_TRAVERSABLE_RESOLVER_CLASS_NAME, this.getClass() ) );
			jpaTraversableResolver = run( NewInstance.action( jpaAwareResolverClass, "" ) );
			log.debugf(
					"Instantiated JPA aware TraversableResolver of type %s.", JPA_AWARE_TRAVERSABLE_RESOLVER_CLASS_NAME
			);
		}
		catch ( ValidationException e ) {
			log.debugf(
					"Unable to load or instantiate JPA aware resolver %s. All properties will per default be traversable.",
					JPA_AWARE_TRAVERSABLE_RESOLVER_CLASS_NAME
			);
		}
	}

	@Override
	public boolean isReachable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
		return jpaTraversableResolver == null || jpaTraversableResolver.isReachable(
				traversableObject, traversableProperty, rootBeanType, pathToTraversableObject, elementType
		);
	}

	@Override
	public boolean isCascadable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
		return jpaTraversableResolver == null || jpaTraversableResolver.isCascadable(
				traversableObject, traversableProperty, rootBeanType, pathToTraversableObject, elementType
		);
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
