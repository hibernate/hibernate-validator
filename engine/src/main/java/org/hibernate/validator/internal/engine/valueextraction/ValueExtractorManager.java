/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintDeclarationException;
import javax.validation.ValidationException;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * @author Gunnar Morling
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public class ValueExtractorManager {

	@Immutable
	public static final Set<ValueExtractorDescriptor> SPEC_DEFINED_EXTRACTORS;

	static {
		LinkedHashSet<ValueExtractorDescriptor> specDefinedExtractors = new LinkedHashSet<>();

		if ( isJavaFxInClasspath() ) {
			specDefinedExtractors.add( ObservableValueValueExtractor.DESCRIPTOR );
			specDefinedExtractors.add( ListPropertyValueExtractor.DESCRIPTOR );
			specDefinedExtractors.add( ReadOnlyListPropertyValueExtractor.DESCRIPTOR );
			specDefinedExtractors.add( MapPropertyValueExtractor.DESCRIPTOR );
			specDefinedExtractors.add( ReadOnlyMapPropertyValueExtractor.DESCRIPTOR );
			specDefinedExtractors.add( MapPropertyKeyExtractor.DESCRIPTOR );
			specDefinedExtractors.add( ReadOnlyMapPropertyKeyExtractor.DESCRIPTOR );
			specDefinedExtractors.add( SetPropertyValueExtractor.DESCRIPTOR );
			specDefinedExtractors.add( ReadOnlySetPropertyValueExtractor.DESCRIPTOR );
		}

		specDefinedExtractors.add( ByteArrayValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( ShortArrayValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( IntArrayValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( LongArrayValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( FloatArrayValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( DoubleArrayValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( CharArrayValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( BooleanArrayValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( ObjectArrayValueExtractor.DESCRIPTOR );

		specDefinedExtractors.add( ListValueExtractor.DESCRIPTOR );

		specDefinedExtractors.add( MapValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( MapKeyExtractor.DESCRIPTOR );

		specDefinedExtractors.add( IterableValueExtractor.DESCRIPTOR );

		specDefinedExtractors.add( OptionalValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( OptionalIntValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( OptionalDoubleValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( OptionalLongValueExtractor.DESCRIPTOR );

		SPEC_DEFINED_EXTRACTORS = Collections.unmodifiableSet( specDefinedExtractors );
	}

	@Immutable
	private final Map<ValueExtractorDescriptor.Key, ValueExtractorDescriptor> valueExtractors;

	private final ValueExtractorResolver valueExtractorResolver;

	public ValueExtractorManager(Set<ValueExtractor<?>> externalExtractors) {
		LinkedHashMap<ValueExtractorDescriptor.Key, ValueExtractorDescriptor> tmpValueExtractors = new LinkedHashMap<>();

		// first all built-in extractors
		for ( ValueExtractorDescriptor descriptor : SPEC_DEFINED_EXTRACTORS ) {
			tmpValueExtractors.put( descriptor.getKey(), descriptor );
		}

		// then any custom ones, overriding the built-in ones
		for ( ValueExtractor<?> valueExtractor : externalExtractors ) {
			ValueExtractorDescriptor descriptor = new ValueExtractorDescriptor( valueExtractor );
			tmpValueExtractors.put( descriptor.getKey(), descriptor );
		}

		valueExtractors = Collections.unmodifiableMap( tmpValueExtractors );
		valueExtractorResolver = new ValueExtractorResolver( new HashSet<>( valueExtractors.values() ) );
	}

	public ValueExtractorManager(ValueExtractorManager template,
			Map<ValueExtractorDescriptor.Key, ValueExtractorDescriptor> externalValueExtractorDescriptors) {
		LinkedHashMap<ValueExtractorDescriptor.Key, ValueExtractorDescriptor> tmpValueExtractors = new LinkedHashMap<>( template.valueExtractors );
		tmpValueExtractors.putAll( externalValueExtractorDescriptors );

		valueExtractors = Collections.unmodifiableMap( tmpValueExtractors );
		valueExtractorResolver = new ValueExtractorResolver( new HashSet<>( valueExtractors.values() ) );
	}

	public static Set<ValueExtractor<?>> getDefaultValueExtractors() {
		return SPEC_DEFINED_EXTRACTORS.stream()
				.map( d -> d.getValueExtractor() )
				.collect( Collectors.collectingAndThen( Collectors.toSet(), Collections::unmodifiableSet ) );
	}

	/**
	 * Used to find all the maximally specific value extractors based on a declared type in the case of value unwrapping.
	 * <p>
	 * There might be several of them as there might be several type parameters.
	 * <p>
	 * Used for container element constraints.
	 *
	 * @see ValueExtractorResolver#getMaximallySpecificValueExtractors(Class)
	 */
	public Set<ValueExtractorDescriptor> getMaximallySpecificValueExtractors(Class<?> declaredType) {
		return valueExtractorResolver.getMaximallySpecificValueExtractors( declaredType );
	}

	/**
	 * Used to find the maximally specific and container element compliant value extractor based on the declared type
	 * and the type parameter.
	 * <p>
	 * Used for container element constraints.
	 *
	 * @see ValueExtractorResolver#getMaximallySpecificAndContainerElementCompliantValueExtractor(Class, TypeVariable)
	 * @throws ConstraintDeclarationException if more than 2 maximally specific container-element-compliant value extractors are found
	 */
	public ValueExtractorDescriptor getMaximallySpecificAndContainerElementCompliantValueExtractor(Class<?> declaredType, TypeVariable<?> typeParameter) {
		return valueExtractorResolver.getMaximallySpecificAndContainerElementCompliantValueExtractor( declaredType, typeParameter );
	}

	/**
	 * Used to find the maximally specific and container element compliant value extractor based on the runtime type.
	 * <p>
	 * The maximally specific one is chosen among the candidates passed to this method.
	 * <p>
	 * Used for cascading validation.
	 *
	 * @see ValueExtractorResolver#getMaximallySpecificAndRuntimeContainerElementCompliantValueExtractor(Type,
	 * TypeVariable, Class, Collection)
	 * @throws ConstraintDeclarationException if more than 2 maximally specific container-element-compliant value extractors are found
	 */
	public ValueExtractorDescriptor getMaximallySpecificAndRuntimeContainerElementCompliantValueExtractor(Type declaredType, TypeVariable<?> typeParameter,
			Class<?> runtimeType, Collection<ValueExtractorDescriptor> valueExtractorCandidates) {

		if ( valueExtractorCandidates.size() == 1 ) {
			return valueExtractorCandidates.iterator().next();
		}
		else if ( !valueExtractorCandidates.isEmpty() ) {
			return valueExtractorResolver.getMaximallySpecificAndRuntimeContainerElementCompliantValueExtractor(
					declaredType,
					typeParameter,
					runtimeType,
					valueExtractorCandidates
			);
		}
		else {
			return valueExtractorResolver.getMaximallySpecificAndRuntimeContainerElementCompliantValueExtractor(
					declaredType,
					typeParameter,
					runtimeType,
					valueExtractors.values()
			);
		}
	}

	/**
	 * Used to determine if the passed runtime type is a container and if so return a corresponding maximally specific
	 * value extractor.
	 * <p>
	 * Obviously, it only works if there's only one value extractor corresponding to the runtime type as we don't
	 * precise any type parameter.
	 * <p>
	 * There is a special case: when the passed type is assignable to a {@link Map}, the {@link MapValueExtractor} will
	 * be returned. This is required by the Bean Validation specification.
	 * <p>
	 * Used for cascading validation when the {@code @Valid} annotation is placed on the whole container.
	 *
	 * @see ValueExtractorResolver#getMaximallySpecificValueExtractorForAllContainerElements(Class)
	 */
	public ValueExtractorDescriptor getMaximallySpecificValueExtractorForAllContainerElements(Class<?> runtimeType) {
		return valueExtractorResolver.getMaximallySpecificValueExtractorForAllContainerElements( runtimeType );
	}

	/**
	 * Used to determine the value extractor candidates valid for a declared type and type variable.
	 * <p>
	 * The effective value extractor will be narrowed from these candidates using the runtime type.
	 * <p>
	 * Used to optimize the choice of the value extractor in the case of cascading validation.
	 *
	 * @see ValueExtractorResolver#getValueExtractorCandidatesForCascadedValidation(Type, TypeVariable)
	 */
	public Set<ValueExtractorDescriptor> getValueExtractorCandidatesForCascadedValidation(Type enclosingType, TypeVariable<?> typeParameter) {
		return valueExtractorResolver.getValueExtractorCandidatesForCascadedValidation( enclosingType, typeParameter );
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( valueExtractors == null ) ? 0 : valueExtractors.hashCode() );
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		ValueExtractorManager other = (ValueExtractorManager) obj;

		return valueExtractors.equals( other.valueExtractors );
	}

	private static boolean isJavaFxInClasspath() {
		return isClassPresent( "javafx.application.Application", false );
	}

	private static boolean isClassPresent(String className, boolean fallbackOnTCCL) {
		try {
			run( LoadClass.action( className, ValueExtractorManager.class.getClassLoader(), fallbackOnTCCL ) );
			return true;
		}
		catch (ValidationException e) {
			return false;
		}
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
