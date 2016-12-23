/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.jandex;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;

/**
 * Builder used to extract field constraints from the Jandex index.
 *
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public class ConstrainedFieldJandexBuilder extends AbstractConstrainedElementJandexBuilder {

	public ConstrainedFieldJandexBuilder(ConstraintHelper constraintHelper, JandexHelper jandexHelper,
			AnnotationProcessingOptions annotationProcessingOptions, List<DotName> constraintAnnotations) {
		super( constraintHelper, jandexHelper, annotationProcessingOptions, constraintAnnotations );
	}

	public Stream<ConstrainedElement> getConstrainedFields(ClassInfo classInfo, Class<?> beanClass) {
		return classInfo.fields().stream()
				.map( fieldInfo -> toConstrainedField( beanClass, fieldInfo ) );
	}

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
				// TODO Jandex: we need to determine the TypeVariables
				Collections.emptyList(),
				commonInformation.getUnwrapMode()
		);
	}

	private Stream<MetaConstraint<?>> findMetaConstraints(Collection<AnnotationInstance> annotationInstances, Field field) {
		return findConstraints( annotationInstances, field )
				.map( descriptor -> createMetaConstraint( field, descriptor ) );
	}

	private Field findField(Class<?> beanClass, FieldInfo fieldInfo) {
		try {
			return beanClass.getDeclaredField( fieldInfo.name() );
		}
		catch (NoSuchFieldException e) {
			throw LOG.getUnableToFindFieldReferencedInJandexIndex( beanClass, fieldInfo.name().toString(), e );
		}
	}

	private <A extends Annotation> MetaConstraint<?> createMetaConstraint(Field member, ConstraintDescriptorImpl<A> descriptor) {
		return new MetaConstraint<>( descriptor, ConstraintLocation.forProperty( member ) );
	}
}
