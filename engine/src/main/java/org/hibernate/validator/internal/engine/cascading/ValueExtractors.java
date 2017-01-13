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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ValidationException;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.TypeVariableBindings;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;

/**
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class ValueExtractors {

	private final List<ValueExtractorDescriptor> valueExtractors;

	public ValueExtractors(Iterable<ValueExtractor<?>> externalExtractors) {
		List<ValueExtractorDescriptor> tmpValueExtractors = new ArrayList<>();

		for ( ValueExtractor<?> valueExtractor : externalExtractors ) {
			tmpValueExtractors.add( new ValueExtractorDescriptor( valueExtractor ) );
		}

		if ( isJavaFxInClasspath() ) {
			tmpValueExtractors.add( new ValueExtractorDescriptor( ObservableValueExtractor.INSTANCE ) );
		}

		tmpValueExtractors.add( new ValueExtractorDescriptor( LegacyListValueExtractor.INSTANCE ) );
		tmpValueExtractors.add( new ValueExtractorDescriptor( ListValueExtractor.INSTANCE ) );

		tmpValueExtractors.add( new ValueExtractorDescriptor( ByteArrayValueExtractor.INSTANCE ) );
		tmpValueExtractors.add( new ValueExtractorDescriptor( ShortArrayValueExtractor.INSTANCE ) );
		tmpValueExtractors.add( new ValueExtractorDescriptor( IntArrayValueExtractor.INSTANCE ) );
		tmpValueExtractors.add( new ValueExtractorDescriptor( LongArrayValueExtractor.INSTANCE ) );
		tmpValueExtractors.add( new ValueExtractorDescriptor( FloatArrayValueExtractor.INSTANCE ) );
		tmpValueExtractors.add( new ValueExtractorDescriptor( DoubleArrayValueExtractor.INSTANCE ) );
		tmpValueExtractors.add( new ValueExtractorDescriptor( CharArrayValueExtractor.INSTANCE ) );
		tmpValueExtractors.add( new ValueExtractorDescriptor( BooleanArrayValueExtractor.INSTANCE ) );
		tmpValueExtractors.add( new ValueExtractorDescriptor( ObjectArrayValueExtractor.INSTANCE ) );

		tmpValueExtractors.add( new ValueExtractorDescriptor( LegacyMapValueExtractor.INSTANCE ) );
		tmpValueExtractors.add( new ValueExtractorDescriptor( MapValueExtractor.INSTANCE ) );
		tmpValueExtractors.add( new ValueExtractorDescriptor( MapKeyExtractor.INSTANCE ) );

		tmpValueExtractors.add( new ValueExtractorDescriptor( LegacyIterableValueExtractor.INSTANCE ) );
		tmpValueExtractors.add( new ValueExtractorDescriptor( IterableValueExtractor.INSTANCE ) );

		tmpValueExtractors.add( new ValueExtractorDescriptor( LegacyOptionalValueExtractor.INSTANCE ) );
		tmpValueExtractors.add( new ValueExtractorDescriptor( OptionalValueExtractor.INSTANCE ) );
		tmpValueExtractors.add( new ValueExtractorDescriptor( ObjectValueExtractor.INSTANCE ) );

		valueExtractors = Collections.unmodifiableList( tmpValueExtractors );
	}

	/**
	 * Returns the most specific value extractor extracting the given type or {@code null} if none was found.
	 */
	public ValueExtractorDescriptor getValueExtractor(Class<?> valueType) {
		List<ValueExtractorDescriptor> typeCompatibleExtractors = valueExtractors.stream()
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

	public ValueExtractorDescriptor getCascadedValueExtractor(Class<?> valueType, TypeVariable<?> cascadingTypeParameter) {
		Map<Class<?>, Map<TypeVariable<?>, TypeVariable<?>>> allBindings = null;

		if ( cascadingTypeParameter != AnnotatedObject.INSTANCE && cascadingTypeParameter != ArrayElement.INSTANCE ) {
			allBindings = TypeVariableBindings.getTypeVariableBindings( (Class<?>) cascadingTypeParameter.getGenericDeclaration() );
		}

		List<ValueExtractorDescriptor> typeCompatibleExtractors = valueExtractors.stream()
			.filter( e -> TypeHelper.isAssignable( TypeHelper.getErasedReferenceType( e.getExtractedType() ), valueType ) )
			.collect( Collectors.toList() );

		for ( ValueExtractorDescriptor extractorDescriptor : typeCompatibleExtractors ) {
			TypeVariable<?> cascadingParameterBoundToExtractorType;
			if ( cascadingTypeParameter != AnnotatedObject.INSTANCE && cascadingTypeParameter != ArrayElement.INSTANCE ) {
				Map<TypeVariable<?>, TypeVariable<?>> bindingsForExtractorType = allBindings.get( TypeHelper.getErasedReferenceType( extractorDescriptor.getExtractedType() ) );
				cascadingParameterBoundToExtractorType = bind( cascadingTypeParameter, bindingsForExtractorType );
			}
			else {
				cascadingParameterBoundToExtractorType = cascadingTypeParameter;
			}

			if ( cascadingParameterBoundToExtractorType.equals( extractorDescriptor.extractedTypeParameter() ) ) {
				// TODO implement selection of most specific extractor per requested type parameter
				return extractorDescriptor;
			}
		}

		// TODO should only happen during transition off value unwrappers
		return null;
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
