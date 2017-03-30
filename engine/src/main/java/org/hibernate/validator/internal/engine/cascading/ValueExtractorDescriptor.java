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
import java.util.ArrayList;
import java.util.List;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.UnwrapByDefault;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.util.ReflectionHelper;
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

	private final Key key;
	private final ValueExtractor<?> valueExtractor;
	private final boolean unwrapByDefault;

	public ValueExtractorDescriptor(ValueExtractor<?> valueExtractor) {
		this.key = new Key(
				getExtractedType( valueExtractor.getClass() ),
				getExtractedTypeParameter( valueExtractor.getClass() )
		);
		this.valueExtractor = valueExtractor;
		this.unwrapByDefault = hasUnwrapByDefaultAnnotation( valueExtractor.getClass() );
	}

	@SuppressWarnings("rawtypes")
	private static TypeVariable<?> getExtractedTypeParameter(Class<? extends ValueExtractor> extractorImplementationType) {
		AnnotatedParameterizedType valueExtractorDefinition = getValueExtractorDefinition( extractorImplementationType );
		AnnotatedType extractedType = valueExtractorDefinition.getAnnotatedActualTypeArguments()[0];
		Class<?> extractedTypeRaw = (Class<?>) TypeHelper.getErasedType( extractedType.getType() );

		TypeVariable<?> extractedTypeParameter = null;

		if ( extractedType.isAnnotationPresent( ExtractedValue.class ) ) {
			if ( extractedType instanceof AnnotatedArrayType ) {
				extractedTypeParameter = new ArrayElement( (AnnotatedArrayType) extractedType );
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

	@SuppressWarnings("rawtypes")
	private static Class<?> getExtractedType(Class<? extends ValueExtractor> extractorImplementationType) {
		AnnotatedParameterizedType genericInterface = getValueExtractorDefinition( extractorImplementationType );
		AnnotatedType extractedType = genericInterface.getAnnotatedActualTypeArguments()[0];
		return TypeHelper.getErasedReferenceType( extractedType.getType() );
	}

	private static AnnotatedParameterizedType getValueExtractorDefinition(Class<?> extractorImplementationType) {
		List<AnnotatedType> valueExtractorAnnotatedTypes = new ArrayList<>();

		determineValueExtractorDefinitions( valueExtractorAnnotatedTypes, extractorImplementationType );

		if ( valueExtractorAnnotatedTypes.size() == 1 ) {
			return (AnnotatedParameterizedType) valueExtractorAnnotatedTypes.get( 0 );
		}
		else if ( valueExtractorAnnotatedTypes.size() > 1 ) {
			throw LOG.getParallelDefinitionsOfValueExtractorException( extractorImplementationType );
		}
		else {
			throw new AssertionError( extractorImplementationType.getName() + " should be a subclass of " + ValueExtractor.class.getSimpleName() );
		}
	}

	private static void determineValueExtractorDefinitions(List<AnnotatedType> valueExtractorDefinitions, Class<?> extractorImplementationType) {
		if ( !ValueExtractor.class.isAssignableFrom( extractorImplementationType ) ) {
			return;
		}

		Class<?> superClass = extractorImplementationType.getSuperclass();
		if ( superClass != null && !Object.class.equals( superClass ) ) {
			determineValueExtractorDefinitions( valueExtractorDefinitions, superClass );
		}
		for ( Class<?> implementedInterface : extractorImplementationType.getInterfaces() ) {
			if ( !ValueExtractor.class.equals( implementedInterface ) ) {
				determineValueExtractorDefinitions( valueExtractorDefinitions, implementedInterface );
			}
		}
		for ( AnnotatedType annotatedInterface : extractorImplementationType.getAnnotatedInterfaces() ) {
			if ( ValueExtractor.class.equals( ReflectionHelper.getClassFromType( annotatedInterface.getType() ) ) ) {
				valueExtractorDefinitions.add( annotatedInterface );
			}
		}
	}

	private static boolean hasUnwrapByDefaultAnnotation(Class<?> extractorImplementationType) {
		return extractorImplementationType.isAnnotationPresent( UnwrapByDefault.class );
	}

	public Key getKey() {
		return key;
	}

	public Class<?> getExtractedType() {
		return key.extractedType;
	}

	public TypeVariable<?> getExtractedTypeParameter() {
		return key.extractedTypeParameter;
	}

	public ValueExtractor<?> getValueExtractor() {
		return valueExtractor;
	}

	public boolean isUnwrapByDefault() {
		return unwrapByDefault;
	}

	@Override
	public String toString() {
		return "ValueExtractorDescriptor [key=" + key + ", valueExtractor=" + valueExtractor + ", unwrapByDefault=" + unwrapByDefault + "]";
	}

	public static class Key {

		private final Class<?> extractedType;
		private final TypeVariable<?> extractedTypeParameter;
		private final int hashCode;

		public Key(Class<?> extractedType, TypeVariable<?> extractedTypeParameter) {
			this.extractedType = extractedType;
			this.extractedTypeParameter = extractedTypeParameter;
			this.hashCode = buildHashCode( extractedType, extractedTypeParameter );
		}

		private static int buildHashCode(Type extractedType, TypeVariable<?> extractedTypeParameter) {
			final int prime = 31;
			int result = 1;
			result = prime * result + extractedType.hashCode();
			result = prime * result + extractedTypeParameter.hashCode();
			return result;
		}

		@Override
		public int hashCode() {
			return hashCode;
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
			Key other = (Key) obj;

			return extractedType.equals( other.extractedType ) &&
					extractedTypeParameter.equals( other.extractedTypeParameter );
		}

		@Override
		public String toString() {
			return "Key [extractedType=" + StringHelper.toShortString( extractedType ) + ", extractedTypeParameter=" + extractedTypeParameter + "]";
		}
	}
}
