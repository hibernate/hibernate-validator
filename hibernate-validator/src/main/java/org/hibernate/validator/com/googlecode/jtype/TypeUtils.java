package org.hibernate.validator.com.googlecode.jtype;

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Provides utility methods for working with types.
 * 
 * @author Mark Hobson
 * @version $Id: TypeUtils.java 94 2010-11-23 16:09:23Z markhobson $
 */
public final class TypeUtils
{
	// constants --------------------------------------------------------------
	
	private static final Map<Class<?>, Set<Class<?>>> SUBTYPES_BY_PRIMITIVE;
	
	static
	{
		Map<Class<?>, Set<Class<?>>> subtypesByPrimitive = new HashMap<Class<?>, Set<Class<?>>>();
		
		putPrimitiveSubtypes(subtypesByPrimitive, Void.TYPE);
		putPrimitiveSubtypes(subtypesByPrimitive, Boolean.TYPE);
		putPrimitiveSubtypes(subtypesByPrimitive, Byte.TYPE);
		putPrimitiveSubtypes(subtypesByPrimitive, Character.TYPE);
		putPrimitiveSubtypes(subtypesByPrimitive, Short.TYPE, Byte.TYPE);
		putPrimitiveSubtypes(subtypesByPrimitive, Integer.TYPE, Character.TYPE, Short.TYPE);
		putPrimitiveSubtypes(subtypesByPrimitive, Long.TYPE, Integer.TYPE);
		putPrimitiveSubtypes(subtypesByPrimitive, Float.TYPE, Long.TYPE);
		putPrimitiveSubtypes(subtypesByPrimitive, Double.TYPE, Float.TYPE);

		SUBTYPES_BY_PRIMITIVE = Collections.unmodifiableMap(subtypesByPrimitive);
	}
	
	// constructors -----------------------------------------------------------
	
	private TypeUtils()
	{
		throw new AssertionError();
	}
	
	// public methods ---------------------------------------------------------
	
	public static void accept(Type type, TypeVisitor visitor)
	{
		Utils.checkNotNull(type, "type");
		Utils.checkNotNull(visitor, "visitor");
		
		if (type instanceof Class<?>)
		{
			visitor.visit((Class<?>) type);
		}
		else if (type instanceof TypeVariable<?>)
		{
			TypeVariable<?> typeVariable = (TypeVariable<?>) type;
			
			if (visitor.beginVisit(typeVariable))
			{
				// visit bounds
			
				Type[] bounds = typeVariable.getBounds();
				
				for (int i = 0; i < bounds.length; i++)
				{
					visitor.visitTypeVariableBound(bounds[i], i);
				}
			}
			
			visitor.endVisit(typeVariable);
		}
		else if (type instanceof GenericArrayType)
		{
			visitor.visit((GenericArrayType) type);
		}
		else if (type instanceof ParameterizedType)
		{
			ParameterizedType parameterizedType = (ParameterizedType) type;
			
			if (visitor.beginVisit(parameterizedType))
			{
				// visit actual type arguments
				
				Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
				
				for (int i = 0; i < actualTypeArguments.length; i++)
				{
					visitor.visitActualTypeArgument(actualTypeArguments[i], i);
				}
			}
			
			visitor.endVisit(parameterizedType);
		}
		else if (type instanceof WildcardType)
		{
			WildcardType wildcardType = (WildcardType) type;

			if (visitor.beginVisit(wildcardType))
			{
				// visit upper bounds
				
				Type[] upperBounds = wildcardType.getUpperBounds();
				
				for (int i = 0; i < upperBounds.length; i++)
				{
					visitor.visitUpperBound(upperBounds[i], i);
				}
				
				// visit lower bounds
				
				Type[] lowerBounds = wildcardType.getLowerBounds();
				
				for (int i = 0; i < lowerBounds.length; i++)
				{
					visitor.visitLowerBound(lowerBounds[i], i);
				}
			}
				
			visitor.endVisit(wildcardType);
		}
		else
		{
			throw new IllegalArgumentException("Unknown type: " + type);
		}
	}
	
	public static boolean isAssignable(Type supertype, Type type)
	{
		Utils.checkNotNull(supertype, "supertype");
		Utils.checkNotNull(type, "type");
		
		if (supertype.equals(type))
		{
			return true;
		}
		
		if (supertype instanceof Class<?>)
		{
			if (type instanceof Class<?>)
			{
				return isClassAssignable((Class<?>) supertype, (Class<?>) type);
			}
			
			if (type instanceof ParameterizedType)
			{
				return isAssignable(supertype, ((ParameterizedType) type).getRawType());
			}
			
			if (type instanceof TypeVariable<?>)
			{
				return isTypeVariableAssignable(supertype, (TypeVariable<?>) type);
			}
			
			if (type instanceof GenericArrayType)
			{
				if (((Class<?>) supertype).isArray())
				{
					return isAssignable(getComponentType(supertype), getComponentType(type));
				}
				
				return isArraySupertype((Class<?>) supertype);
			}
			
			if (type instanceof WildcardType)
			{
				return isClassAssignableToWildcardType((Class<?>) supertype, (WildcardType) type);
			}
			
			return false;
		}
		
		if (supertype instanceof ParameterizedType)
		{
			if (type instanceof Class<?>)
			{
				return isSuperAssignable(supertype, type);
			}
			
			if (type instanceof ParameterizedType)
			{
				return isParameterizedTypeAssignable((ParameterizedType) supertype, (ParameterizedType) type);
			}
			
			return false;
		}
		
		if (type instanceof TypeVariable<?>)
		{
			return isTypeVariableAssignable(supertype, (TypeVariable<?>) type);
		}
		
		if (supertype instanceof GenericArrayType)
		{
			if (isArray(type))
			{
				return isAssignable(getComponentType(supertype), getComponentType(type));
			}
			
			return false;
		}
		
		if (supertype instanceof WildcardType)
		{
			return isWildcardTypeAssignable((WildcardType) supertype, type);
		}
		
		return false;
	}
	
	public static boolean isInstance(Type type, Object object)
	{
		return getErasedReferenceType(type).isInstance(object);
	}
	
	/**
	 * Gets the erased type of the specified type.
	 * 
	 * @param type
	 *            the type to perform erasure on
	 * @return the erased type, never a parameterized type nor a type variable
	 * @see <a href="http://java.sun.com/docs/books/jls/third_edition/html/typesValues.html#4.6">4.6 Type Erasure</a>
	 */
	public static Type getErasedType(Type type)
	{
		// the erasure of a parameterized type G<T1, ... ,Tn> is |G|
		if (type instanceof ParameterizedType)
		{
			Type rawType = ((ParameterizedType) type).getRawType();
			
			return getErasedType(rawType);
		}
		
		// TODO: the erasure of a nested type T.C is |T|.C
		
		// the erasure of an array type T[] is |T|[]
		if (isArray(type))
		{
			Type componentType = getComponentType(type);
			Type erasedComponentType = getErasedType(componentType);
			
			return getArrayType(erasedComponentType);
		}
		
		// the erasure of a type variable is the erasure of its leftmost bound 
		if (type instanceof TypeVariable<?>)
		{
			Type[] bounds = ((TypeVariable<?>) type).getBounds();
			
			return getErasedType(bounds[0]);
		}
		
		// the erasure of every other type is the type itself
		return type;
	}
	
	public static Class<?> getErasedReferenceType(Type type)
	{
		Utils.checkTrue(isReferenceType(type), "type is not a reference type: ", type);
		
		return (Class<?>) getErasedType(type);
	}

	/**
	 * @deprecated Use {@link #getErasedReferenceType(Type)} instead.
	 */
	@Deprecated
	public static Class<?> getRawType(Type type)
	{
		return getErasedReferenceType(type);
	}
	
	public static boolean isArray(Type type)
	{
		return (type instanceof Class<?> && ((Class<?>) type).isArray())
			|| (type instanceof GenericArrayType);
	}
	
	public static boolean isPrimitive(Type type)
	{
		return (type instanceof Class<?>) && ((Class<?>) type).isPrimitive();
	}
	
	public static Type getComponentType(Type type)
	{
		if (type instanceof Class<?>)
		{
			Class<?> klass = (Class<?>) type;
			
			return klass.isArray() ? klass.getComponentType() : null;
		}
		
		if (type instanceof GenericArrayType)
		{
			return ((GenericArrayType) type).getGenericComponentType();
		}
		
		return null;
	}
	
	public static Type getArrayType(Type componentType)
	{
		Utils.checkNotNull(componentType, "componentType");
		
		if (componentType instanceof Class<?>)
		{
			return ClassUtils.getArrayType((Class<?>) componentType);
		}
		
		return Types.genericArrayType(componentType);
	}
	
	// TODO: perform isSubtype(type, Types.parameterizedType(rawType, Types.unboundedWildcardType()))
	public static boolean isSimpleParameterizedType(Type type, Class<?> rawType)
	{
		Utils.checkNotNull(type, "type");
		Utils.checkNotNull(rawType, "rawType");
		
		if (!(type instanceof ParameterizedType))
		{
			return false;
		}
		
		ParameterizedType paramType = (ParameterizedType) type;
		
		Type paramRawType = paramType.getRawType();
		
		if (!(paramRawType instanceof Class<?>))
		{
			return false;
		}
		
		Class<?> paramRawClass = (Class<?>) paramRawType;
		
		if (!rawType.isAssignableFrom(paramRawClass))
		{
			return false;
		}
		
		Type[] typeArgs = paramType.getActualTypeArguments();
		
		return (typeArgs.length == 1);
	}
	
	// TODO: remove?
	public static Type getActualTypeArgument(Type type)
	{
		Utils.checkNotNull(type, "type");
		
		ParameterizedType paramType = (ParameterizedType) type;
		
		Type[] typeArgs = paramType.getActualTypeArguments();
		
		Utils.checkTrue(typeArgs.length == 1, "type must be a ParameterizedType with one actual type argument: ", type);
		
		return typeArgs[0];
	}
	
	public static Type getResolvedSuperclass(Type type)
	{
		Utils.checkNotNull(type, "type");
		
		Class<?> rawType = getErasedReferenceType(type);
		Type supertype = rawType.getGenericSuperclass();
		
		if (supertype == null)
		{
			return null;
		}

		return resolveTypeVariables(supertype, type);
	}
	
	public static Type[] getResolvedInterfaces(Type type)
	{
		Utils.checkNotNull(type, "type");
		
		Class<?> rawType = getErasedReferenceType(type);
		Type[] interfaces = rawType.getGenericInterfaces();
		Type[] resolvedInterfaces = new Type[interfaces.length];
		
		for (int i = 0; i < interfaces.length; i++)
		{
			resolvedInterfaces[i] = resolveTypeVariables(interfaces[i], type);
		}
		
		return resolvedInterfaces;
	}
	
	public static String toString(Type type)
	{
		return toString(type, ClassSerializers.QUALIFIED);
	}
	
	public static String toString(Type type, ClassSerializer serializer)
	{
		if (type == null)
		{
			return String.valueOf(type);
		}
		
		SerializingTypeVisitor visitor = new SerializingTypeVisitor(serializer);
		
		accept(type, visitor);
		
		return visitor.toString();
	}

	public static String toUnqualifiedString(Type type)
	{
		return toString(type, ClassSerializers.UNQUALIFIED);
	}
	
	public static String toSimpleString(Type type)
	{
		return toString(type, ClassSerializers.SIMPLE);
	}
	
	// private methods --------------------------------------------------------
	
	private static void putPrimitiveSubtypes(Map<Class<?>, Set<Class<?>>> subtypesByPrimitive, Class<?> primitiveType,
		Class<?>... directSubtypes)
	{
		Set<Class<?>> subtypes = new HashSet<Class<?>>();
		
		for (Class<?> directSubtype : directSubtypes)
		{
			subtypes.add(directSubtype);
			subtypes.addAll(subtypesByPrimitive.get(directSubtype));
		}
		
		subtypesByPrimitive.put(primitiveType, Collections.unmodifiableSet(subtypes));
	}
	
	private static boolean isClassAssignable(Class<?> supertype, Class<?> type)
	{
		// Class.isAssignableFrom does not perform primitive widening
		if (supertype.isPrimitive() && type.isPrimitive())
		{
			return SUBTYPES_BY_PRIMITIVE.get(supertype).contains(type);
		}
		
		return supertype.isAssignableFrom(type);
	}
	
	private static boolean isClassAssignableToWildcardType(Class<?> supertype, WildcardType type)
	{
		for (Type upperBound : type.getUpperBounds())
		{
			if (!isAssignable(supertype, upperBound))
			{
				return false;
			}
		}
		
		return true;
	}
	
	private static boolean isParameterizedTypeAssignable(ParameterizedType supertype, ParameterizedType type)
	{
		Type rawSupertype = supertype.getRawType();
		Type rawType = type.getRawType();

		if (!rawSupertype.equals(rawType))
		{
			// short circuit when class raw types are unassignable
			if (rawSupertype instanceof Class<?> && rawType instanceof Class<?>
				&& !(((Class<?>) rawSupertype).isAssignableFrom((Class<?>) rawType)))
			{
				return false;
			}
		
			return isSuperAssignable(supertype, type);
		}
		
		Type[] supertypeArgs = supertype.getActualTypeArguments();
		Type[] typeArgs = type.getActualTypeArguments();
		
		if (supertypeArgs.length != typeArgs.length)
		{
			return false;
		}
		
		for (int i = 0; i < supertypeArgs.length; i++)
		{
			Type supertypeArg = supertypeArgs[i];
			Type typeArg = typeArgs[i];
			
			if (supertypeArg instanceof WildcardType)
			{
				if (!isWildcardTypeAssignable((WildcardType) supertypeArg, typeArg))
				{
					return false;
				}
			}
			else if (!supertypeArg.equals(typeArg))
			{
				return false;
			}
		}
		
		return true;
	}
	
	private static boolean isTypeVariableAssignable(Type supertype, TypeVariable<?> type)
	{
		for (Type bound : type.getBounds())
		{
			if (isAssignable(supertype, bound))
			{
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean isWildcardTypeAssignable(WildcardType supertype, Type type)
	{
		for (Type upperBound : supertype.getUpperBounds())
		{
			if (!isAssignable(upperBound, type))
			{
				return false;
			}
		}
		
		for (Type lowerBound : supertype.getLowerBounds())
		{
			if (!isAssignable(type, lowerBound))
			{
				return false;
			}
		}
		
		return true;
	}
	
	private static boolean isSuperAssignable(Type supertype, Type type)
	{
		Type superclass = getResolvedSuperclass(type);
		
		if (superclass != null && isAssignable(supertype, superclass))
		{
			return true;
		}
		
		for (Type interphace : getResolvedInterfaces(type))
		{
			if (isAssignable(supertype, interphace))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Gets whether the specified type is a <em>reference type</em>.
	 * <p>
	 * More specifically, this method returns {@code true} if the specified type is one of the following:
	 * <ul>
	 * <li>a class type</li>
	 * <li>an interface type</li>
	 * <li>an array type</li>
	 * <li>a parameterized type</li>
	 * <li>a type variable</li>
	 * <li>the null type</li>
	 * </ul>
	 * 
	 * @param type
	 *            the type to check
	 * @return {@code true} if the specified type is a reference type
	 * @see <a href="http://java.sun.com/docs/books/jls/third_edition/html/typesValues.html#4.3">4.3 Reference Types and Values</a>
	 */
	private static boolean isReferenceType(Type type)
	{
		return type == null
			|| type instanceof Class<?>
			|| type instanceof ParameterizedType
			|| type instanceof TypeVariable<?>
			|| type instanceof GenericArrayType;
	}
	
	private static boolean isArraySupertype(Class<?> type)
	{
		return Object.class.equals(type)
			|| Cloneable.class.equals(type)
			|| Serializable.class.equals(type);
	}

	private static Type resolveTypeVariables(Type type, Type subtype)
	{
		// TODO: need to support other types in future, e.g. T[], etc.
		if (!(type instanceof ParameterizedType))
		{
			return type;
		}
		
		Map<Type, Type> actualTypeArgumentsByParameter = getActualTypeArgumentsByParameter(type, subtype);
		Class<?> rawType = getErasedReferenceType(type);
		
		return parameterizeClass(rawType, actualTypeArgumentsByParameter);
	}
	
	private static Map<Type, Type> getActualTypeArgumentsByParameter(Type... types)
	{
		// TODO: return Map<TypeVariable<Class<?>>, Type> somehow
		
		Map<Type, Type> actualTypeArgumentsByParameter = new LinkedHashMap<Type, Type>();
		
		for (Type type : types)
		{
			actualTypeArgumentsByParameter.putAll(getActualTypeArgumentsByParameterInternal(type));
		}
		
		return normalize(actualTypeArgumentsByParameter);
	}
	
	private static Map<Type, Type> getActualTypeArgumentsByParameterInternal(Type type)
	{
		// TODO: look deeply within non-parameterized types when visitors implemented
		if (!(type instanceof ParameterizedType))
		{
			return Collections.emptyMap();
		}
		
		TypeVariable<?>[] typeParameters = getErasedReferenceType(type).getTypeParameters();
		Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
		
		if (typeParameters.length != typeArguments.length)
		{
			throw new MalformedParameterizedTypeException();
		}
		
		Map<Type, Type> actualTypeArgumentsByParameter = new LinkedHashMap<Type, Type>();
		
		for (int i = 0; i < typeParameters.length; i++)
		{
			actualTypeArgumentsByParameter.put(typeParameters[i], typeArguments[i]);
		}
		
		return actualTypeArgumentsByParameter;
	}
	
	private static ParameterizedType parameterizeClass(Class<?> type, Map<Type, Type> actualTypeArgumentsByParameter)
	{
		return parameterizeClassCapture(type, actualTypeArgumentsByParameter);
	}
	
	private static <T> ParameterizedType parameterizeClassCapture(Class<T> type, Map<Type, Type> actualTypeArgumentsByParameter)
	{
		// TODO: actualTypeArgumentsByParameter should be Map<TypeVariable<Class<T>>, Type>
		
		TypeVariable<Class<T>>[] typeParameters = type.getTypeParameters();
		Type[] actualTypeArguments = new Type[typeParameters.length];
		
		for (int i = 0; i < typeParameters.length; i++)
		{
			TypeVariable<Class<T>> typeParameter = typeParameters[i];
			Type actualTypeArgument = actualTypeArgumentsByParameter.get(typeParameter);
			
			if (actualTypeArgument == null)
			{
				throw new IllegalArgumentException("Missing actual type argument for type parameter: " + typeParameter);
			}
			
			actualTypeArguments[i] = actualTypeArgument;
		}
		
		return Types.parameterizedType(getErasedReferenceType(type), actualTypeArguments);
	}
	
	private static <K, V> Map<K, V> normalize(Map<K, V> map)
	{
		// TODO: will this cause an infinite loop with recursive bounds?
		
		for (Entry<K, V> entry : map.entrySet())
		{
			K key = entry.getKey();
			V value = entry.getValue();
			
			while (map.containsKey(value))
			{
				value = map.get(value);
			}
			
			map.put(key, value);
		}
		
		return map;
	}
}
