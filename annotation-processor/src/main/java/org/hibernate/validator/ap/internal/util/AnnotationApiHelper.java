/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.util;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.Types;

/**
 * A helper class providing some useful methods to work with types
 * from the JSR-269-API.
 *
 * @author Gunnar Morling
 */
public class AnnotationApiHelper {

	private final Elements elementUtils;

	private final Types typeUtils;

	private final Map<Class<?>, TypeMirror> primitiveMirrors;

	public AnnotationApiHelper(Elements elementUtils, Types typeUtils) {

		this.elementUtils = elementUtils;
		this.typeUtils = typeUtils;

		Map<Class<?>, TypeMirror> tempPrimitiveMirrors = CollectionHelper.newHashMap();

		tempPrimitiveMirrors.put( Boolean.TYPE, typeUtils.getPrimitiveType( TypeKind.BOOLEAN ) );
		tempPrimitiveMirrors.put( Character.TYPE, typeUtils.getPrimitiveType( TypeKind.CHAR ) );
		tempPrimitiveMirrors.put( Byte.TYPE, typeUtils.getPrimitiveType( TypeKind.BYTE ) );
		tempPrimitiveMirrors.put( Short.TYPE, typeUtils.getPrimitiveType( TypeKind.SHORT ) );
		tempPrimitiveMirrors.put( Integer.TYPE, typeUtils.getPrimitiveType( TypeKind.INT ) );
		tempPrimitiveMirrors.put( Long.TYPE, typeUtils.getPrimitiveType( TypeKind.LONG ) );
		tempPrimitiveMirrors.put( Float.TYPE, typeUtils.getPrimitiveType( TypeKind.FLOAT ) );
		tempPrimitiveMirrors.put( Double.TYPE, typeUtils.getPrimitiveType( TypeKind.DOUBLE ) );

		primitiveMirrors = Collections.unmodifiableMap( tempPrimitiveMirrors );
	}

	/**
	 * Returns a list containing those annotation mirrors from the input list,
	 * which are of type {@code annotationType}. The input collection
	 * remains untouched.
	 *
	 * @param annotationMirrors A list of annotation mirrors.
	 * @param annotationType The type to be compared against.
	 *
	 * @return A list with those annotation mirrors from the input list, which
	 *         are of type {@code annotationType}. May be empty but never
	 *         null.
	 */
	public List<AnnotationMirror> filterByType(List<? extends AnnotationMirror> annotationMirrors, TypeMirror annotationType) {

		List<AnnotationMirror> theValue = CollectionHelper.newArrayList();

		if ( annotationMirrors == null || annotationType == null ) {
			return theValue;
		}

		for ( AnnotationMirror oneAnnotationMirror : annotationMirrors ) {

			if ( typeUtils.isSameType( oneAnnotationMirror.getAnnotationType(), annotationType ) ) {
				theValue.add( oneAnnotationMirror );
			}
		}

		return theValue;
	}

	/**
	 * Returns that mirror from the given list of annotation mirrors that
	 * represents the annotation type specified by the given class.
	 *
	 * @param annotationMirrors A list of annotation mirrors.
	 * @param annotationClazz The class of the annotation of interest.
	 *
	 * @return The mirror from the given list that represents the specified
	 *         annotation or null, if the given list doesn't contain such a
	 *         mirror.
	 */
	public AnnotationMirror getMirror(List<? extends AnnotationMirror> annotationMirrors, Class<? extends Annotation> annotationClazz) {

		return getMirror( annotationMirrors, annotationClazz.getCanonicalName() );
	}

	/**
	 * Returns that mirror from the given list of annotation mirrors that
	 * represents the annotation type specified by the given class.
	 *
	 * @param annotationMirrors A list of annotation mirrors.
	 * @param annotationTypeName The FQN of the annotation of interest.
	 *
	 * @return The mirror from the given list that represents the specified
	 *         annotation or null, if the given list doesn't contain such a
	 *         mirror.
	 */
	public AnnotationMirror getMirror(List<? extends AnnotationMirror> annotationMirrors, String annotationTypeName) {

		if ( annotationMirrors == null || annotationTypeName == null ) {
			return null;
		}

		TypeElement typeElement = elementUtils.getTypeElement( annotationTypeName );

		if ( typeElement == null ) {
			return null;
		}

		for ( AnnotationMirror oneAnnotationMirror : annotationMirrors ) {

			if ( typeUtils.isSameType(
					oneAnnotationMirror.getAnnotationType(),
					typeElement.asType()
			) ) {
				return oneAnnotationMirror;
			}
		}

		return null;
	}

	/**
	 * Returns a TypeMirror for the given class.
	 *
	 * @param clazz The class of interest.
	 *
	 * @return A TypeMirror for the given class.
	 */
	public TypeMirror getMirrorForType(Class<?> clazz) {

		if ( clazz.isArray() ) {
			return typeUtils.getArrayType( getMirrorForNonArrayType( clazz.getComponentType() ) );
		}
		else {
			return getMirrorForNonArrayType( clazz );
		}
	}

	private TypeMirror getMirrorForNonArrayType(Class<?> clazz) {

		TypeMirror theValue = null;

		if ( clazz.isPrimitive() ) {
			theValue = primitiveMirrors.get( clazz );
		}
		else {
			theValue = getDeclaredTypeByName( clazz.getCanonicalName() );
		}

		if ( theValue != null ) {
			return theValue;
		}
		else {
			throw new AssertionError( "Couldn't find a type mirror for class " + clazz );
		}
	}

	/**
	 * Returns the {@link DeclaredType} for the given class name.
	 *
	 * @param className A fully qualified class name, e.g. "java.lang.String".
	 *
	 * @return A {@link DeclaredType} representing the type with the given name,
	 *         or null, if no such type exists.
	 */
	public DeclaredType getDeclaredTypeByName(String className) {

		TypeElement typeElement = elementUtils.getTypeElement( className );

		return typeElement != null ? typeUtils.getDeclaredType( typeElement ) : null;
	}

	/**
	 * Returns the annotation value of the given annotation mirror with the
	 * given name.
	 *
	 * @param annotationMirror An annotation mirror.
	 * @param name The name of the annotation value of interest.
	 *
	 * @return The annotation value with the given name or null, if one of the
	 *         input values is null or if no value with the given name exists
	 *         within the given annotation mirror.
	 */
	public AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String name) {

		if ( annotationMirror == null || name == null ) {
			return null;
		}

		Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotationMirror.getElementValues();

		for ( Entry<? extends ExecutableElement, ? extends AnnotationValue> oneElementValue : elementValues.entrySet() ) {

			if ( oneElementValue.getKey().getSimpleName().contentEquals( name ) ) {

				return oneElementValue.getValue();
			}
		}

		return null;
	}

	/**
	 * Returns the annotation value of the given annotation mirror with the
	 * given name or its default value if it was not specified.
	 *
	 * @param annotationMirror An annotation mirror.
	 * @param name The name of the annotation value of interest.
	 *
	 * @return The annotation value with the given name or it's default value or null, if one of the
	 *         input values is null or if no value with the given name exists
	 *         within the given annotation mirror.
	 */
	public AnnotationValue getAnnotationValueOrDefault(AnnotationMirror annotationMirror, String name) {
		if ( annotationMirror == null || name == null ) {
			return null;
		}

		Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = elementUtils.getElementValuesWithDefaults( annotationMirror );

		for ( Entry<? extends ExecutableElement, ? extends AnnotationValue> oneElementValue : elementValues.entrySet() ) {
			if ( oneElementValue.getKey().getSimpleName().contentEquals( name ) ) {
				return oneElementValue.getValue();
			}
		}

		return null;
	}

	/**
	 * Returns the given annotation mirror's array-typed annotation value with
	 * the given name.
	 *
	 * @param annotationMirror An annotation mirror.
	 * @param name The name of the annotation value of interest.
	 *
	 * @return The annotation value with the given name or an empty list, if no
	 *         such value exists within the given annotation mirror or such a
	 *         value exists but is not an array-typed one.
	 */
	public List<? extends AnnotationValue> getAnnotationArrayValue(AnnotationMirror annotationMirror, String name) {

		AnnotationValue annotationValue = getAnnotationValue( annotationMirror, name );

		if ( annotationValue == null ) {
			return Collections.<AnnotationValue>emptyList();
		}

		List<? extends AnnotationValue> theValue = annotationValue.accept(
				new SimpleAnnotationValueVisitor6<List<? extends AnnotationValue>, Void>() {

					@Override
					public List<? extends AnnotationValue> visitArray(List<? extends AnnotationValue> values, Void p) {
						return values;
					}

				}, null
		);

		return theValue != null ? theValue : Collections
				.<AnnotationValue>emptyList();
	}

	/**
	 * <p>
	 * Returns a set containing the "lowest" type per hierarchy contained in the
	 * input set. The following examples shall demonstrate the behavior.
	 * </p>
	 * <ul>
	 * <li>
	 * Input: {@code String}; Output: {@code String}</li>
	 * <li>
	 * Input: {@code Object}, {@code String}; Output:
	 * {@code String}</li>
	 * <li>
	 * Input: {@code Object}, {@code Collection}, {@code List};
	 * Output: {@code List}</li>
	 * <li>
	 * Input: {@code Collection}, {@code Set}, {@code List};
	 * Output: {@code List}, {@code Set}</li>
	 * </ul>
	 *
	 * @param types A set of type mirrors.
	 *
	 * @return A set with the lowest types per hierarchy or null, if the input
	 *         set was null.
	 */
	public Set<TypeMirror> keepLowestTypePerHierarchy(Set<TypeMirror> types) {

		if ( types == null ) {
			return null;
		}

		Set<TypeMirror> theValue = CollectionHelper.newHashSet();

		for ( TypeMirror typeMirror1 : types ) {
			boolean foundSubType = false;
			for ( TypeMirror typeMirror2 : types ) {
				if ( !typeUtils.isSameType( typeMirror2, typeMirror1 ) && typeUtils.isAssignable(
						typeMirror2, typeMirror1
				) ) {
					foundSubType = true;
					continue;
				}
			}
			if ( !foundSubType ) {
				theValue.add( typeMirror1 );
			}
		}

		return theValue;
	}

	/**
	 * Test if the given {@link TypeMirror} represents an interface or not.
	 */
	public boolean isInterface(TypeMirror typeMirror) {
		return TypeKind.DECLARED.equals( typeMirror.getKind() ) && ( (DeclaredType) typeMirror ).asElement().getKind().isInterface();
	}

	/**
	 * Test if the given {@link TypeMirror} represents a class or not.
	 */
	public boolean isClass(TypeMirror typeMirror) {
		return TypeKind.DECLARED.equals( typeMirror.getKind() ) && ( (DeclaredType) typeMirror ).asElement().getKind().isClass();
	}

	/**
	 * Checks the annotation's payload for unwrapping option ({@code javax.validation.valueextraction.Unwrapping.Unwrap},
	 * {@code javax.validation.valueextraction.Unwrapping.Skip}) of constraint.
	 *
	 * @param annotationMirror constraint annotation mirror under check
	 * @return unwrapping option, if one is present in the annotation payload, {@link UnwrapMode#NONE} otherwise
	 */
	public UnwrapMode determineUnwrapMode(AnnotationMirror annotationMirror) {
		return getAnnotationArrayValue( annotationMirror, "payload" ).stream()
				.map( AnnotationValue::getValue )
				.map( type -> (TypeMirror) type )
				.map( typeUtils::asElement )
				.map( elem -> ( (TypeElement) elem ).getQualifiedName() )
				.filter( name -> name.toString().startsWith( "javax.validation.valueextraction.Unwrapping." ) )
				.map( UnwrapMode::of )
				.findAny().orElse( UnwrapMode.NONE );
	}

	public enum UnwrapMode {
		UNWRAP, SKIP, NONE;

		public static UnwrapMode of(Name qualifiedName) {
			if ( "javax.validation.valueextraction.Unwrapping.Unwrap".equals( qualifiedName.toString() ) ) {
				return UNWRAP;
			}
			else if ( "javax.validation.valueextraction.Unwrapping.Skip".equals( qualifiedName.toString() ) ) {
				return SKIP;
			}
			else {
				return NONE;
			}
		}
	}

}
