/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.metadata.provider;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ParameterNameProvider;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.TypeUseHelper;
import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Extends the base {@code AnnotationMetaDataProvider} with the following:
 *
 * <ul>
 *     <li>Discover and register constraints defined on formal parameters and actual type arguments.</li>
 *     <li>Discover and register constraints defined on type arguments for fields, parameters, and return
 *     values parameterized types.</li>
 * </ul>
 *
 * @author Khalid Alqinyah
 */
public class TypeUseAnnotationMetaDataProvider extends AnnotationMetaDataProvider {

	/**
	 * Keeps track of discovered annotations on formal parameters and actual type arguments for the current hierarchy.
	 * Each thread will have its own map, so that discovered annotations of different hierarchies don't mix with each
	 * other.
	 */
	private static final ThreadLocal<Map<TypeVariable<?>, Set<Annotation>>> typeUseConstraintsMap = new ThreadLocal<Map<TypeVariable<?>, Set<Annotation>>>() {
		@Override
		protected Map<TypeVariable<?>, Set<Annotation>> initialValue() {
			return newHashMap();
		}
	};

	public TypeUseAnnotationMetaDataProvider(ConstraintHelper constraintHelper,
											 ParameterNameProvider parameterNameProvider,
											 AnnotationProcessingOptions annotationProcessingOptions) {
		super( constraintHelper, parameterNameProvider, annotationProcessingOptions );
	}

	@Override
	public <T> List<BeanConfiguration<? super T>> getBeanConfigurationForHierarchy(Class<T> beanClass) {

		// Discover and map type parameters and actual type arguments constraints (e.g. class Student<@NotNull T>)
		for ( Class<? super T> hierarchyClass : ClassHierarchyHelper.getHierarchy( beanClass ) ) {
			findTypeUseConstraints( hierarchyClass );
		}

		// Get the constrained elements
		List<BeanConfiguration<? super T>> configurations = super.getBeanConfigurationForHierarchy( beanClass );

		return configurations;
	}

	/**
	 * Discovers and maps annotations constraints on formal type parameters and actual type arguments defined on the
	 * class, super class, and interfaces.
	 *
	 * @param cls the class to examine
	 */
	private void findTypeUseConstraints(Class<?> cls) {

		Map<TypeVariable<?>, Set<Annotation>> typeUseConstraintsMap = newHashMap();

		// 1. Formal types (e.g. class Pair<@NotNull T, @NotBlank V extends String>)
		for ( TypeVariable<?> typeVariable : cls.getTypeParameters() ) {
			for ( Annotation annotation : typeVariable.getAnnotations() ) {
				Set<Annotation> constraintAnnotations = findTypeUseConstraintAnnotations( annotation );
				addTypeUseConstraintsAnnotations( typeVariable, constraintAnnotations );
			}
		}

		// 2. Superclass type arguments (e.g. class BarExt extends Bar<@NotNull Integer, @NotBlank String>)
		if ( cls.getAnnotatedSuperclass() != null ) {
			Map<TypeVariable<?>, AnnotatedType> superFormalToActualMap = TypeUseHelper.getFormalToActualMap(
					cls.getAnnotatedSuperclass(),
					cls.getSuperclass()
			);
			addConstraintsToContext( superFormalToActualMap );
		}

		// 3. Interfaces type arguments (e.g. class BarImpl implements Bar<@NotNull Integer, @NotBlank String>)
		if ( cls.getAnnotatedInterfaces().length > 0 ) {
			Class<?>[] interfaces = cls.getInterfaces();
			AnnotatedType[] annotatedInterfaces = cls.getAnnotatedInterfaces();

			for ( int i = 0; i < annotatedInterfaces.length; i++ ) {
				Map<TypeVariable<?>, AnnotatedType> interfaceFormalToActualMap = TypeUseHelper.getFormalToActualMap(
						annotatedInterfaces[i],
						interfaces[i]
				);
				addConstraintsToContext( interfaceFormalToActualMap );
			}
		}
	}

	/**
	 * Find annotations defined on actual type arguments and map them to formal parameters.
	 *
	 * @param formalToActualMap formal parameters to actual type arguments
	 */
	private void addConstraintsToContext(Map<TypeVariable<?>, AnnotatedType> formalToActualMap) {
		for ( Map.Entry<TypeVariable<?>, AnnotatedType> entry : formalToActualMap.entrySet() ) {
			for ( Annotation annotation : entry.getValue().getAnnotations() ) {
				Set<Annotation> annotations = findTypeUseConstraintAnnotations( annotation );
				addTypeUseConstraintsAnnotations( entry.getKey(), annotations );
			}
		}
	}

	/**
	 * Adds discovered annotations to formal parameters mapping to {@code typeUseConstraintsMap}
	 */
	private void addTypeUseConstraintsAnnotations(TypeVariable<?> typeVariable, Set<Annotation> annotations) {
		Map<TypeVariable<?>, Set<Annotation>> map = typeUseConstraintsMap.get();

		Set<Annotation> existingAnnotations = map.get( typeVariable );
		if ( existingAnnotations != null ) {
			existingAnnotations.addAll( annotations );
		}
		else {
			map.put( typeVariable, annotations );
		}
	}

	/**
	 * Gets discovered annotations for type.
	 */
	private Set<Annotation> getTypeUseConstraintsAnnotations(Type type) {
		Map<TypeVariable<?>, Set<Annotation>> map = typeUseConstraintsMap.get();

		Set<Annotation> annotations = Collections.<Annotation>emptySet();

		if ( type != null && TypeHelper.isAssignable( TypeVariable.class, type.getClass() ) ) {
			TypeVariable<?> typeVariable = (TypeVariable<?>) type;
			annotations = map.get( typeVariable );
		}

		return ( annotations == null ? Collections.<Annotation>emptySet() : annotations );
	}

	/**
	 * Returns a set of discovered annotations constraints.
	 */
	private <A extends Annotation> Set<Annotation> findTypeUseConstraintAnnotations(A annotation) {
		Set<Annotation> constraints = newHashSet();
		Class<? extends Annotation> annotationType = annotation.annotationType();
		if ( constraintHelper.isConstraintAnnotation( annotationType ) ) {
			constraints.add( annotation );
		}
		else if ( constraintHelper.isMultiValueConstraint( annotationType ) ) {
			constraints.addAll( constraintHelper.getConstraintsFromMultiValueConstraint( annotation ) );
		}

		return constraints;
	}

	@Override
	protected List<ConstraintDescriptorImpl<?>> findConstraints(Member member, ElementType type) {
		List<ConstraintDescriptorImpl<?>> constraintDescriptors = super.findConstraints( member, type );

		// Add type use constraints
		Type genericType = ReflectionHelper.genericTypeOf( member );
		Set<Annotation> typeUseAnnotations = getTypeUseConstraintsAnnotations( genericType );
		for ( Annotation annotation : typeUseAnnotations ) {
			constraintDescriptors.addAll( findConstraintAnnotations( member, annotation, type ) );
		}
		return constraintDescriptors;
	}

	@Override
	protected Set<MetaConstraint<?>> convertToMetaConstraints(List<ConstraintDescriptorImpl<?>> constraintDescriptors, Field field) {
		Set<MetaConstraint<?>> metaConstraints = super.convertToMetaConstraints( constraintDescriptors, field );
		// Add type argument constraints for a field
		metaConstraints.addAll( findConstrainedTypeArguments( field.getAnnotatedType(), field ) );
		return metaConstraints;
	}

	@Override
	protected Set<MetaConstraint<?>> convertToMetaConstraints(List<ConstraintDescriptorImpl<?>> constraintsDescriptors, ExecutableElement executable) {
		Set<MetaConstraint<?>> metaConstraints = super.convertToMetaConstraints( constraintsDescriptors, executable );

		if ( metaConstraints.isEmpty() ) {
			metaConstraints = newHashSet();
		}
		// Add type argument constraints for an executable
		Member member = executable.getMember();
		AnnotatedType annotatedType = ( (Executable) member ).getAnnotatedReturnType();
		metaConstraints.addAll( findConstrainedTypeArguments( annotatedType, member ) );
		return metaConstraints;
	}

	@Override
	protected Set<MetaConstraint<?>> convertToMetaConstraints(List<ConstraintDescriptorImpl<?>> constraintDescriptors, ExecutableElement executable, int i) {
		// Add type use constraints
		Type genericType = ReflectionHelper.genericTypeOf( executable, i );
		Set<Annotation> annotations = getTypeUseConstraintsAnnotations( genericType );
		for ( Annotation annotation : annotations ) {
			constraintDescriptors.addAll(
					findConstraintAnnotations(
							executable.getMember(), annotation, ElementType.PARAMETER
					)
			);
		}
		Set<MetaConstraint<?>> metaConstraints = super.convertToMetaConstraints( constraintDescriptors, executable, i );

		// Add type argument constraints for a parameter
		try {
			AnnotatedType annotatedType = ( (Executable) executable.getMember() ).getParameters()[i].getAnnotatedType();
			metaConstraints.addAll( findConstrainedTypeArguments( annotatedType, executable.getMember() ) );
		}
		catch ( ArrayIndexOutOfBoundsException ex ) {
			// Constructors for inner non-static classes have a default synthetic parameter for the outer class,
			// it's counted, but cannot be accessed, ignore it
		}

		return metaConstraints;
	}

	/**
	 * Creates meta constraints based on the constraints annotations discovered on the {@code AnnotatedType}.
	 */
	private Set<MetaConstraint<?>> findConstrainedTypeArguments(AnnotatedType annotatedType, Member member) {
		List<AnnotatedType> typeArguments = TypeUseHelper.getAnnotatedActualTypeArguments( annotatedType );
		Set<MetaConstraint<?>> metaConstraints = newHashSet();

		for ( AnnotatedType typeArgument : typeArguments ) {
			List<ConstraintDescriptorImpl<?>> metaData = findTypeArgumentConstraints( member, typeArgument );

			Set<MetaConstraint<?>> constraints = convertToTypeArgumentMetaConstraints(
					metaData,
					member,
					typeArgument.getType()
			);

			metaConstraints.addAll( constraints );
		}

		return metaConstraints;
	}

	/**
	 * Finds annotation constraints defined on the type {@code AnnotatedType}.
	 */
	public List<ConstraintDescriptorImpl<?>> findTypeArgumentConstraints(Member member, AnnotatedType typeArgument) {
		List<ConstraintDescriptorImpl<?>> metaData = newArrayList();

		// Defined directly on the type argument
		Annotation[] direct = typeArgument.getAnnotations();

		// Matched from the type use context
		Set<Annotation> annotations = getTypeUseConstraintsAnnotations( typeArgument.getType() );

		// Combine
		Set<Annotation> combined = newHashSet( annotations );
		Collections.addAll( combined, direct );

		for ( Annotation annotation : combined ) {
			metaData.addAll( findConstraintAnnotations( member, annotation, ElementType.TYPE_USE ) );
		}

		return metaData;
	}

	/**
	 * Creates meta constraints for type arguments constraints.
	 */
	private Set<MetaConstraint<?>> convertToTypeArgumentMetaConstraints(List<ConstraintDescriptorImpl<?>> constraintDescriptors, Member member, Type type) {
		Set<MetaConstraint<?>> constraints = newHashSet();
		for ( ConstraintDescriptorImpl<?> constraintDescription : constraintDescriptors ) {
			MetaConstraint<?> metaConstraint = createTypeArgumentMetaConstraint( member, constraintDescription, type );
			constraints.add( metaConstraint );
		}
		return constraints;
	}

	/**
	 * Creates a {@code MetaConstraint} for a type argument constraint.
	 */
	private <A extends Annotation> MetaConstraint<?> createTypeArgumentMetaConstraint(Member member, ConstraintDescriptorImpl<A> descriptor, Type type) {
		return new MetaConstraint<A>( descriptor, ConstraintLocation.forTypeArgument( member, type ) );
	}
}
