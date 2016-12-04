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

import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;

import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.jandex.util.JandexHelper;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.parameternameprovider.ParanamerParameterNameProvider;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.Type;

/**
 * Base builder for constrained elements that uses Jandex index.
 *
 * @author Marko Bekhta
 */
public abstract class AbstractConstrainedElementJandexBuilder {

	protected static final Log log = LoggerFactory.make();

	protected final ConstraintHelper constraintHelper;

	protected final JandexHelper jandexHelper;

	protected final ExecutableParameterNameProvider parameterNameProvider;

	protected final AnnotationProcessingOptions annotationProcessingOptions;

	protected AbstractConstrainedElementJandexBuilder(ConstraintHelper constraintHelper, JandexHelper jandexHelper) {
		this.jandexHelper = jandexHelper;
		this.constraintHelper = constraintHelper;
		//TODO: init correctly this is just for testing
		this.parameterNameProvider = new ExecutableParameterNameProvider( new ParanamerParameterNameProvider() );
		this.annotationProcessingOptions = new AnnotationProcessingOptionsImpl();
	}

	/**
	 * Determine the unwrap mode.
	 *
	 * @param type a {@link Type} of an element
	 * @param annotationInstances a {@link Collection} of annotations present on that element
	 * @param typeArgumentAnnotated ?
	 *
	 * @return {@link UnwrapMode} for a given field
	 */
	protected UnwrapMode determineUnwrapMode(Type type, Collection<AnnotationInstance> annotationInstances, boolean typeArgumentAnnotated) {
		boolean indexable = jandexHelper.isIndexable( type );
		Optional<AnnotationInstance> unwrapValidatedValue = findAnnotation( annotationInstances, UnwrapValidatedValue.class );

		if ( !unwrapValidatedValue.isPresent() && typeArgumentAnnotated && !indexable ) {
			return UnwrapMode.UNWRAP;
		}
		else if ( unwrapValidatedValue.isPresent() ) {
			return unwrapValidatedValue.get().value().asBoolean() ? UnwrapMode.UNWRAP : UnwrapMode.SKIP_UNWRAP;
		}
		return UnwrapMode.AUTOMATIC;
	}

	/**
	 * Builds a map of group conversions based on given {@link ConvertGroup} and {@link ConvertGroup.List} parameters.
	 *
	 * @param convertGroup an optional for {@link ConvertGroup}
	 * @param convertGroupList an optional for {@link ConvertGroup.List}
	 *
	 * @return a {@link Map} containing group conversions
	 */
	protected Map<Class<?>, Class<?>> getGroupConversions(Optional<AnnotationInstance> convertGroup, Optional<AnnotationInstance> convertGroupList) {
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
		Class<?> from = jandexHelper.getClassForName( annotation.value( "from" ).asClass().name().toString() );
		groupConversionMap.merge(
				from,
				jandexHelper.getClassForName( annotation.value( "to" ).asClass().name().toString() ),
				( val1, val2 ) -> {
					throw log.getMultipleGroupConversionsForSameSourceException(
							from,
							CollectionHelper.asSet( val1, val2 )
					);
				}
		);
	}

	/**
	 * Finds and converts constraint annotations to {@link MetaConstraint}
	 *
	 * @param annotationInstances a collection of annotation instances
	 * @param member a {@link Member} under investigation
	 *
	 * @return a stream of {@link MetaConstraint}s based on input parameters
	 */
	protected Stream<MetaConstraint<?>> findConstraints(Collection<AnnotationInstance> annotationInstances, Member member) {
		return findConstrainAnnotations( annotationInstances )
				.flatMap( annotationInstance -> findConstraintAnnotations( member, annotationInstance ) )
				.map( descriptor -> createMetaConstraint( member, descriptor ) );
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
	protected <A extends Annotation> Stream<Annotation> instanceToAnnotations(AnnotationInstance annotationInstance) {
		Class<A> annotationClass = (Class<A>) jandexHelper.getClassForName( annotationInstance.name().toString() );
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

	/**
	 * Converts given annotation instance of ({@link AnnotationInstance}) type to its {@link Annotation} representation.
	 *
	 * @param annotationInstance annotation instance to convert
	 *
	 * @return an annotation based on input parameters
	 */
	protected <A extends Annotation> A instanceToAnnotation(AnnotationInstance annotationInstance) {
		return instanceToAnnotation( (Class<A>) jandexHelper.getClassForName( annotationInstance.name()
				.toString() ), annotationInstance );
	}

	/**
	 * Converts given annotation instance of ({@link AnnotationInstance}) type to its {@link Annotation} representation.
	 *
	 * @param annotationClass a class of annotation to convert to
	 * @param annotationInstance annotation instance to convert
	 *
	 * @return an annotation based on input parameters
	 */
	protected <A extends Annotation> A instanceToAnnotation(Class<A> annotationClass, AnnotationInstance annotationInstance) {
		AnnotationDescriptor<A> annotationDescriptor = new AnnotationDescriptor<>( annotationClass );

		annotationInstance.values().stream()
				.forEach( annotationValue -> annotationDescriptor.setValue( annotationValue.name(), convertAnnotationValue( annotationValue ) ) );

		A annotation;
		try {
			annotation = AnnotationFactory.create( annotationDescriptor );
		}
		catch (RuntimeException e) {
			throw log.getUnableToCreateAnnotationForConfiguredConstraintException( e );
		}
		return annotation;
	}

	/**
	 * Converts annotation value to a value usable for {@link Annotation}.
	 *
	 * @param annotationValue annotation value to convert
	 *
	 * @return converted value
	 */
	protected Object convertAnnotationValue(AnnotationValue annotationValue) {
		if ( AnnotationValue.Kind.ARRAY.equals( annotationValue.kind() ) ) {
			if ( AnnotationValue.Kind.CLASS.equals( annotationValue.componentKind() ) ) {
				return Arrays.stream( annotationValue.asClassArray() )
						.map( type -> jandexHelper.getClassForName( type.name().toString() ) )
						.toArray( size -> new Class[size] );
			}
		}
		else if ( AnnotationValue.Kind.CLASS.equals( annotationValue.kind() ) ) {
			return jandexHelper.getClassForName( annotationValue.asClass().name().toString() );
		}
		return annotationValue.value();
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
	protected Optional<AnnotationInstance> findAnnotation(Collection<AnnotationInstance> annotations, Class<?> aClass) {
		return annotations.stream()
				.filter( annotation -> annotation.name().toString().equals( aClass.getName() ) )
				.findAny();
	}

	/**
	 * Finds all constrain annotations in given collection.
	 *
	 * @param allAnnotations collection to look for constraints in
	 *
	 * @return a {@link Stream<AnnotationInstance>} representing constraint annotations
	 */
	protected Stream<AnnotationInstance> findConstrainAnnotations(Collection<AnnotationInstance> allAnnotations) {
		return allAnnotations.stream().filter( this::isConstraintAnnotation );
	}

	/**
	 * Checks if given annotation is a constraint or not.
	 *
	 * @param annotationInstance an annotation to check
	 *
	 * @return {@code true} if given annotation is a constraint, {@code false} otherwise
	 */
	protected boolean isConstraintAnnotation(AnnotationInstance annotationInstance) {
		// HV-1049 Ignore annotations from jdk.internal.*; They cannot be constraint annotations so skip them right
		// here, as for the proper check we'd need package access permission for "jdk.internal"
		if ( "jdk.internal".equals( annotationInstance.name().prefix().toString() ) ) {
			return false;
		}
		// TODO: need to find a way to determine if an annotation is a constraint or not

		// constraintAnnotationCache.computeIfAbsent( annotationInstance.name().toString(), ???)

		return true;
	}

	/**
	 * Provides a {@link Stream} of type constraints based on given member information.
	 *
	 * @param information information about member of interest
	 * @param isCascaded if member is marked with {@link Valid}
	 *
	 * @return a stream of {@link MetaConstraint}s
	 */
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
				if ( !isCascaded ) {
					throw log.getTypeAnnotationConstraintOnIterableRequiresUseOfValidAnnotationException(
							information.getBeanClass(),
							information.getName()
					);
				}
				validatedType = argument.get();
			}
			Class<?> memberType = jandexHelper.getClassForName( validatedType.name().toString() );
			return findConstrainAnnotations( argument.get().annotations() )
					.flatMap( annotationInstance -> findConstraintAnnotations( information.getMember(), annotationInstance ) )
					.map( constraintDescriptor -> createTypeArgumentMetaConstraint( information.getMember(), constraintDescriptor, memberType ) );
		}

		return Stream.empty();
	}

	/**
	 * Collects common constraint information for given parameters.
	 *
	 * @param type a type of an element under investigation
	 * @param annotationInstances a collection of {@link AnnotationInstance}s on an element under investigation
	 * @param typeArgumentAnnotated is type argument annotated
	 * @param isCascading is an element marked with {@link Valid} ?
	 *
	 * @return a {@link CommonConstraintInformation} instance containing common constraint information
	 */
	protected CommonConstraintInformation findCommonConstraintInformation(Type type, Collection<AnnotationInstance> annotationInstances,
			boolean typeArgumentAnnotated, boolean isCascading) {
		return new CommonConstraintInformation(
				getGroupConversions(
						findAnnotation( annotationInstances, ConvertGroup.class ),
						findAnnotation( annotationInstances, ConvertGroup.List.class )
				),
				findAnnotation( annotationInstances, Valid.class ).isPresent(),
				determineUnwrapMode( type, annotationInstances, typeArgumentAnnotated )
		);
	}

	protected <A extends Annotation> MetaConstraint<?> createMetaConstraint(Member member, ConstraintDescriptorImpl<A> descriptor) {
		return new MetaConstraint<>( descriptor, ConstraintLocation.forProperty( member ) );
	}

	/**
	 * Creates a {@code MetaConstraint} for a type argument constraint.
	 */
	protected <A extends Annotation> MetaConstraint<?> createTypeArgumentMetaConstraint(
			Member member, ConstraintDescriptorImpl<A> descriptor,
			java.lang.reflect.Type type) {
		return new MetaConstraint<>( descriptor, ConstraintLocation.forTypeArgument( member, type ) );
	}

	protected <A extends Annotation> ConstraintDescriptorImpl<A> buildConstraintDescriptor(
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

		public MemberInformation(Type type, String name, Member member, Class<?> beanClass) {
			this.type = type;
			this.name = name;
			this.member = member;
			this.beanClass = beanClass;
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
	}

}
