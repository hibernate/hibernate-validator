/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.jandex;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.validation.groups.ConvertGroup;

import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.jandex.util.JandexHelper;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

/**
 * Abstract builder exploiting the Jandex index to build {@link ConstrainedElement}s.
 *
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public abstract class AbstractConstrainedElementJandexBuilder {

	protected static final Log LOG = LoggerFactory.make();

	private final ConstraintHelper constraintHelper;

	protected final JandexHelper jandexHelper;

	protected final AnnotationProcessingOptions annotationProcessingOptions;

	private final List<DotName> constraintAnnotations;

	protected AbstractConstrainedElementJandexBuilder(ConstraintHelper constraintHelper, JandexHelper jandexHelper,
			AnnotationProcessingOptions annotationProcessingOptions, List<DotName> constraintAnnotations) {
		this.jandexHelper = jandexHelper;
		this.constraintHelper = constraintHelper;
		this.annotationProcessingOptions = annotationProcessingOptions;
		this.constraintAnnotations = constraintAnnotations;
	}

	protected Stream<ConstraintDescriptorImpl<?>> findConstraints(Collection<AnnotationInstance> annotationInstances, Member member) {
		return findConstrainAnnotations( annotationInstances )
				.flatMap( annotationInstance -> findConstraintAnnotations( member, annotationInstance ) );
	}

	protected Stream<MetaConstraint<?>> findTypeAnnotationConstraintsForMember(MemberInformation information, boolean isCascaded) {
		//TODO: Do we need to include Type.Kind.WILDCARD_TYPE ?
		if ( !Type.Kind.PARAMETERIZED_TYPE.equals( information.getType().kind() ) ) {
			return Stream.empty();
		}

		List<Type> arguments = information.getType().asParameterizedType().arguments();
		Optional<Type> argument;
		if ( arguments.size() == 1 ) {
			argument = Optional.of( arguments.get( 0 ) );
		}
		else if ( jandexHelper.isMap( information.getType() ) ) {
			argument = Optional.of( arguments.get( 1 ) );
		}
		else {
			argument = Optional.empty();
		}
		if ( argument.isPresent() ) {
			// HV-925
			// We need to determine the validated type used for constraint validator resolution.
			// Iterables and maps need special treatment at this point, since the validated type is the type of the
			// specified type parameter. In the other cases the validated type is the parameterized type, eg Optional<String>.
			// In the latter case a value unwrapping has to occur
			Type validatedType = information.getType();
			if ( jandexHelper.isIterable( validatedType ) || jandexHelper.isMap( validatedType ) ) {
				validatedType = argument.get();
			}
			Class<?> memberType = jandexHelper.getClassForName( validatedType.name() );
			return findConstrainAnnotations( argument.get().annotations() )
					.flatMap( annotationInstance -> findConstraintAnnotations( information.getMember(), annotationInstance ) )
					.map( constraintDescriptor -> createTypeArgumentMetaConstraint( information.getConstraintLocation(), constraintDescriptor, memberType ) );
		}

		return Stream.empty();
	}

	protected CommonConstraintInformation findCommonConstraintInformation(Type type, Collection<AnnotationInstance> annotationInstances,
			boolean typeArgumentAnnotated, boolean isCascading) {
		return new CommonConstraintInformation(
				getGroupConversions(
						jandexHelper.findAnnotation( annotationInstances, ConvertGroup.class ),
						jandexHelper.findAnnotation( annotationInstances, ConvertGroup.List.class )
				),
				isCascading,
				determineUnwrapMode( type, annotationInstances, typeArgumentAnnotated )
		);
	}

	private UnwrapMode determineUnwrapMode(Type type, Collection<AnnotationInstance> annotationInstances, boolean typeArgumentAnnotated) {
		boolean indexable = jandexHelper.isIndexable( type );
		Optional<AnnotationInstance> unwrapValidatedValue = jandexHelper.findAnnotation( annotationInstances, UnwrapValidatedValue.class );

		if ( !unwrapValidatedValue.isPresent() && typeArgumentAnnotated && !indexable ) {
			return UnwrapMode.UNWRAP;
		}
		else if ( unwrapValidatedValue.isPresent() ) {
			return unwrapValidatedValue.get().value().asBoolean() ? UnwrapMode.UNWRAP : UnwrapMode.SKIP_UNWRAP;
		}
		return UnwrapMode.AUTOMATIC;
	}

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

	private void addToConversionGroup(Map<Class<?>, Class<?>> groupConversionMap, AnnotationInstance annotation) {
		Class<?> from = jandexHelper.getClassForName( annotation.value( "from" ).asClass().name() );
		groupConversionMap.merge(
				from,
				jandexHelper.getClassForName( annotation.value( "to" ).asClass().name() ),
				( val1, val2 ) -> {
					throw LOG.getMultipleGroupConversionsForSameSourceException(
							from,
							CollectionHelper.asSet( val1, val2 )
					);
				}
		);
	}

	private Stream<ConstraintDescriptorImpl<?>> findConstraintAnnotations(Member member, AnnotationInstance annotationInstance) {
		return instanceToAnnotations( annotationInstance ).map( constraint -> buildConstraintDescriptor( member, constraint, ElementType.FIELD ) );
	}

	@SuppressWarnings("unchecked")
	private <A extends Annotation> Stream<Annotation> instanceToAnnotations(AnnotationInstance annotationInstance) {
		Class<A> annotationClass = (Class<A>) jandexHelper.getClassForName( annotationInstance.name() );
		if ( constraintHelper.isMultiValueConstraint( annotationClass ) ) {
			return Arrays.stream( (AnnotationValue[]) annotationInstance.value().value() )
					.map( annotationValue -> instanceToAnnotation( annotationValue.asNested() ) );
		}
		else if ( constraintHelper.isConstraintAnnotation( annotationClass ) ) {
			return Stream.of( instanceToAnnotation( annotationClass, annotationInstance ) );
		}
		else {
			return Stream.empty();
		}
	}

	@SuppressWarnings("unchecked")
	private <A extends Annotation> A instanceToAnnotation(AnnotationInstance annotationInstance) {
		return instanceToAnnotation( (Class<A>) jandexHelper.getClassForName( annotationInstance.name() ), annotationInstance );
	}

	private <A extends Annotation> A instanceToAnnotation(Class<A> annotationClass, AnnotationInstance annotationInstance) {
		AnnotationDescriptor<A> annotationDescriptor = new AnnotationDescriptor<>( annotationClass );

		annotationInstance.values().stream()
				.forEach( annotationValue -> annotationDescriptor.setValue( annotationValue.name(), jandexHelper.convertAnnotationValue( annotationValue ) ) );

		A annotation;
		try {
			annotation = AnnotationFactory.create( annotationDescriptor );
		}
		catch (RuntimeException e) {
			throw LOG.getUnableToCreateAnnotationForConfiguredConstraintException( e );
		}
		return annotation;
	}

	private Stream<AnnotationInstance> findConstrainAnnotations(Collection<AnnotationInstance> allAnnotations) {
		return allAnnotations.stream().filter( this::isConstraintAnnotation );
	}

	private boolean isConstraintAnnotation(AnnotationInstance annotationInstance) {
		return constraintAnnotations.contains( annotationInstance.name() );
	}

	private <A extends Annotation> MetaConstraint<?> createTypeArgumentMetaConstraint(
			ConstraintLocation location,
			ConstraintDescriptorImpl<A> descriptor,
			java.lang.reflect.Type type
	) {
		// TODO Jandex: we need a new way to build the type argument constraint location
		return new MetaConstraint<>( descriptor, ConstraintLocation.forTypeArgument( location, null, type ) );
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

	/**
	 * Simple POJO that contains constraint information common to all elements.
	 */
	protected static class CommonConstraintInformation {

		private Map<Class<?>, Class<?>> groupConversions;
		private boolean isCascading;
		private UnwrapMode unwrapMode;

		public CommonConstraintInformation() {
			this.groupConversions = Collections.emptyMap();
			this.isCascading = false;
			this.unwrapMode = UnwrapMode.AUTOMATIC;
		}

		public CommonConstraintInformation(Map<Class<?>, Class<?>> groupConversions, boolean isCascading, UnwrapMode unwrapMode) {
			this.groupConversions = groupConversions;
			this.isCascading = isCascading;
			this.unwrapMode = unwrapMode;
		}

		public Map<Class<?>, Class<?>> getGroupConversions() {
			return groupConversions;
		}

		public boolean isCascading() {
			return isCascading;
		}

		public UnwrapMode getUnwrapMode() {
			return unwrapMode;
		}
	}

	/**
	 * Contains general information about {@link Member}.
	 */
	protected static class MemberInformation {

		private Type type;
		private String name;
		private Member member;
		private Class<?> beanClass;
		private ConstraintLocation constraintLocation;

		public MemberInformation(Type type, String name, Member member,ConstraintLocation constraintLocation, Class<?> beanClass) {
			this.type = type;
			this.name = name;
			this.member = member;
			this.beanClass = beanClass;
			this.constraintLocation = constraintLocation;
		}

		public Type getType() {
			return type;
		}

		public String getName() {
			return name;
		}

		public Member getMember() {
			return member;
		}

		public Class<?> getBeanClass() {
			return beanClass;
		}

		public ConstraintLocation getConstraintLocation() {
			return constraintLocation;
		}
	}

}
