package org.hibernate.validation.engine.resolver;

import java.lang.annotation.ElementType;
import javax.validation.Path;
import javax.validation.TraversableResolver;

import org.slf4j.Logger;

import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.ReflectionHelper;

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
	private static final String JPA_AWARE_TRAVERSABLE_RESOLVER_CLASS_NAME = "org.hibernate.validation.engine.resolver.JPATraversableResolver";

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
			ReflectionHelper.classForName( PERSISTENCE_UTIL_CLASS_NAME, this.getClass() );
			log.debug( "Found {} on classpath.", PERSISTENCE_UTIL_CLASS_NAME );
		}
		catch ( ClassNotFoundException e ) {
			log.debug(
					"Cannot find {} on classpath. All properties will per default be traversable.",
					PERSISTENCE_UTIL_CLASS_NAME
			);
			return;
		}

		try {
			Class jpaAwareResolverClass = ReflectionHelper.classForName(
					JPA_AWARE_TRAVERSABLE_RESOLVER_CLASS_NAME, this.getClass()
			);
			jpaTraversableResolver = ( TraversableResolver ) jpaAwareResolverClass.newInstance();
			log.info(
					"Instantiated an instance of {}.", JPA_AWARE_TRAVERSABLE_RESOLVER_CLASS_NAME
			);
		}
		catch ( ClassNotFoundException e ) {
			log.info(
					"Unable to load JPA aware resolver {}. All properties will per default be traversable.",
					JPA_AWARE_TRAVERSABLE_RESOLVER_CLASS_NAME
			);
		}
		catch ( IllegalAccessException e ) {
			log.info(
					"Unable to instantiate JPA aware resolver {}. All properties will per default be traversable.",
					JPA_AWARE_TRAVERSABLE_RESOLVER_CLASS_NAME
			);
		}
		catch ( InstantiationException e ) {
			log.info(
					"Unable to instantiate JPA aware resolver {}. All properties will per default be traversable.",
					JPA_AWARE_TRAVERSABLE_RESOLVER_CLASS_NAME
			);
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
