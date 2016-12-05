/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.jandex;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;
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
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;

/**
 * Builder for constrained fields that uses Jandex index.
 *
 * @author Marko Bekhta
 */
public class ConstrainedFieldJandexBuilder extends AbstractConstrainedElementJandexBuilder {

	private ConstrainedFieldJandexBuilder(ConstraintHelper constraintHelper, JandexHelper jandexHelper,
			AnnotationProcessingOptions annotationProcessingOptions) {
		super( constraintHelper, jandexHelper, annotationProcessingOptions );
	}

	/**
	 * Creates an instance of a {@link ConstrainedFieldJandexBuilder}.
	 *
	 * @param constraintHelper an instance of {@link ConstraintHelper}
	 * @param jandexHelper an instance of {@link JandexHelper}
	 *
	 * @return a new instance of {@link ConstrainedFieldJandexBuilder}
	 */
	public static ConstrainedFieldJandexBuilder getInstance(ConstraintHelper constraintHelper, JandexHelper jandexHelper,
			AnnotationProcessingOptions annotationProcessingOptions) {
		return new ConstrainedFieldJandexBuilder( constraintHelper, jandexHelper, annotationProcessingOptions );
	}

	/**
	 * Gets {@link ConstrainedField}s from a given class.
	 *
	 * @param classInfo a class in which to look for constrained fileds
	 * @param beanClass same class as {@code classInfo} but represented as {@link Class}
	 *
	 * @return a stream of {@link ConstrainedElement}s that represents fields
	 */
	public Stream<ConstrainedElement> getConstrainedFields(ClassInfo classInfo, Class<?> beanClass) {
		return classInfo.fields().stream()
				.map( fieldInfo -> toConstrainedField( beanClass, fieldInfo ) );
	}

	/**
	 * Converts given field to {@link ConstrainedField}.
	 *
	 * @param beanClass a {@link Class} where {@code fieldInfo} is located
	 * @param fieldInfo a field to convert
	 *
	 * @return {@link ConstrainedField} representation of a given field
	 */
	private ConstrainedField toConstrainedField(Class<?> beanClass, FieldInfo fieldInfo) {
		Field field = findField( beanClass, fieldInfo );
		Set<MetaConstraint<?>> constraints = findMetaConstraints( fieldInfo.annotations(), field ).collect( Collectors.toSet() );

		boolean isCascading = jandexHelper.isCascading( fieldInfo.annotations() );
		Set<MetaConstraint<?>> typeArgumentsConstraints = findTypeAnnotationConstraintsForMember(
				new MemberInformation(
						fieldInfo.type(),
						fieldInfo.name(),
						field,
						ConstraintLocation.forProperty( field ),
						beanClass
				),
				isCascading
		).collect( Collectors.toSet() );

		CommonConstraintInformation commonInformation = findCommonConstraintInformation(
				fieldInfo.type(),
				fieldInfo.annotations(),
				!typeArgumentsConstraints.isEmpty(),
				isCascading
		);
		return new ConstrainedField(
				ConfigurationSource.JANDEX,
				findField( beanClass, fieldInfo ),
				constraints,
				typeArgumentsConstraints,
				commonInformation.getGroupConversions(),
				commonInformation.isCascading(),
				commonInformation.getUnwrapMode()
		);
	}

	/**
	 * Converts {@link ConstraintDescriptorImpl} to {@link MetaConstraint}.
	 *
	 * @param annotationInstances collection of annotations declared on a field
	 * @param field a field under investigation
	 *
	 * @return a stream of {@link MetaConstraint}s for a given field
	 */
	private Stream<MetaConstraint<?>> findMetaConstraints(Collection<AnnotationInstance> annotationInstances, Field field) {
		return findConstraints( annotationInstances, field )
				.map( descriptor -> createMetaConstraint( field, descriptor ) );
	}

	/**
	 * Find a {@link Field} by given bean class and field information.
	 *
	 * @param beanClass a bean class in which to look for the field
	 * @param fieldInfo {@link FieldInfo} representing information about the field
	 *
	 * @return a {@link Field} for the given information
	 *
	 * @throws IllegalArgumentException if no filed was found for a given bean class and field information
	 */
	private Field findField(Class<?> beanClass, FieldInfo fieldInfo) {
		try {
			return beanClass.getDeclaredField( fieldInfo.name() );
		}
		catch (NoSuchFieldException e) {
			throw new IllegalArgumentException(
					String.format( "Wasn't able to find a filed for a given parameters. Field name - %s in bean - %s", fieldInfo.name(), beanClass.getName() ),
					e
			);
		}
	}
}
