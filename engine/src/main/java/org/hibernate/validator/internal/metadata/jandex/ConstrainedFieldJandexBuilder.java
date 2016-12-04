/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.jandex;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.Valid;

import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.jandex.util.JandexHelper;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;

/**
 * Builder for constrained fields that uses Jandex index.
 *
 * @author Marko Bekhta
 */
public class ConstrainedFieldJandexBuilder extends AbstractConstrainedElementJandexBuilder {

	private ConstrainedFieldJandexBuilder(ConstraintHelper constraintHelper, JandexHelper jandexHelper) {
		super( constraintHelper, jandexHelper );
	}

	/**
	 * Creates an instance of a {@link ConstrainedFieldJandexBuilder}.
	 *
	 * @param constraintHelper an instance of {@link ConstraintHelper}
	 *
	 * @return a new instance of {@link ConstrainedFieldJandexBuilder}
	 */
	public static ConstrainedFieldJandexBuilder getInstance(ConstraintHelper constraintHelper, JandexHelper jandexHelper) {
		return new ConstrainedFieldJandexBuilder( constraintHelper, jandexHelper );
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
		Set<MetaConstraint<?>> constraints = findConstraints( fieldInfo.annotations(), field ).collect( Collectors.toSet() );

		boolean isCascading = findAnnotation( fieldInfo.annotations(), Valid.class ).isPresent();
		Set<MetaConstraint<?>> typeArgumentsConstraints = findTypeAnnotationConstraintsForMember(
				new MemberInformation(
						fieldInfo.type(),
						fieldInfo.name(),
						field,
						beanClass
				),
				isCascading
		).collect( Collectors.toSet() );

		CommonConstraintInformation commonInformation = findCommonConstraintInformation( fieldInfo.type(), fieldInfo.annotations(),
				!typeArgumentsConstraints.isEmpty(), isCascading
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

	//	/**
	//	 * Converts a stream of constraint annotations to a set of {@link MetaConstraint}s.
	//	 *
	//	 * @param beanClass a {@link Class} in which field is located
	//	 * @param fieldInfo a field on which constraints are defined
	//	 *
	//	 * @return a set of {@link MetaConstraint}s based on provided parameters
	//	 */
	//	private Set<MetaConstraint<?>> convertToMetaConstraints(Class<?> beanClass, FieldInfo fieldInfo) {
	//		Field field = findField( beanClass, fieldInfo );
	//		return findConstraints( fieldInfo, field )
	//				.map( descriptor -> createMetaConstraint( field, descriptor ) )
	//				.collect( Collectors.<MetaConstraint<?>>toSet() );
	//	}
	//
	//	/**
	//	 * Finds all constraint annotations defined for the given field and returns them as a stream of
	//	 * constraint descriptors.
	//	 *
	//	 * @param fieldInfo a {@link FieldInfo} representation of a given field.
	//	 * @param field a {@link Field} representation of a given field.
	//	 *
	//	 * @return A stream of constraint descriptors for all constraint specified for the given member.
	//	 */
	//	private Stream<ConstraintDescriptorImpl<?>> findConstraints(FieldInfo fieldInfo, Field field) {
	//		return findConstrainAnnotations( fieldInfo.annotations() )
	//				.flatMap( annotationInstance -> findConstraintAnnotations( field, annotationInstance ) );
	//	}

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
					String.format( "Wasn't able to find a filed for a given parameters. Field name - %s in bean - %s", fieldInfo
							.name(), beanClass.getName() ),
					e
			);
		}
	}
}
