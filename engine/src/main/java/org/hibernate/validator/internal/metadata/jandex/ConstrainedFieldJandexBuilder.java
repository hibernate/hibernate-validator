/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.jandex;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;

import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.jandex.util.JandexUtils;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;

/**
 * Builder for constrained fields that uses Jandex index.
 *
 * @author Marko Bekhta
 */
public class ConstrainedFieldJandexBuilder {

	private static final Log log = LoggerFactory.make();

	private Map<String, Boolean> constraintAnnotationCache = CollectionHelper.newConcurrentHashMap();

	protected final ConstraintHelper constraintHelper;

	private ConstrainedFieldJandexBuilder() {
		//TODO: need to pass constraintHelper as a parameter and init the variable correctly
		constraintHelper = null;
	}

	/**
	 * Creates an instance of a {@link ConstrainedFieldJandexBuilder}.
	 *
	 * @return a new instance of {@link ConstrainedFieldJandexBuilder}
	 */
	public static ConstrainedFieldJandexBuilder getInstance() {
		return new ConstrainedFieldJandexBuilder();
	}

	/**
	 * Gets {@link ConstrainedField}s from a given class.
	 *
	 * @param classInfo a class in which to look for constrained fileds
	 * @param beanClass same class as {@code classInfo} but represented as {@link Class}
	 *
	 * @return a collection of {@link ConstrainedElement}s that represents fields
	 */
	public Collection<ConstrainedElement> getConstrainedFields(ClassInfo classInfo, Class<?> beanClass) {
		return classInfo.fields().stream()
				.map( fieldInfo -> toConstrainedField( beanClass, fieldInfo ) )
				.collect( Collectors.toSet() );
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

		Set<MetaConstraint<?>> constraints = convertToMetaConstraints( beanClass, fieldInfo );

		Map<Class<?>, Class<?>> groupConversions = getGroupConversions(
				findAnnotation( fieldInfo.annotations(), ConvertGroup.class ),
				findAnnotation( fieldInfo.annotations(), ConvertGroup.List.class )
		);

		boolean isCascading = findAnnotation( fieldInfo.annotations(), Valid.class ).isPresent();
		Set<MetaConstraint<?>> typeArgumentsConstraints = findTypeAnnotationConstraintsForMember( fieldInfo );

		boolean typeArgumentAnnotated = !typeArgumentsConstraints.isEmpty();
		UnwrapMode unwrapMode = determineUnwrapMode( fieldInfo, typeArgumentAnnotated );

		return new ConstrainedField(
				ConfigurationSource.JANDEX,
				findField( beanClass, fieldInfo ),
				constraints,
				typeArgumentsConstraints,
				groupConversions,
				isCascading,
				unwrapMode
		);
	}

	/**
	 * Determine the unwrap mode.
	 *
	 * @param fieldInfo a field to check
	 * @param typeArgumentAnnotated ?
	 *
	 * @return {@link UnwrapMode} for a given field
	 */
	private UnwrapMode determineUnwrapMode(FieldInfo fieldInfo, boolean typeArgumentAnnotated) {
		boolean indexable = JandexUtils.isIndexable( fieldInfo.type() );
		Optional<AnnotationInstance> unwrapValidatedValue = findAnnotation( fieldInfo.annotations(), UnwrapValidatedValue.class );

		if ( !unwrapValidatedValue.isPresent() && typeArgumentAnnotated && !indexable ) {
			return UnwrapMode.UNWRAP;
		}
		else if ( unwrapValidatedValue.isPresent() ) {
			return unwrapValidatedValue.get().value().asBoolean() ? UnwrapMode.UNWRAP : UnwrapMode.SKIP_UNWRAP;
		}
		return UnwrapMode.AUTOMATIC;
	}

	private Set<MetaConstraint<?>> findTypeAnnotationConstraintsForMember(FieldInfo fieldInfo) {
		return Collections.emptySet();
	}

	/**
	 * Builds a map of group conversions based on given {@link ConvertGroup} and {@link ConvertGroup.List} parameters.
	 *
	 * @param convertGroup an optional for {@link ConvertGroup}
	 * @param convertGroupList an optional for {@link ConvertGroup.List}
	 *
	 * @return a {@link Map} containing group conversions
	 */
	private Map<Class<?>, Class<?>> getGroupConversions(Optional<AnnotationInstance> convertGroup, Optional<AnnotationInstance> convertGroupList) {
		Map<Class<?>, Class<?>> groupConversionMap = CollectionHelper.newHashMap();

		convertGroup.ifPresent( annotation -> addToConversionGroup( groupConversionMap, annotation ) );

		convertGroupList.ifPresent( nestedAnnotations -> {
			for ( AnnotationValue annotationValue : (AnnotationValue[]) nestedAnnotations.value().value() ) {
				addToConversionGroup( groupConversionMap, annotationValue.asNested() );
			}
		} );

		return groupConversionMap;
	}

	/**
	 * Adds a conversion group ot a given map from an annotation that represents {@link ConvertGroup}.
	 *
	 * @param groupConversionMap a group conversion map to which to add new value
	 * @param annotation an annotation {@link AnnotationInstance} that represents {@link ConvertGroup}
	 */
	private void addToConversionGroup(Map<Class<?>, Class<?>> groupConversionMap, AnnotationInstance annotation) {
		Class<?> from = JandexUtils.getClassForName( annotation.value( "from" ).asClass().name().toString() );
		groupConversionMap.merge(
				from,
				JandexUtils.getClassForName( annotation.value( "to" ).asClass().name().toString() ),
				( val1, val2 ) -> {
					throw log.getMultipleGroupConversionsForSameSourceException(
							from,
							CollectionHelper.asSet( val1, val2 )
					);
				}
		);
	}

	/**
	 * Converts a stream of constraint annotations to a set of {@link MetaConstraint}s.
	 *
	 * @param beanClass a {@link Class} in which field is located
	 * @param fieldInfo a field on which constraints are defined
	 *
	 * @return a set of {@link MetaConstraint}s based on provided parameters
	 */
	private Set<MetaConstraint<?>> convertToMetaConstraints(Class<?> beanClass, FieldInfo fieldInfo) {
		Field field = findField( beanClass, fieldInfo );
		return findConstraints( fieldInfo, field ).map(
				descriptor -> createMetaConstraint( field, descriptor ) )
				.collect( Collectors.<MetaConstraint<?>>toSet() );
	}

	/**
	 * Finds all constraint annotations defined for the given field and returns them as a stream of
	 * constraint descriptors.
	 *
	 * @param fieldInfo a {@link FieldInfo} representation of a given field.
	 * @param field a {@link Field} representation of a given field.
	 *
	 * @return A stream of constraint descriptors for all constraint specified for the given member.
	 */
	private Stream<ConstraintDescriptorImpl<?>> findConstraints(FieldInfo fieldInfo, Field field) {
		return findConstrainAnnotations( fieldInfo.annotations() )
				.flatMap( annotationInstance -> findConstraintAnnotations( field, annotationInstance ) );
	}

	/**
	 * Examines the given annotation to see whether it is a single- or multi-valued constraint annotation.
	 *
	 * @param member The member to check for constraints annotations
	 * @param annotationInstance The annotation to examine
	 *
	 * @return A stream of constraint descriptors or the empty stream in case given {@code annotationInstance} is neither a
	 * single nor multi-valued annotation.
	 */
	protected Stream<ConstraintDescriptorImpl<?>> findConstraintAnnotations(Member member, AnnotationInstance annotationInstance) {

		//TODO: probably need to do something similar to what is done in MetaConstraintBuilder#buildMetaConstraint to get the "Annotation"s

//		List<Annotation> constraints = newArrayList();
//		Class<? extends Annotation> annotationType = annotation.annotationType();
//		if ( constraintHelper.isConstraintAnnotation( annotationType ) ) {
//			constraints.add( annotation );
//		}
//		else if ( constraintHelper.isMultiValueConstraint( annotationType ) ) {
//			constraints.addAll( constraintHelper.getConstraintsFromMultiValueConstraint( annotation ) );
//		}
//
//		for ( Annotation constraint : constraints ) {
//			final ConstraintDescriptorImpl<?> constraintDescriptor = buildConstraintDescriptor(
//					member, constraint, type
//			);
//			constraintDescriptors.add( constraintDescriptor );
//		}

		return instanceToAnnotations( annotationInstance ).map( constraint -> buildConstraintDescriptor( member, constraint, ElementType.FIELD ) );
	}

	/**
	 * Converts a given annotation instance of ({@link AnnotationInstance}) to a stream of {@link Annotation}s
	 *
	 * @param annotationInstance instance to convert
	 *
	 * @return a stream that contains {@link Annotation}s based on given instance. If given instance is a simple constraint annotation
	 * it will contain it. If given instance is a multivalued constraint - it will be unwrapped and stream will contain annotations
	 * from that multivalued one.
	 */
	private Stream<Annotation> instanceToAnnotations(AnnotationInstance annotationInstance) {

//		if ( constraintHelper.isConstraintAnnotation( annotationType ) ) {
//			constraints.add( annotation );
//		}
//		else if ( constraintHelper.isMultiValueConstraint( annotationType ) ) {
//			constraints.addAll( constraintHelper.getConstraintsFromMultiValueConstraint( annotation ) );
//		}

		return Stream.empty();
	}

	/**
	 * Finds an annotation of a given type inside provided collection.
	 *
	 * @param annotations a collection of annotation in which to look for a provided annotation type
	 * @param aClass a type of annotation to look for.
	 *
	 * @return an {@link Optional<AnnotationInstance>} which will contain a found annotation, an empty {@link Optional}
	 * if none was found. Also if there are more than one annotation of provided type present in the collection there's
	 * no guarantee which one will be returned.
	 */
	private Optional<AnnotationInstance> findAnnotation(Collection<AnnotationInstance> annotations, Class<?> aClass) {
		return annotations.stream().filter( annotation -> annotation.name().toString().equals( aClass.getName() ) ).findAny();
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

	/**
	 * Finds all constrain annotations in given collection.
	 *
	 * @param allAnnotations collection to look for constraints in
	 *
	 * @return a {@link Stream<AnnotationInstance>} representing constraint annotations
	 */
	private Stream<AnnotationInstance> findConstrainAnnotations(Collection<AnnotationInstance> allAnnotations) {
		return allAnnotations.stream().filter( this::isConstraintAnnotation );
	}

	/**
	 * Checks if given annotation is a constraint or not.
	 *
	 * @param annotationInstance an annotation to check
	 *
	 * @return {@code true} if given annotation is a constraint, {@code false} otherwise
	 */
	private boolean isConstraintAnnotation(AnnotationInstance annotationInstance) {
		// HV-1049 Ignore annotations from jdk.internal.*; They cannot be constraint annotations so skip them right
		// here, as for the proper check we'd need package access permission for "jdk.internal"
		if ( "jdk.internal".equals( annotationInstance.name().prefix().toString() ) ) {
			return false;
		}
		// TODO: need to find a way to determine if an annotation is a constraint or not

		// constraintAnnotationCache.computeIfAbsent( annotationInstance.name().toString(), ???)

		return true;
	}


	private <A extends Annotation> MetaConstraint<?> createMetaConstraint(Member member, ConstraintDescriptorImpl<A> descriptor) {
		return new MetaConstraint<>( descriptor, ConstraintLocation.forProperty( member ) );
	}

	private <A extends Annotation> ConstraintDescriptorImpl<A> buildConstraintDescriptor(
			Member member,
			A annotation,
			ElementType type) {
		return new ConstraintDescriptorImpl<>(
				constraintHelper,
				member,
				annotation,
				type
		);
	}

}
