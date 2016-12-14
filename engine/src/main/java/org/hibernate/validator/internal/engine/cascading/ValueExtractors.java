/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.TypeVariableBindings;
import org.hibernate.validator.spi.cascading.ValueExtractor;

/**
 * @author Gunnar Morling
 */
public class ValueExtractors {

	private final List<ValueExtractorDescriptor> valueExtractors;

	public ValueExtractors(Iterable<ValueExtractor<?>> externalExtractors) {
		List<ValueExtractorDescriptor> tmpValueExtractors = new ArrayList<>();

		for ( ValueExtractor<?> valueExtractor : externalExtractors ) {
			tmpValueExtractors.add( new ValueExtractorDescriptor( valueExtractor ) );
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

		tmpValueExtractors.add( new ValueExtractorDescriptor( LegacyIterableValueExtractor.INSTANCE ) );
		tmpValueExtractors.add( new ValueExtractorDescriptor( IterableValueExtractor.INSTANCE ) );

		tmpValueExtractors.add( new ValueExtractorDescriptor( ObjectValueExtractor.INSTANCE ) );

		valueExtractors = Collections.unmodifiableList( tmpValueExtractors );
	}

	public ValueExtractor<?> getCascadedValueExtractor(Class<?> valueType, TypeVariable<?> cascadingTypeParameter) {
		Map<Class<?>, Map<TypeVariable<?>, TypeVariable<?>>> allBindings = TypeVariableBindings.getTypeVariableBindings( valueType );

		List<ValueExtractorDescriptor> typeCompatibleExtractors = valueExtractors.stream()
			.filter( e -> TypeHelper.isAssignable( e.getExtractedType(), valueType ) )
			.collect( Collectors.toList() );


		for ( ValueExtractorDescriptor extractorDescriptor : typeCompatibleExtractors ) {
			Map<TypeVariable<?>, TypeVariable<?>> bindingsForExtractorType = allBindings.get( TypeHelper.getErasedReferenceType( extractorDescriptor.getExtractedType() ) );

			TypeVariable<?> cascadingParameterBoundToExtractorType = bind( cascadingTypeParameter, bindingsForExtractorType );
			if ( cascadingParameterBoundToExtractorType.equals( extractorDescriptor.extractedTypeParameter() ) ) {
				// TODO implement selection of most specific extractor per requested type parameter
				return extractorDescriptor.getValueExtractor();
			}
		}

		throw new IllegalArgumentException( "No extractor found" );
	}

	private TypeVariable<?> bind(TypeVariable<?> typeParameter, Map<TypeVariable<?>, TypeVariable<?>> bindings) {
		TypeVariable<?> bound = null;

		if ( bindings != null ) {
			bound = bindings.get( typeParameter );
		}

		return bound != null ? bound : AnnotatedObject.INSTANCE;
	}
}
