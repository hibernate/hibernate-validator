/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.jandex.util;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;

import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.Type;

/**
 * Utility methods used for Jandex metadata retrieval.
 *
 * @author Marko Bekhta
 */
public final class JandexHelper {

	private static final Log log = LoggerFactory.make();

	private JandexHelper() {
	}

	/**
	 * @return an instance of {@link JandexHelper}
	 */
	public static JandexHelper getInstance() {
		return new JandexHelper();
	}

	/**
	 * Finds a class ({@link Class}) for a given name.
	 *
	 * @param name a name of a class to find
	 *
	 * @return a found {@link Class}
	 */
	public Class<?> getClassForName(String name) {
		// TODO: change how class are loaded (from GM: Using this one without passing a classloader may give us trouble due to classes not being visible to
		// the implicitly used loader. You can check out org.hibernate.validator.internal.util.privilegedactions.LoadClass and its usage as a starting point.
		try {
			return Class.forName( name.toString() );
		}
		catch (ClassNotFoundException e) {
			throw log.getFindingClassReflectionJandexIndexException( name.toString(), e );
		}
	}

	/**
	 * Indicates if the type is considered indexable (ie is an {@link Iterable}, an array or a {@link Map}).
	 *
	 * @param type the type to inspect.
	 *
	 * @return Returns {@code true} if the type is indexable.
	 */
	public boolean isIndexable(Type type) {
		// TODO: Can this property be somehow determined from a type parameter and without converting it to a class ???
		return ReflectionHelper.isIndexable( getClassForName( type.name().toString() ) );
	}

	/**
	 * Checks if given type is a {@link Map} implementation.
	 *
	 * @param type a type to check
	 *
	 * @return {@code true} if given type is an implementation of a {@link Map}, {@code false} otherwise
	 */
	public boolean isMap(Type type) {
		// TODO: Can this property be somehow determined from a type parameter and without converting it to a class ???
		return ReflectionHelper.isMap( getClassForName( type.name().toString() ) );
	}

	/**
	 * Checks if given type is an {@link Iterable} implementation.
	 *
	 * @param type a type to check
	 *
	 * @return {@code true} if given type is an implementation of an {@link Iterable}, {@code false} otherwise
	 */
	public boolean isIterable(Type type) {
		// TODO: Can this property be somehow determined from a type parameter and without converting it to a class ???
		return ReflectionHelper.isIterable( getClassForName( type.name().toString() ) );
	}

	/**
	 * Finds an annotation of a given type inside provided collection.
	 *
	 * @param annotations a collection of annotation in which to look for a provided annotation type
	 * @param aClass a type of annotation to look for.
	 *
	 * @return an {@link Optional < AnnotationInstance >} which will contain a found annotation, an empty {@link Optional}
	 * if none was found. Also if there are more than one annotation of provided type present in the collection there's
	 * no guarantee which one will be returned.
	 */
	public Optional<AnnotationInstance> findAnnotation(Collection<AnnotationInstance> annotations, Class<?> aClass) {
		return annotations.stream()
				.filter( annotation -> annotation.name().toString().equals( aClass.getName() ) )
				.findAny();
	}

	/**
	 * Converts annotation value to a value usable for {@link Annotation}.
	 *
	 * @param annotationValue annotation value to convert
	 *
	 * @return converted value
	 */
	public Object convertAnnotationValue(AnnotationValue annotationValue) {
		if ( AnnotationValue.Kind.ARRAY.equals( annotationValue.kind() ) ) {
			if ( AnnotationValue.Kind.CLASS.equals( annotationValue.componentKind() ) ) {
				return Arrays.stream( annotationValue.asClassArray() )
						.map( type -> getClassForName( type.name().toString() ) )
						.toArray( size -> new Class[size] );
			}
		}
		else if ( AnnotationValue.Kind.CLASS.equals( annotationValue.kind() ) ) {
			return getClassForName( annotationValue.asClass().name().toString() );
		}
		return annotationValue.value();
	}

	/**
	 * Checks if there's a {@link Valid} annotation present in the collection.
	 *
	 * @param annotations a collection of {@link AnnotationInstance}s to check in
	 *
	 * @return {@code true} if {@link Valid} is present in collection, {@code false} otherwise
	 */
	public boolean isCascading(Collection<AnnotationInstance> annotations) {
		return findAnnotation( annotations, Valid.class ).isPresent();
	}
}
