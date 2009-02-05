// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validation.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintValidator;
import javax.validation.ValidationException;


/**
 * Helper methods around <code>ConstraintValidator</code> types.
 *
 * @author Emmanuel Bernanrd
 * @author Hardy Ferentschik
 */
public class ValidatorTypeHelper {
	private static final int VALIDATOR_TYPE_INDEX = 1;

	/**
	 * @param validators List of constraint validator classes (for a given constraint).
	 *
	 * @return Return a Map&lt;Class, Class&lt;? extends ConstraintValidator&gt;&gt; where the map
	 *         key is the type the validator accepts and value the validator class itself.
	 */
	public static Map<Class<?>, Class<? extends ConstraintValidator<? extends Annotation, ?>>>
	getValidatorsTypes(List<Class<? extends ConstraintValidator<?, ?>>> validators) {
		if ( validators == null || validators.size() == 0 ) {
			throw new ValidationException( "No ConstraintValidators associated to @Constraint" );
		}
		else {
			Map<Class<?>, Class<? extends ConstraintValidator<? extends Annotation, ?>>> validatorsTypes =
					new HashMap<Class<?>, Class<? extends ConstraintValidator<? extends Annotation, ?>>>();
			for ( Class<? extends ConstraintValidator<? extends Annotation, ?>> validator : validators ) {
				validatorsTypes.put( extractType( validator ), validator );
			}
			return validatorsTypes;
		}
	}

	private static Class<?> extractType(Class<? extends ConstraintValidator<?, ?>> validator) {
		Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
		Type constraintValidatorType = resolveTypes( resolvedTypes, validator );

		//we now have all bind from a type to its resolution at one level
		Type validatorType = ( ( ParameterizedType ) constraintValidatorType ).getActualTypeArguments()[VALIDATOR_TYPE_INDEX];
		if ( validatorType == null ) {
			throw new ValidationException( "Null is an invalid type for a constraint validator." );
		}
		else if ( validatorType instanceof GenericArrayType ) {
			validatorType = Array.class;
		}

		while ( resolvedTypes.containsKey( validatorType ) ) {
			validatorType = resolvedTypes.get( validatorType );
		}
		//FIXME raise an exception if validatorType is not a class
		return ( Class<?> ) validatorType;
	}

	private static Type resolveTypes(Map<Type, Type> resolvedTypes, Type type) {
		if ( type == null ) {
			return null;
		}
		else if ( type instanceof Class ) {
			Class clazz = ( Class ) type;
			Type returnedType = resolveTypes( resolvedTypes, clazz.getGenericSuperclass() );
			if ( returnedType != null ) {
				return returnedType;
			}
			for ( Type genericInterface : clazz.getGenericInterfaces() ) {
				returnedType = resolveTypes( resolvedTypes, genericInterface );
				if ( returnedType != null ) {
					return returnedType;
				}
			}
		}
		else if ( type instanceof ParameterizedType ) {
			ParameterizedType paramType = ( ParameterizedType ) type;
			if ( !( paramType.getRawType() instanceof Class ) ) {
				return null; //don't know what to do here
			}
			Class<?> rawType = ( Class<?> ) paramType.getRawType();

			TypeVariable<?>[] originalTypes = rawType.getTypeParameters();
			Type[] partiallyResolvedTypes = paramType.getActualTypeArguments();
			int nbrOfParams = originalTypes.length;
			for ( int i = 0; i < nbrOfParams; i++ ) {
				resolvedTypes.put( originalTypes[i], partiallyResolvedTypes[i] );
			}

			if ( rawType.equals( ConstraintValidator.class ) ) {
				//we found our baby
				return type;
			}
			else {
				resolveTypes( resolvedTypes, rawType.getGenericSuperclass() );
				for ( Type genericInterface : rawType.getGenericInterfaces() ) {
					resolveTypes( resolvedTypes, genericInterface );
				}
			}
		}
		//else we don't care I think
		return null;
	}
}
