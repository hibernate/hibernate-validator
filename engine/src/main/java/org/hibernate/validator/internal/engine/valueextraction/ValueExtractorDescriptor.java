/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.UnwrapByDefault;
import jakarta.validation.valueextraction.ValueExtractor;

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

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup()  );

	private final Key key;
	private final ValueExtractor<?> valueExtractor;
	private final boolean unwrapByDefault;
	private final Optional<Class<?>> extractedType;

	public ValueExtractorDescriptor(ValueExtractor<?> valueExtractor) {
		AnnotatedParameterizedType valueExtractorDefinition = getValueExtractorDefinition( valueExtractor.getClass() );

		this.key = new Key(
				getContainerType( valueExtractorDefinition, valueExtractor.getClass() ),
				getExtractedTypeParameter( valueExtractorDefinition, valueExtractor.getClass() )
		);
		this.valueExtractor = valueExtractor;
		this.unwrapByDefault = hasUnwrapByDefaultAnnotation( valueExtractor.getClass() );
		this.extractedType = getExtractedType( valueExtractorDefinition );
	}

	ValueExtractorDescriptor(ValueExtractor<?> valueExtractor,
			Class<?> containerType,
			TypeVariable<?> extractedTypeParameter,
			boolean unwrapByDefault,
			Optional<Class<?>> extractedType) {
		this.key = new Key(
				containerType,
				extractedTypeParameter );
		this.valueExtractor = valueExtractor;
		this.unwrapByDefault = unwrapByDefault;
		this.extractedType = extractedType;
	}

	@SuppressWarnings("rawtypes")
	private static TypeVariable<?> getExtractedTypeParameter(AnnotatedParameterizedType valueExtractorDefinition,
			Class<? extends ValueExtractor> extractorImplementationType) {
		AnnotatedType containerType = valueExtractorDefinition.getAnnotatedActualTypeArguments()[0];
		Class<?> containerTypeRaw = (Class<?>) TypeHelper.getErasedType( containerType.getType() );

		TypeVariable<?> extractedTypeParameter = null;

		if ( containerType.isAnnotationPresent( ExtractedValue.class ) ) {
			if ( containerType instanceof AnnotatedArrayType ) {
				extractedTypeParameter = new ArrayElement( (AnnotatedArrayType) containerType );
			}
			else {
				extractedTypeParameter = AnnotatedObject.INSTANCE;
			}
		}

		if ( containerType instanceof AnnotatedParameterizedType ) {
			AnnotatedParameterizedType parameterizedExtractedType = (AnnotatedParameterizedType) containerType;
			int i = 0;
			for ( AnnotatedType typeArgument : parameterizedExtractedType.getAnnotatedActualTypeArguments() ) {
				if ( !TypeHelper.isUnboundWildcard( typeArgument.getType() ) ) {
					throw LOG.getOnlyUnboundWildcardTypeArgumentsSupportedForContainerTypeOfValueExtractorException( extractorImplementationType );
				}
				if ( typeArgument.isAnnotationPresent( ExtractedValue.class ) ) {
					if ( extractedTypeParameter != null ) {
						throw LOG.getValueExtractorDeclaresExtractedValueMultipleTimesException( extractorImplementationType );
					}
					if ( !void.class.equals( typeArgument.getAnnotation( ExtractedValue.class ).type() ) ) {
						throw LOG.getExtractedValueOnTypeParameterOfContainerTypeMayNotDefineTypeAttributeException( extractorImplementationType );
					}

					extractedTypeParameter = containerTypeRaw.getTypeParameters()[i];
				}
				i++;
			}
		}

		if ( extractedTypeParameter == null ) {
			throw LOG.getValueExtractorFailsToDeclareExtractedValueException( extractorImplementationType );
		}

		return extractedTypeParameter;
	}

	private static Optional<Class<?>> getExtractedType(AnnotatedParameterizedType valueExtractorDefinition) {
		AnnotatedType containerType = valueExtractorDefinition.getAnnotatedActualTypeArguments()[0];

		if ( containerType.isAnnotationPresent( ExtractedValue.class ) ) {
			Class<?> extractedType = containerType.getAnnotation( ExtractedValue.class ).type();
			if ( !void.class.equals( extractedType ) ) {
				return Optional.of( ReflectionHelper.boxedType( extractedType ) );
			}
		}

		return Optional.empty();
	}

	@SuppressWarnings("rawtypes")
	private static Class<?> getContainerType(AnnotatedParameterizedType valueExtractorDefinition, Class<? extends ValueExtractor> extractorImplementationType) {
		AnnotatedType containerType = valueExtractorDefinition.getAnnotatedActualTypeArguments()[0];
		return TypeHelper.getErasedReferenceType( containerType.getType() );
	}

	private static AnnotatedParameterizedType getValueExtractorDefinition(Class<?> extractorImplementationType) {
		List<AnnotatedType> valueExtractorAnnotatedTypes = new ArrayList<>();

		determineValueExtractorDefinitions( valueExtractorAnnotatedTypes, extractorImplementationType );

		if ( valueExtractorAnnotatedTypes.size() == 1 ) {
			return (AnnotatedParameterizedType) valueExtractorAnnotatedTypes.get( 0 );
		}
		else if ( valueExtractorAnnotatedTypes.size() > 1 ) {
			throw LOG.getParallelDefinitionsOfValueExtractorsException( extractorImplementationType );
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

	public Class<?> getContainerType() {
		return key.containerType;
	}

	public TypeVariable<?> getExtractedTypeParameter() {
		return key.extractedTypeParameter;
	}

	public Optional<Class<?>> getExtractedType() {
		return extractedType;
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

		private final Class<?> containerType;
		private final TypeVariable<?> extractedTypeParameter;
		private final int hashCode;

		public Key(Class<?> containerType, TypeVariable<?> extractedTypeParameter) {
			this.containerType = containerType;
			this.extractedTypeParameter = extractedTypeParameter;
			this.hashCode = buildHashCode( containerType, extractedTypeParameter );
		}

		private static int buildHashCode(Type containerType, TypeVariable<?> extractedTypeParameter) {
			final int prime = 31;
			int result = 1;
			result = prime * result + containerType.hashCode();
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

			return containerType.equals( other.containerType ) &&
					extractedTypeParameter.equals( other.extractedTypeParameter );
		}

		@Override
		public String toString() {
			return "Key [containerType=" + StringHelper.toShortString( containerType ) + ", extractedTypeParameter=" + extractedTypeParameter + "]";
		}
	}
}
