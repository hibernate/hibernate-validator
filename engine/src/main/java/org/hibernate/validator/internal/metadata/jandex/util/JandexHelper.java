/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.jandex.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.ValidationException;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

/**
 * Utility methods used for Jandex metadata retrieval.
 *
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public final class JandexHelper {

	private static final Log LOG = LoggerFactory.make();

	private final ClassLoader classLoader;

	public JandexHelper(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public JandexHelper() {
		this( HibernateValidator.class.getClassLoader() );
	}

	/**
	 * Returns a class ({@link Class}) for a given class {@link DotName}.
	 */
	public Class<?> getClassForName(DotName className) {
		// TODO: change how class are loaded (from GM: Using this one without passing a classloader may give us trouble due to classes not being visible to
		// the implicitly used loader. You can check out org.hibernate.validator.internal.util.privilegedactions.LoadClass and its usage as a starting point.
		try {
			return run( LoadClass.action( className.toString(), classLoader, true ) );
		}
		catch (ValidationException e) {
			throw LOG.getUnableToFindClassReferencedInJandexIndex( className.toString(), e );
		}
	}

	/**
	 * Indicates if the type is considered indexable (ie is an {@link Iterable}, an array or a {@link Map}).
	 *
	 * @param type the type to inspect.
	 * @return Returns {@code true} if the type is indexable.
	 */
	public boolean isIndexable(Type type) {
		// TODO: Can this property be somehow determined from a type parameter and without converting it to a class ???
		return ReflectionHelper.isIndexable( getClassForName( type.name() ) );
	}

	/**
	 * Checks if given type is a {@link Map} implementation.
	 *
	 * @param type a type to check
	 * @return {@code true} if given type is an implementation of a {@link Map}, {@code false} otherwise
	 */
	public boolean isMap(Type type) {
		// TODO: Can this property be somehow determined from a type parameter and without converting it to a class ???
		return ReflectionHelper.isMap( getClassForName( type.name() ) );
	}

	/**
	 * Checks if given type is an {@link Iterable} implementation.
	 *
	 * @param type a type to check
	 * @return {@code true} if given type is an implementation of an {@link Iterable}, {@code false} otherwise
	 */
	public boolean isIterable(Type type) {
		// TODO: Can this property be somehow determined from a type parameter and without converting it to a class ???
		return ReflectionHelper.isIterable( getClassForName( type.name() ) );
	}

	/**
	 * Finds an annotation of a given type inside the provided collection of annotations.
	 */
	public Optional<AnnotationInstance> findAnnotation(Collection<AnnotationInstance> annotations, Class<?> clazz) {
		return annotations.stream()
				.filter( annotation -> annotation.name().toString().equals( clazz.getName() ) )
				.findAny();
	}

	/**
	 * Converts the {@link AnnotationValue} to a value usable by an {@link AnnotationDescriptor}.
	 */
	public Object convertAnnotationValue(AnnotationValue annotationValue) {
		if ( AnnotationValue.Kind.ARRAY.equals( annotationValue.kind() ) ) {
			if ( AnnotationValue.Kind.CLASS.equals( annotationValue.componentKind() ) ) {
				return Arrays.stream( annotationValue.asClassArray() )
						.map( type -> getClassForName( type.name() ) )
						.toArray( size -> new Class[size] );
			}
		}
		else if ( AnnotationValue.Kind.CLASS.equals( annotationValue.kind() ) ) {
			return getClassForName( annotationValue.asClass().name() );
		}
		return annotationValue.value();
	}

	/**
	 * Checks if there's a {@link Valid} annotation present in the collection.
	 */
	public boolean isCascading(Collection<AnnotationInstance> annotations) {
		return findAnnotation( annotations, Valid.class ).isPresent();
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
