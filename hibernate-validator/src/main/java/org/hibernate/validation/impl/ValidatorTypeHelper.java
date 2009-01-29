package org.hibernate.validation.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import javax.validation.ConstraintValidator;
import javax.validation.ValidationException;


/**
 * Helper methods around ConstraintValidator types
 */
public class ValidatorTypeHelper {
	private static final int VALIDATOR_TYPE_INDEX = 1;

	/**
	 * for a lsit of validators, return a Map<Class, Class&lt;? extends ConstraintValidator&gt;&gt;
	 * The key is the type the validator accepts, the value is the validator class itself

	 */
	public static Map<Class<?>, Class<? extends ConstraintValidator<? extends Annotation, ?>>>
	    getValidatorsTypes(Class<? extends ConstraintValidator<? extends Annotation,?>>[] validators) {
		if (validators == null || validators.length == 0) {
			//TODObe more contextually specific
			throw new ValidationException("No ConstraintValidators associated to @Constraint");
		}
		else {
			Map<Class<?>, Class<? extends ConstraintValidator<? extends Annotation, ?>>> validatorsTypes =
					new HashMap<Class<?>, Class<? extends ConstraintValidator<? extends Annotation, ?>>>();
			for (Class<? extends ConstraintValidator<? extends Annotation,?>> validator : validators) {
				validatorsTypes.put( extractType(validator), validator );
			}
			return validatorsTypes;
		}
	}

	private static Class<?> extractType(Class<? extends ConstraintValidator<?,?>> validator) {

		Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
		Type constraintValidatorType = resolveTypes(resolvedTypes, validator);

		//we now have all bind from a type to its resolution at one level

		//FIXME throw assertion exception if constraintValidatorType == null
		Type validatorType = ( (ParameterizedType) constraintValidatorType ).getActualTypeArguments()[VALIDATOR_TYPE_INDEX];
		while ( resolvedTypes.containsKey( validatorType ) ) {
			validatorType = resolvedTypes.get( validatorType );
		}
		//FIXME raise an exception if validatorType is not a class
		return (Class<?>) validatorType;
	}

	//TEst method, remove
	public static Type extractTypeLoose(Class<? extends ConstraintValidator<?,?>> validator) {

		Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
		Type constraintValidatorType = resolveTypes(resolvedTypes, validator);

		//we now have all bind from a type to its resolution at one level

		//FIXME throw assertion exception if constraintValidatorType == null
		Type validatorType = ( (ParameterizedType) constraintValidatorType ).getActualTypeArguments()[VALIDATOR_TYPE_INDEX];
		while ( resolvedTypes.containsKey( validatorType ) ) {
			validatorType = resolvedTypes.get( validatorType );
		}
		//FIXME raise an exception if validatorType is not a class
		return validatorType;
	}

	private static Type resolveTypes(Map<Type, Type> resolvedTypes, Type type) {
		if (type == null) {
			return null;
		}
		else if ( type instanceof Class ) {
			Class<?> clazz = ( Class<?> ) type;
			Type returnedType = resolveTypes(resolvedTypes, clazz.getGenericSuperclass() );
			if (returnedType != null) return returnedType;
			for (Type genericInterface : clazz.getGenericInterfaces() ) {
				returnedType = resolveTypes(resolvedTypes, genericInterface );
				if (returnedType != null) return returnedType;
			}
		}
		else if ( type instanceof ParameterizedType ) {
			ParameterizedType paramType = (ParameterizedType) type;
			if ( ! ( paramType.getRawType() instanceof Class ) ) return null; //don't know what to do here
			Class<?> rawType = (Class<?>) paramType.getRawType();

			TypeVariable<?>[] originalTypes = rawType.getTypeParameters();
			Type[] partiallyResolvedTypes = paramType.getActualTypeArguments();
			int nbrOfParams = originalTypes.length;
			for (int i = 0 ; i < nbrOfParams; i++) {
				resolvedTypes.put( originalTypes[i], partiallyResolvedTypes[i] );
			}

			if ( rawType.equals( ConstraintValidator.class ) ) {
				//we found our baby
				return type;
			}
			else {
				resolveTypes(resolvedTypes, rawType.getGenericSuperclass() );
				for (Type genericInterface : rawType.getGenericInterfaces() ) {
					resolveTypes(resolvedTypes, genericInterface );
				}
			}
		}
		//else we don't care I think
		return null;
	}
}
