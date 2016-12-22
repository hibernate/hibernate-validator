/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.jandex;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.jandex.util.JandexHelper;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

/**
 * Builder for class level constrains that uses Jandex index.
 *
 * @author Marko Bekhta
 */
public class ClassConstraintsJandexBuilder extends AbstractConstrainedElementJandexBuilder {

	public ClassConstraintsJandexBuilder(ConstraintHelper constraintHelper, JandexHelper jandexHelper, AnnotationProcessingOptions annotationProcessingOptions,
			List<DotName> constraintAnnotations) {
		super( constraintHelper, jandexHelper, annotationProcessingOptions, constraintAnnotations );
	}

	/**
	 * Gets {@link ConstrainedType}s from a given class.
	 *
	 * @param classInfo a class in which to look for class level constraints
	 * @param beanClass same class as {@code classInfo} but represented as {@link Class}
	 *
	 * @return a stream of {@link ConstrainedElement}s that represent class type
	 */
	public Stream<ConstrainedElement> getClassConstraints(ClassInfo classInfo, Class<?> beanClass) {
		if ( annotationProcessingOptions.areClassLevelConstraintsIgnoredFor( beanClass ) ) {
			return Stream.empty();
		}

		return Stream.of( new ConstrainedType(
				ConfigurationSource.JANDEX,
				beanClass,
				findMetaConstraints( classInfo.classAnnotations(), beanClass ).collect( Collectors.toSet() )
		) );
	}

	/**
	 * Converts {@link ConstraintDescriptorImpl} to {@link MetaConstraint}.
	 *
	 * @param annotationInstances collection of annotations declared on a class
	 * @param beanClass a class under investigation
	 *
	 * @return a stream of {@link MetaConstraint}s for a given class describing class level constraints
	 */
	private Stream<MetaConstraint<?>> findMetaConstraints(Collection<AnnotationInstance> annotationInstances, Class<?> beanClass) {
		return findConstraints( annotationInstances, null )
				.map( descriptor -> createMetaConstraint( beanClass, descriptor ) );
	}

}
