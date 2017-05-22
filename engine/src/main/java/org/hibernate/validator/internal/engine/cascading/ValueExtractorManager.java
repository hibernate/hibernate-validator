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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ValidationException;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.TypeVariableBindings;
import org.hibernate.validator.internal.util.TypeVariables;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class ValueExtractorManager {

	private static final Log LOG = LoggerFactory.make();

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

		specDefinedExtractors.add( ObjectValueExtractor.DESCRIPTOR );

		SPEC_DEFINED_EXTRACTORS = Collections.unmodifiableSet( specDefinedExtractors );
	}

	@Immutable
	private final Map<ValueExtractorDescriptor.Key, ValueExtractorDescriptor> valueExtractors;

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
	}

	public ValueExtractorManager(ValueExtractorManager template, Map<ValueExtractorDescriptor.Key, ValueExtractorDescriptor> externalValueExtractorDescriptors) {
		LinkedHashMap<ValueExtractorDescriptor.Key, ValueExtractorDescriptor> tmpValueExtractors = new LinkedHashMap<>( template.valueExtractors );
		tmpValueExtractors.putAll( externalValueExtractorDescriptors );

		valueExtractors = Collections.unmodifiableMap( tmpValueExtractors );
	}

	public static Set<ValueExtractor<?>> getDefaultValueExtractors() {
		return SPEC_DEFINED_EXTRACTORS.stream()
				.map( d -> d.getValueExtractor() )
				.collect( Collectors.collectingAndThen( Collectors.toSet(), Collections::unmodifiableSet ) );
	}

	/**
	 * Returns the most specific value extractor extracting the given type or {@code null} if none was found.
	 */
	public ValueExtractorDescriptor getValueExtractor(Class<?> valueType) {
		List<ValueExtractorDescriptor> typeCompatibleExtractors = valueExtractors.values()
				.stream()
				.filter( e -> TypeHelper.isAssignable( TypeHelper.getErasedReferenceType( e.getContainerType() ), valueType ) )
				.collect( Collectors.toList() );

		return getMostSpecific( valueType, typeCompatibleExtractors );
	}

	public ValueExtractorDescriptor getValueExtractor(Class<?> valueType, TypeVariable<?> typeParameter) {
		boolean isInternal = TypeVariables.isInternal( typeParameter );
		Map<Class<?>, Map<TypeVariable<?>, TypeVariable<?>>> allBindings = null;

		if ( !isInternal ) {
			allBindings = TypeVariableBindings.getTypeVariableBindings( (Class<?>) typeParameter.getGenericDeclaration() );
		}

		List<ValueExtractorDescriptor> typeCompatibleExtractors = valueExtractors.values()
				.stream()
				.filter( e -> TypeHelper.isAssignable( e.getContainerType(), valueType ) )
				.collect( Collectors.toList() );

		List<ValueExtractorDescriptor> typeParameterCompatibleExtractors = new ArrayList<>();

		for ( ValueExtractorDescriptor extractorDescriptor : typeCompatibleExtractors ) {
			TypeVariable<?> typeParameterBoundToExtractorType;

			if ( !isInternal ) {
				Map<TypeVariable<?>, TypeVariable<?>> bindingsForExtractorType = allBindings.get( extractorDescriptor.getContainerType() );
				typeParameterBoundToExtractorType = bind( typeParameter, bindingsForExtractorType );
			}
			else {
				typeParameterBoundToExtractorType = typeParameter;
			}

			if ( Objects.equals( extractorDescriptor.getExtractedTypeParameter(), typeParameterBoundToExtractorType ) ) {
				typeParameterCompatibleExtractors.add( extractorDescriptor );
			}
		}

		return getMostSpecific( valueType, typeParameterCompatibleExtractors );
	}

	private ValueExtractorDescriptor getMostSpecific(Class<?> valueType, List<ValueExtractorDescriptor> extractors) {
		Set<ValueExtractorDescriptor> candidates = CollectionHelper.newHashSet( extractors.size() );

		for ( ValueExtractorDescriptor descriptor : extractors ) {
			if ( candidates.isEmpty() ) {
				candidates.add( descriptor );
				continue;
			}
			Iterator<ValueExtractorDescriptor> candidatesIterator = candidates.iterator();
			boolean isNewRoot = true;
			while ( candidatesIterator.hasNext() ) {
				ValueExtractorDescriptor candidate = candidatesIterator.next();
				if ( TypeHelper.isAssignable( candidate.getContainerType(), descriptor.getContainerType() ) ) {
					candidatesIterator.remove();
				}
				else if ( TypeHelper.isAssignable( descriptor.getContainerType(), candidate.getContainerType() ) ) {
					isNewRoot = false;
				}
			}
			if ( isNewRoot ) {
				candidates.add( descriptor );
			}
		}

		if ( candidates.isEmpty() ) {
			return null;
		}
		else if ( candidates.size() == 1 ) {
			return candidates.iterator().next();
		}
		else {
			@SuppressWarnings("rawtypes")
			List<Class<? extends ValueExtractor>> valueExtractorCandidates = candidates.stream()
					.map( valueExtractorDescriptor -> valueExtractorDescriptor.getValueExtractor().getClass() )
					.collect( Collectors.toList() );
			throw LOG.unableToGetMostSpecificValueExtractorDueToSeveralValueExtractorsDefinedForParallelHierarchies( valueType, valueExtractorCandidates );
		}
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
		return bindings != null ? bindings.get( typeParameter ) : null;
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
