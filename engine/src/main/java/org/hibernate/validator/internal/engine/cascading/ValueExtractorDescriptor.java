/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.spi.cascading.ExtractedValue;
import org.hibernate.validator.spi.cascading.ValueExtractor;

/**
 * Describes a {@link ValueExtractor}.
 *
 * @author Gunnar Morling
 */
public class ValueExtractorDescriptor {

	private final ValueExtractor<?> valueExtractor;
	private final Type extractedType;
	private final TypeVariable<?> extractedTypeParameter;

	public ValueExtractorDescriptor(ValueExtractor<?> valueExtractor) {
		this.valueExtractor = valueExtractor;
		this.extractedTypeParameter = getExtractedTypeParameter( valueExtractor.getClass() );
		this.extractedType = getExtractedType( valueExtractor.getClass() );
	}

	private static TypeVariable<?> getExtractedTypeParameter(Class<?> extractorImplementationType) {
		// TODO deal with indirect implementations (MyExtractor -> BaseExtractor -> ValueExtractor)
		AnnotatedType genericInterface = extractorImplementationType.getAnnotatedInterfaces()[0];
		AnnotatedType extractedType = ( (AnnotatedParameterizedType) genericInterface ).getAnnotatedActualTypeArguments()[0];
		Class<?> extractedTypeRaw = (Class<?>) TypeHelper.getErasedType( extractedType.getType() );

		if ( extractedType instanceof AnnotatedParameterizedType ) {
			AnnotatedParameterizedType parameterizedExtractedType = (AnnotatedParameterizedType) extractedType;

			int i = 0;
			for ( AnnotatedType typeArgument : parameterizedExtractedType.getAnnotatedActualTypeArguments() ) {
				// TODO raise error if given several times
				if ( typeArgument.isAnnotationPresent( ExtractedValue.class ) ) {
					return extractedTypeRaw.getTypeParameters()[i];
				}
				i++;
			}
		}
		else if ( extractedType instanceof AnnotatedArrayType ) {
			return ArrayElement.INSTANCE;
		}

		return AnnotatedObject.INSTANCE;
	}

	private static Type getExtractedType(Class<?> extractorImplementationType) {
		// TODO deal with indirect implementations (MyExtractor -> BaseExtractor -> ValueExtractor)
		AnnotatedType genericInterface = extractorImplementationType.getAnnotatedInterfaces()[0];
		AnnotatedType extractedType = ( (AnnotatedParameterizedType) genericInterface ).getAnnotatedActualTypeArguments()[0];
		return extractedType.getType();
	}

	public Type getExtractedType() {
		return extractedType;
	}

	public TypeVariable<?> extractedTypeParameter() {
		return extractedTypeParameter;
	}

	public ValueExtractor<?> getValueExtractor() {
		return valueExtractor;
	}

	@Override
	public String toString() {
		return "ValueExtractorDescriptor [valueExtractor=" + StringHelper.toShortString( valueExtractor.getClass() ) + ", extractedType=" + StringHelper.toShortString( extractedType )
				+ ", extractedTypeParameter=" + extractedTypeParameter + "]";
	}
}
