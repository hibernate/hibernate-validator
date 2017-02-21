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

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.UnwrapByDefault;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Describes a {@link ValueExtractor}.
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class ValueExtractorDescriptor {

	private static final Log LOG = LoggerFactory.make();

	private final ValueExtractor<?> valueExtractor;
	private final Type extractedType;
	private final TypeVariable<?> extractedTypeParameter;
	private final boolean unwrapByDefault;

	public ValueExtractorDescriptor(ValueExtractor<?> valueExtractor) {
		this.valueExtractor = valueExtractor;
		this.extractedTypeParameter = getExtractedTypeParameter( valueExtractor.getClass() );
		this.extractedType = getExtractedType( valueExtractor.getClass() );
		this.unwrapByDefault = hasUnwrapByDefaultAnnotation( valueExtractor.getClass() );
	}

	private static TypeVariable<?> getExtractedTypeParameter(Class<?> extractorImplementationType) {
		// TODO deal with indirect implementations (MyExtractor -> BaseExtractor -> ValueExtractor)
		AnnotatedType genericInterface = extractorImplementationType.getAnnotatedInterfaces()[0];
		AnnotatedType extractedType = ( (AnnotatedParameterizedType) genericInterface ).getAnnotatedActualTypeArguments()[0];
		Class<?> extractedTypeRaw = (Class<?>) TypeHelper.getErasedType( extractedType.getType() );

		TypeVariable<?> extractedTypeParameter = null;

		if ( extractedType.isAnnotationPresent( ExtractedValue.class ) ) {
			if ( extractedType instanceof AnnotatedArrayType ) {
				extractedTypeParameter = ArrayElement.INSTANCE;
			}
			else {
				extractedTypeParameter = AnnotatedObject.INSTANCE;
			}
		}

		if ( extractedType instanceof AnnotatedParameterizedType ) {
			AnnotatedParameterizedType parameterizedExtractedType = (AnnotatedParameterizedType) extractedType;
			int i = 0;
			for ( AnnotatedType typeArgument : parameterizedExtractedType.getAnnotatedActualTypeArguments() ) {
				if ( typeArgument.isAnnotationPresent( ExtractedValue.class ) ) {
					if ( extractedTypeParameter != null ) {
						throw LOG.getValueExtractorDeclaresExtractedValueMultipleTimesException( extractorImplementationType );
					}

					extractedTypeParameter = extractedTypeRaw.getTypeParameters()[i];
				}
				i++;
			}
		}

		if ( extractedTypeParameter == null ) {
			throw LOG.getValueExtractorFailsToDeclareExtractedValueException( extractorImplementationType );
		}

		return extractedTypeParameter;
	}

	private static Type getExtractedType(Class<?> extractorImplementationType) {
		// TODO deal with indirect implementations (MyExtractor -> BaseExtractor -> ValueExtractor)
		AnnotatedType genericInterface = extractorImplementationType.getAnnotatedInterfaces()[0];
		AnnotatedType extractedType = ( (AnnotatedParameterizedType) genericInterface ).getAnnotatedActualTypeArguments()[0];
		return extractedType.getType();
	}

	private static boolean hasUnwrapByDefaultAnnotation(Class<?> extractorImplementationType) {
		return extractorImplementationType.isAnnotationPresent( UnwrapByDefault.class );
	}

	public Type getExtractedType() {
		return extractedType;
	}

	public TypeVariable<?> getExtractedTypeParameter() {
		return extractedTypeParameter;
	}

	public ValueExtractor<?> getValueExtractor() {
		return valueExtractor;
	}

	public boolean isUnwrapByDefault() {
		return unwrapByDefault;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + extractedType.hashCode();
		result = prime * result + extractedTypeParameter.hashCode();
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
		ValueExtractorDescriptor other = (ValueExtractorDescriptor) obj;

		return extractedTypeParameter.equals( other.extractedTypeParameter );
	}

	@Override
	public String toString() {
		return "ValueExtractorDescriptor [valueExtractor=" + StringHelper.toShortString( valueExtractor.getClass() ) + ", extractedType=" + StringHelper.toShortString( extractedType )
				+ ", extractedTypeParameter=" + extractedTypeParameter + ", unwrapByDefault=" + unwrapByDefault + "]";
	}
}
