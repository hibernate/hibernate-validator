/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Gunnar Morling
 *
 */
public class TypeVariableBindings {

	private TypeVariableBindings() {
	}

	/**
	 * Returns the bindings for all the type variables from the given type's hierarchy. The returned map will contain an
	 * entry for each type in the given type's class hierarchy (including interfaces). Each entry is a map from the
	 * given type's type variables to the corresponding type variables of the type represented by that entry.
	 */
	public static Map<Class<?>, Map<TypeVariable<?>, TypeVariable<?>>> getTypeVariableBindings(Class<?> type) {
		Map<Class<?>, Map<TypeVariable<?>, TypeVariable<?>>> allBindings = new HashMap<>();
		Map<TypeVariable<?>, TypeVariable<?>> currentBindings = new HashMap<>();

		TypeVariable<?>[] subTypeParameters = type.getTypeParameters();
		for ( TypeVariable<?> typeVariable : subTypeParameters ) {
			currentBindings.put( typeVariable, typeVariable );
		}
		allBindings.put( type, currentBindings );

		collectTypeBindings( type, allBindings, currentBindings );

		allBindings.put( Object.class, Collections.emptyMap() );

		return CollectionHelper.toImmutableMap( allBindings );
	}

	private static void collectTypeBindings(Class<?> subType, Map<Class<?>, Map<TypeVariable<?>, TypeVariable<?>>> allBindings, Map<TypeVariable<?>, TypeVariable<?>> bindings) {
		processGenericSuperType( allBindings, bindings, subType.getGenericSuperclass() );

		for ( Type genericInterface : subType.getGenericInterfaces() ) {
			processGenericSuperType( allBindings, bindings, genericInterface );
		}
	}

	private static void processGenericSuperType(Map<Class<?>, Map<TypeVariable<?>, TypeVariable<?>>> allBindings,
			Map<TypeVariable<?>, TypeVariable<?>> bindings, Type genericSuperType) {
		if ( genericSuperType == null ) {
			return;
		}
		else if ( genericSuperType instanceof ParameterizedType ) {
			Map<TypeVariable<?>, TypeVariable<?>> newBindings = new HashMap<>();
			Type[] typeArguments = ( (ParameterizedType) genericSuperType ).getActualTypeArguments();
			TypeVariable<?>[] typeParameters = ( (Class<?>) ( (ParameterizedType) genericSuperType ).getRawType() ).getTypeParameters();

			for ( int i = 0; i < typeArguments.length; i++ ) {
				Type typeArgument = typeArguments[i];
				TypeVariable<?> typeParameter = typeParameters[i];

				boolean typeParameterFoundInSubType = false;
				for ( Entry<TypeVariable<?>, TypeVariable<?>> subTypeParameter : bindings.entrySet() ) {
					if ( typeArgument.equals( subTypeParameter.getValue() ) ) {
						newBindings.put( subTypeParameter.getKey(), typeParameter );
						typeParameterFoundInSubType = true;
					}
				}
				if ( !typeParameterFoundInSubType ) {
					newBindings.put( typeParameter, typeParameter );
				}
			}

			allBindings.put( ( (Class<?>) ( (ParameterizedType) genericSuperType ).getRawType() ), newBindings );
			collectTypeBindings( ( (Class<?>) ( (ParameterizedType) genericSuperType ).getRawType() ), allBindings, newBindings );
		}
		else if ( genericSuperType instanceof Class ) {
			allBindings.put( (Class<?>) genericSuperType, Collections.emptyMap() );
			collectTypeBindings( (Class<?>) genericSuperType, allBindings, new HashMap<>() );
		}
		else {
			throw new IllegalArgumentException( "Unexpected type: " + genericSuperType );
		}
	}
}
