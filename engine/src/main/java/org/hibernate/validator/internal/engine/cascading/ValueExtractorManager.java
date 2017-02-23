/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import java.lang.reflect.TypeVariable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.ValidationException;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.TypeVariableBindings;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class ValueExtractorManager {

	@Immutable
	public static final Set<ValueExtractor<?>> SPEC_DEFINED_EXTRACTORS = Stream.of(
			ByteArrayValueExtractor.DESCRIPTOR,
			ShortArrayValueExtractor.DESCRIPTOR,
			IntArrayValueExtractor.DESCRIPTOR,
			LongArrayValueExtractor.DESCRIPTOR,
			FloatArrayValueExtractor.DESCRIPTOR,
			DoubleArrayValueExtractor.DESCRIPTOR,
			CharArrayValueExtractor.DESCRIPTOR,
			BooleanArrayValueExtractor.DESCRIPTOR,
			ObjectArrayValueExtractor.DESCRIPTOR,

			LegacyListValueExtractor.DESCRIPTOR,
			ListValueExtractor.DESCRIPTOR,

			LegacyMapValueExtractor.DESCRIPTOR,
			MapValueExtractor.DESCRIPTOR,
			MapKeyExtractor.DESCRIPTOR,

			LegacyIterableValueExtractor.DESCRIPTOR,
			IterableValueExtractor.DESCRIPTOR,

			OptionalValueExtractor.DESCRIPTOR,
			ObjectValueExtractor.DESCRIPTOR
		)
		.map( ValueExtractorDescriptor::getValueExtractor )
		.collect( Collectors.collectingAndThen( Collectors.toSet(), Collections::unmodifiableSet ) );

	@Immutable
	private final Map<ValueExtractorDescriptor.Key, ValueExtractorDescriptor> valueExtractors;

	public ValueExtractorManager(Set<ValueExtractor<?>> externalExtractors) {
		LinkedHashMap<ValueExtractorDescriptor.Key, ValueExtractorDescriptor> tmpValueExtractors = new LinkedHashMap<>();

		for ( ValueExtractor<?> valueExtractor : externalExtractors ) {
			ValueExtractorDescriptor descriptor = new ValueExtractorDescriptor( valueExtractor );
			tmpValueExtractors.put( descriptor.getKey(), descriptor );
		}

		if ( isJavaFxInClasspath() ) {
			tmpValueExtractors.put( ObservableValueValueExtractor.DESCRIPTOR.getKey(), ObservableValueValueExtractor.DESCRIPTOR );
		}

		tmpValueExtractors.put( LegacyListValueExtractor.DESCRIPTOR.getKey(), LegacyListValueExtractor.DESCRIPTOR );
		tmpValueExtractors.put( ListValueExtractor.DESCRIPTOR.getKey(), ListValueExtractor.DESCRIPTOR );

		tmpValueExtractors.put( ByteArrayValueExtractor.DESCRIPTOR.getKey(), ByteArrayValueExtractor.DESCRIPTOR );
		tmpValueExtractors.put( ShortArrayValueExtractor.DESCRIPTOR.getKey(), ShortArrayValueExtractor.DESCRIPTOR );
		tmpValueExtractors.put( IntArrayValueExtractor.DESCRIPTOR.getKey(), IntArrayValueExtractor.DESCRIPTOR );
		tmpValueExtractors.put( LongArrayValueExtractor.DESCRIPTOR.getKey(), LongArrayValueExtractor.DESCRIPTOR );
		tmpValueExtractors.put( FloatArrayValueExtractor.DESCRIPTOR.getKey(), FloatArrayValueExtractor.DESCRIPTOR );
		tmpValueExtractors.put( DoubleArrayValueExtractor.DESCRIPTOR.getKey(), DoubleArrayValueExtractor.DESCRIPTOR );
		tmpValueExtractors.put( CharArrayValueExtractor.DESCRIPTOR.getKey(), CharArrayValueExtractor.DESCRIPTOR );
		tmpValueExtractors.put( BooleanArrayValueExtractor.DESCRIPTOR.getKey(), BooleanArrayValueExtractor.DESCRIPTOR );
		tmpValueExtractors.put( ObjectArrayValueExtractor.DESCRIPTOR.getKey(), ObjectArrayValueExtractor.DESCRIPTOR );

		tmpValueExtractors.put( LegacyMapValueExtractor.DESCRIPTOR.getKey(), LegacyMapValueExtractor.DESCRIPTOR );
		tmpValueExtractors.put( MapValueExtractor.DESCRIPTOR.getKey(), MapValueExtractor.DESCRIPTOR );
		tmpValueExtractors.put( MapKeyExtractor.DESCRIPTOR.getKey(), MapKeyExtractor.DESCRIPTOR );

		tmpValueExtractors.put( LegacyIterableValueExtractor.DESCRIPTOR.getKey(), LegacyIterableValueExtractor.DESCRIPTOR );
		tmpValueExtractors.put( IterableValueExtractor.DESCRIPTOR.getKey(), IterableValueExtractor.DESCRIPTOR );

		tmpValueExtractors.put( LegacyOptionalValueExtractor.DESCRIPTOR.getKey(), LegacyOptionalValueExtractor.DESCRIPTOR );
		tmpValueExtractors.put( OptionalValueExtractor.DESCRIPTOR.getKey(), OptionalValueExtractor.DESCRIPTOR );
		tmpValueExtractors.put( ObjectValueExtractor.DESCRIPTOR.getKey(), ObjectValueExtractor.DESCRIPTOR );

		valueExtractors = Collections.unmodifiableMap( tmpValueExtractors );
	}

	public ValueExtractorManager(ValueExtractorManager template, Set<ValueExtractor<?>> externalExtractors) {
		LinkedHashMap<ValueExtractorDescriptor.Key, ValueExtractorDescriptor> tmpValueExtractors = new LinkedHashMap<>( template.valueExtractors );

		for ( ValueExtractor<?> valueExtractor : externalExtractors ) {
			ValueExtractorDescriptor descriptor = new ValueExtractorDescriptor( valueExtractor );
			tmpValueExtractors.put( descriptor.getKey(), descriptor );
		}

		valueExtractors = Collections.unmodifiableMap( tmpValueExtractors );
	}

	public static Set<ValueExtractor<?>> getDefaultValueExtractors() {
		return SPEC_DEFINED_EXTRACTORS;
	}

	/**
	 * Returns the most specific value extractor extracting the given type or {@code null} if none was found.
	 */
	public ValueExtractorDescriptor getValueExtractor(Class<?> valueType) {
		List<ValueExtractorDescriptor> typeCompatibleExtractors = valueExtractors.values()
				.stream()
				.filter( e -> TypeHelper.isAssignable( TypeHelper.getErasedReferenceType( e.getExtractedType() ), valueType ) )
				.collect( Collectors.toList() );

		// TODO
		// * keep most specific one
		// * if several extractors are found for the most specific type, e.g. key and value extractors for Map, raise an exception
		if ( typeCompatibleExtractors.isEmpty() ) {
			return null;
		}
		else {
			return typeCompatibleExtractors.iterator().next();
		}
	}

	public ValueExtractorDescriptor getValueExtractor(Class<?> valueType, TypeVariable<?> typeParameter) {
		Map<Class<?>, Map<TypeVariable<?>, TypeVariable<?>>> allBindings = null;

		if ( typeParameter != AnnotatedObject.INSTANCE && typeParameter != ArrayElement.INSTANCE ) {
			allBindings = TypeVariableBindings.getTypeVariableBindings( (Class<?>) typeParameter.getGenericDeclaration() );
		}

		List<ValueExtractorDescriptor> typeCompatibleExtractors = valueExtractors.values()
				.stream()
				.filter( e -> TypeHelper.isAssignable( TypeHelper.getErasedReferenceType( e.getExtractedType() ), valueType ) )
				.collect( Collectors.toList() );

		for ( ValueExtractorDescriptor extractorDescriptor : typeCompatibleExtractors ) {
			TypeVariable<?> typeParameterBoundToExtractorType;
			if ( typeParameter != AnnotatedObject.INSTANCE && typeParameter != ArrayElement.INSTANCE ) {
				Map<TypeVariable<?>, TypeVariable<?>> bindingsForExtractorType = allBindings.get( TypeHelper.getErasedReferenceType( extractorDescriptor.getExtractedType() ) );
				typeParameterBoundToExtractorType = bind( typeParameter, bindingsForExtractorType );
			}
			else {
				typeParameterBoundToExtractorType = typeParameter;
			}

			if ( typeParameterBoundToExtractorType.equals( extractorDescriptor.getExtractedTypeParameter() ) ) {
				// TODO implement selection of most specific extractor per requested type parameter
				return extractorDescriptor;
			}
		}

		// TODO should only happen during transition off value unwrappers
		return null;
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

	private TypeVariable<?> bind(TypeVariable<?> typeParameter, Map<TypeVariable<?>, TypeVariable<?>> bindings) {
		TypeVariable<?> bound = null;

		if ( bindings != null ) {
			bound = bindings.get( typeParameter );
		}

		return bound != null ? bound : typeParameter == AnnotatedObject.INSTANCE ? AnnotatedObject.INSTANCE : ArrayElement.INSTANCE;
	}

	private boolean isJavaFxInClasspath() {
		return isClassPresent( "javafx.application.Application", false );
	}

	private boolean isClassPresent(String className, boolean fallbackOnTCCL) {
		try {
			run( LoadClass.action( className, getClass().getClassLoader(), fallbackOnTCCL ) );
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
	private <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
