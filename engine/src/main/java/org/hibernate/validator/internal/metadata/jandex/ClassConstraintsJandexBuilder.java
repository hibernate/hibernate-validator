/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.jandex;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.jandex.util.JandexHelper;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

/**
 * Builder used to extract class constraints from the Jandex index.
 *
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public class ClassConstraintsJandexBuilder extends AbstractConstrainedElementJandexBuilder {

	public ClassConstraintsJandexBuilder(ConstraintHelper constraintHelper, JandexHelper jandexHelper, AnnotationProcessingOptions annotationProcessingOptions,
			List<DotName> constraintAnnotations) {
		super( constraintHelper, jandexHelper, annotationProcessingOptions, constraintAnnotations );
	}

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

	private Stream<MetaConstraint<?>> findMetaConstraints(Collection<AnnotationInstance> annotationInstances, Class<?> beanClass) {
		return findConstraints( annotationInstances, null )
				.map( descriptor -> createMetaConstraint( beanClass, descriptor ) );
	}

	private <A extends Annotation> MetaConstraint<?> createMetaConstraint(Class<?> declaringClass, ConstraintDescriptorImpl<A> descriptor) {
		return new MetaConstraint<>( descriptor, ConstraintLocation.forClass( declaringClass ) );
	}

}
