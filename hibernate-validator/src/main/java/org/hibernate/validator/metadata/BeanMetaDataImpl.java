/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.metadata;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.validation.GroupDefinitionException;
import javax.validation.GroupSequence;
import javax.validation.Valid;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.slf4j.Logger;

import org.hibernate.validator.metadata.site.BeanConstraintSite;
import org.hibernate.validator.metadata.site.MethodParameterConstraintSite;
import org.hibernate.validator.util.LoggerFactory;
import org.hibernate.validator.util.ReflectionHelper;


/**
 * This class encapsulates all meta data needed for validation. Implementations of {@code Validator} interface can
 * instantiate an instance of this class and delegate the metadata extraction to it.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public final class BeanMetaDataImpl<T> implements BeanMetaData<T> {

	private static final Logger log = LoggerFactory.make();

	/**
	 * The root bean class for this meta data.
	 */
	private final Class<T> beanClass;

	/**
	 * The main element descriptor for <code>beanClass</code>.
	 */
	private BeanDescriptorImpl<T> beanDescriptor;

	/**
	 * Map of all direct constraints which belong to the entity {@code beanClass}. The constraints are mapped to the class
	 * (eg super class or interface) in which they are defined.
	 */
	private Map<Class<?>, List<MetaConstraint<T, ? extends Annotation>>> metaConstraints = new HashMap<Class<?>, List<MetaConstraint<T, ? extends Annotation>>>();

	private Map<Class<?>, Map<Method, MethodMetaData>> methodMetaConstraints = new HashMap<Class<?>, Map<Method,MethodMetaData>>();
	
	/**
	 * List of cascaded members.
	 */
	private List<Member> cascadedMembers = new ArrayList<Member>();

	/**
	 * Maps field and method names to their <code>ElementDescriptorImpl</code>.
	 */
	private Map<String, PropertyDescriptor> propertyDescriptors = new HashMap<String, PropertyDescriptor>();

	/**
	 * The default groups sequence for this bean class.
	 */
	private List<Class<?>> defaultGroupSequence = new ArrayList<Class<?>>();

	/**
	 * Object keeping track of all constraints.
	 */
	private final ConstraintHelper constraintHelper;

	/**
	 * A list of all property names in the class (constraint and un-constraint).
	 */
	// Used to avoid ReflectionHelper#containsMember which is slow
	private final Set<String> propertyNames = new HashSet<String>( 30 );

	public BeanMetaDataImpl(Class<T> beanClass, ConstraintHelper constraintHelper, BeanMetaDataCache beanMetaDataCache) {
		this(
				beanClass,
				constraintHelper,
				new ArrayList<Class<?>>(),
				new HashMap<Class<?>, List<MetaConstraint<T, ?>>>(),
				new ArrayList<Member>(),
				new AnnotationIgnores(),
				beanMetaDataCache
		);
	}

	public BeanMetaDataImpl(Class<T> beanClass,
							ConstraintHelper constraintHelper,
							List<Class<?>> defaultGroupSequence,
							Map<Class<?>, List<MetaConstraint<T, ?>>> constraints,
							List<Member> cascadedMembers,
							AnnotationIgnores annotationIgnores,
							BeanMetaDataCache beanMetaDataCache) {
		this.beanClass = beanClass;
		this.constraintHelper = constraintHelper;
		createMetaData( annotationIgnores, beanMetaDataCache );
		if ( !defaultGroupSequence.isEmpty() ) {
			setDefaultGroupSequence( defaultGroupSequence );
		}
		for ( Map.Entry<Class<?>, List<MetaConstraint<T, ?>>> entry : constraints.entrySet() ) {
			Class<?> clazz = entry.getKey();
			for ( MetaConstraint<T, ?> constraint : entry.getValue() ) {
				addMetaConstraint( clazz, constraint );
			}
		}
		for ( Member member : cascadedMembers ) {
			addCascadedMember( member );
		}
	}

	public Class<T> getBeanClass() {
		return beanClass;
	}

	public BeanDescriptor getBeanDescriptor() {
		return beanDescriptor;
	}

	public List<Member> getCascadedMembers() {
		return Collections.unmodifiableList( cascadedMembers );
	}

	public Map<Class<?>, List<MetaConstraint<T, ? extends Annotation>>> getMetaConstraintsAsMap() {
		return Collections.unmodifiableMap( metaConstraints );
	}

	public List<MetaConstraint<T, ? extends Annotation>> getMetaConstraintsAsList() {
		List<MetaConstraint<T, ? extends Annotation>> constraintList = new ArrayList<MetaConstraint<T, ? extends Annotation>>();
		for ( List<MetaConstraint<T, ? extends Annotation>> list : metaConstraints.values() ) {
			constraintList.addAll( list );
		}
		return Collections.unmodifiableList( constraintList );
	}
	
	public Map<Class<?>, MethodMetaData> getMetaDataForMethod(Method method) {

		Map<Class<?>, MethodMetaData> theValue = new HashMap<Class<?>, MethodMetaData>();
		
		for (Entry<Class<?>, Map<Method, MethodMetaData>> methodsOfOneClass : methodMetaConstraints.entrySet()) {
			for(Entry<Method, MethodMetaData> oneMethodEntry : methodsOfOneClass.getValue().entrySet()) {
				
				if(ReflectionHelper.haveSameSignature(method, oneMethodEntry.getKey())) {
					theValue.put(methodsOfOneClass.getKey(), oneMethodEntry.getValue());
				}
			}
		}
		
		return theValue;
	}

	public PropertyDescriptor getPropertyDescriptor(String property) {
		return propertyDescriptors.get( property );
	}

	public boolean isPropertyPresent(String name) {
		return propertyNames.contains( name );
	}

	public List<Class<?>> getDefaultGroupSequence() {
		return Collections.unmodifiableList( defaultGroupSequence );
	}

	public boolean defaultGroupSequenceIsRedefined() {
		return defaultGroupSequence.size() > 1;
	}

	public Set<PropertyDescriptor> getConstrainedProperties() {
		return Collections.unmodifiableSet( new HashSet<PropertyDescriptor>( propertyDescriptors.values() ) );
	}

	private void setDefaultGroupSequence(List<Class<?>> groupSequence) {
		defaultGroupSequence = new ArrayList<Class<?>>();
		boolean groupSequenceContainsDefault = false;
		for ( Class<?> group : groupSequence ) {
			if ( group.getName().equals( beanClass.getName() ) ) {
				defaultGroupSequence.add( Default.class );
				groupSequenceContainsDefault = true;
			}
			else if ( group.getName().equals( Default.class.getName() ) ) {
				throw new GroupDefinitionException( "'Default.class' cannot appear in default group sequence list." );
			}
			else {
				defaultGroupSequence.add( group );
			}
		}
		if ( !groupSequenceContainsDefault ) {
			throw new GroupDefinitionException( beanClass.getName() + " must be part of the redefined default group sequence." );
		}
		if ( log.isTraceEnabled() ) {
			log.trace(
					"Members of the default group sequence for bean {} are: {}",
					beanClass.getName(),
					defaultGroupSequence
			);
		}
	}

	private void addMetaConstraint(Class<?> clazz, MetaConstraint<T, ? extends Annotation> metaConstraint) {
		// first we add the meta constraint to our meta constraint map
		List<MetaConstraint<T, ? extends Annotation>> constraintList;
		if ( !metaConstraints.containsKey( clazz ) ) {
			constraintList = new ArrayList<MetaConstraint<T, ? extends Annotation>>();
			metaConstraints.put( clazz, constraintList );
		}
		else {
			constraintList = metaConstraints.get( clazz );
		}
		constraintList.add( metaConstraint );

		// but we also have to update the descriptors exposing the BV metadata API
		if ( metaConstraint.getElementType() == ElementType.TYPE ) {
			beanDescriptor.addConstraintDescriptor( metaConstraint.getDescriptor() );
		}
		else {
			BeanConstraintSite<?> site = (BeanConstraintSite<?>)(metaConstraint.getSite());
			PropertyDescriptorImpl propertyDescriptor = ( PropertyDescriptorImpl ) propertyDescriptors.get(
					site.getPropertyName()
			);
			if ( propertyDescriptor == null ) {
				Member member = site.getMember();
				propertyDescriptor = addPropertyDescriptorForMember( member, isValidAnnotationPresent( member ) );
			}
			propertyDescriptor.addConstraintDescriptor( metaConstraint.getDescriptor() );
		}
	}
	
	private void addMethodMetaConstraint(Class<?> clazz, MethodMetaData methodMetaData) {

		Map<Method, MethodMetaData> constraintsOfClass = methodMetaConstraints.get(clazz);
		
		if(constraintsOfClass == null) {
			constraintsOfClass = new HashMap<Method, MethodMetaData>();
			methodMetaConstraints.put(clazz, constraintsOfClass);
		}
		
		constraintsOfClass.put(methodMetaData.getMethod(), methodMetaData);
	}

	private void addCascadedMember(Member member) {
		ReflectionHelper.setAccessibility( member );
		cascadedMembers.add( member );
		addPropertyDescriptorForMember( member, true );
	}

	/**
	 * Create bean descriptor, find all classes/subclasses/interfaces which have to be taken in consideration
	 * for this validator and create meta data.
	 *
	 * @param annotationIgnores Data structure keeping track on which annotation should be ignored
	 * @param beanMetaDataCache The bean meta data cache
	 */
	private void createMetaData(AnnotationIgnores annotationIgnores, BeanMetaDataCache beanMetaDataCache) {
		beanDescriptor = new BeanDescriptorImpl<T>( this );
		initDefaultGroupSequence();
		List<Class<?>> classes = ReflectionHelper.computeClassHierarchy( beanClass );
		for ( Class<?> current : classes ) {
			initClass( current, annotationIgnores, beanMetaDataCache );
		}
	}

	private void initClass(Class<?> clazz, AnnotationIgnores annotationIgnores, BeanMetaDataCache beanMetaDataCache) {
		initClassConstraints( clazz, annotationIgnores, beanMetaDataCache );
		initMethodConstraints( clazz, annotationIgnores, beanMetaDataCache );
		initFieldConstraints( clazz, annotationIgnores, beanMetaDataCache );
		initMethodParameterConstraints (clazz, annotationIgnores, beanMetaDataCache );
	}

	/**
	 * Checks whether there is a default group sequence defined for this class.
	 * See HV-113.
	 */
	private void initDefaultGroupSequence() {
		List<Class<?>> groupSequence = new ArrayList<Class<?>>();
		GroupSequence groupSequenceAnnotation = beanClass.getAnnotation( GroupSequence.class );
		if ( groupSequenceAnnotation == null ) {
			groupSequence.add( beanClass );
		}
		else {
			groupSequence.addAll( Arrays.asList( groupSequenceAnnotation.value() ) );
		}

		setDefaultGroupSequence( groupSequence );
	}

	private void initFieldConstraints(Class<?> clazz, AnnotationIgnores annotationIgnores, BeanMetaDataCache beanMetaDataCache) {
		final Field[] fields = ReflectionHelper.getFields( clazz );
		for ( Field field : fields ) {
			addToPropertyNameList( field );

			// HV-172
			if ( Modifier.isStatic( field.getModifiers() ) ) {
				continue;
			}

			if ( annotationIgnores.isIgnoreAnnotations( field ) ) {
				continue;
			}

			// HV-262
			BeanMetaDataImpl<?> cachedMetaData = beanMetaDataCache.getBeanMetaData( clazz );
			List<ConstraintDescriptorImpl<?>> fieldMetaData;
			boolean cachedFieldIsCascaded = false;
			if ( cachedMetaData != null && cachedMetaData.getMetaConstraintsAsMap().get( clazz ) != null ) {
				fieldMetaData = new ArrayList<ConstraintDescriptorImpl<?>>();
				cachedFieldIsCascaded = cachedMetaData.getCascadedMembers().contains( field );
				for ( MetaConstraint<?, ?> metaConstraint : cachedMetaData.getMetaConstraintsAsMap().get( clazz ) ) {
					ConstraintDescriptorImpl<?> descriptor = metaConstraint.getDescriptor();
					if ( descriptor.getElementType() == ElementType.FIELD
							&& ((BeanConstraintSite<?>)(metaConstraint.getSite())).getPropertyName().equals( ReflectionHelper.getPropertyName( field ) ) ) {
						fieldMetaData.add( descriptor );
					}
				}
			}
			else {
				fieldMetaData = findConstraints( field, ElementType.FIELD );
			}

			for ( ConstraintDescriptorImpl<?> constraintDescription : fieldMetaData ) {
				ReflectionHelper.setAccessibility( field );
				MetaConstraint<T, ?> metaConstraint = createMetaConstraint( field, constraintDescription );
				addMetaConstraint( clazz, metaConstraint );
			}

			if ( cachedFieldIsCascaded || field.isAnnotationPresent( Valid.class ) ) {
				addCascadedMember( field );
			}
		}
	}

	private void addToPropertyNameList(Member member) {
		String name = ReflectionHelper.getPropertyName( member );
		if ( name != null ) {
			propertyNames.add( name );
		}
	}

	private void initMethodConstraints(Class<?> clazz, AnnotationIgnores annotationIgnores, BeanMetaDataCache beanMetaDataCache) {
		final Method[] declaredMethods = ReflectionHelper.getMethods( clazz );
		for ( Method method : declaredMethods ) {
			addToPropertyNameList( method );

			// HV-172
			if ( Modifier.isStatic( method.getModifiers() ) ) {
				continue;
			}

			if ( annotationIgnores.isIgnoreAnnotations( method ) ) {
				continue;
			}

			// HV-262
			BeanMetaDataImpl<?> cachedMetaData = beanMetaDataCache.getBeanMetaData( clazz );
			List<ConstraintDescriptorImpl<?>> methodMetaData;
			boolean cachedMethodIsCascaded = false;
			if ( cachedMetaData != null && cachedMetaData.getMetaConstraintsAsMap().get( clazz ) != null ) {
				cachedMethodIsCascaded = cachedMetaData.getCascadedMembers().contains( method );
				methodMetaData = new ArrayList<ConstraintDescriptorImpl<?>>();
				for ( MetaConstraint<?, ?> metaConstraint : cachedMetaData.getMetaConstraintsAsMap().get( clazz ) ) {
					ConstraintDescriptorImpl<?> descriptor = metaConstraint.getDescriptor();
					if ( descriptor.getElementType() == ElementType.METHOD
							&& ((BeanConstraintSite<?>)(metaConstraint.getSite())).getPropertyName().equals( ReflectionHelper.getPropertyName( method ) ) ) {
						methodMetaData.add( descriptor );
					}
				}
			}
			else {
				methodMetaData = findConstraints( method, ElementType.METHOD );
			}

			for ( ConstraintDescriptorImpl<?> constraintDescription : methodMetaData ) {
				ReflectionHelper.setAccessibility( method );
				MetaConstraint<T, ?> metaConstraint = createMetaConstraint( method, constraintDescription );
				addMetaConstraint( clazz, metaConstraint );
			}

			if ( cachedMethodIsCascaded || method.isAnnotationPresent( Valid.class ) ) {
				addCascadedMember( method );
			}
		}
	}

	private <S> void initMethodParameterConstraints(Class<S> clazz, AnnotationIgnores annotationIgnores, BeanMetaDataCache beanMetaDataCache) {
		final Method[] declaredMethods = ReflectionHelper.getMethods( clazz );
		for ( Method method : declaredMethods ) {

			// HV-172
			if ( Modifier.isStatic( method.getModifiers() ) ) {
				continue;
			}

			if ( annotationIgnores.isIgnoreAnnotations( method ) ) {
				continue;
			}

			// HV-262
			BeanMetaDataImpl<S> cachedMetaData = beanMetaDataCache.getBeanMetaData( clazz );
			Map<Integer, ParameterMetaData> constraintsByParameter;
			boolean cachedMethodIsCascaded = false;
//			if ( cachedMetaData != null ) {
////				cachedMethodIsCascaded = cachedMetaData.getCascadedMembers().contains( method );
//				
//				Map<Class<?>, Map<Integer, List<MetaConstraint<S, ? extends Annotation>>>> constraintsByMethod = cachedMetaData.getMetaConstraintsForMethod(method);
//				
//				if(constraintsByMethod != null) {
//					methodMetaData = new HashMap<Integer, List<ConstraintDescriptorImpl<?>>>();
//					for (Entry<Class<?>, Map<Integer, List<MetaConstraint<S, ? extends Annotation>>>> constraintsOfOneParameter : constraintsByMethod.entrySet()) {
//						
//						for(Entry<Integer, List<MetaConstraint<S, ? extends Annotation>>> o :constraintsOfOneParameter.getValue().entrySet()) {
//							List<ConstraintDescriptorImpl<?>> constraintDescriptors = new ArrayList<ConstraintDescriptorImpl<?>>();
//							for(MetaConstraint<S, ? extends Annotation> oneMetaConstraint : o.getValue()) {
//								constraintDescriptors.add(oneMetaConstraint.getDescriptor());
//							}
//							methodMetaData.put(o.getKey(), constraintDescriptors);
//						}
//						
//					}
//				}
//				else {
//					methodMetaData = findParameterConstraints( method );
//				}
//			}
//			else {
			constraintsByParameter = findParameterConstraints( method );
//			}
			
			MethodMetaData methodMetaData = new MethodMetaData(method, constraintsByParameter);
			addMethodMetaConstraint(clazz, methodMetaData );
		}
	}
	
	private PropertyDescriptorImpl addPropertyDescriptorForMember(Member member, boolean isCascaded) {
		String name = ReflectionHelper.getPropertyName( member );
		PropertyDescriptorImpl propertyDescriptor = ( PropertyDescriptorImpl ) propertyDescriptors.get(
				name
		);
		if ( propertyDescriptor == null ) {
			propertyDescriptor = new PropertyDescriptorImpl(
					ReflectionHelper.getType( member ),
					isCascaded,
					name,
					this
			);
			propertyDescriptors.put( name, propertyDescriptor );
		}
		return propertyDescriptor;
	}

	private boolean isValidAnnotationPresent(Member member) {
		return ( ( AnnotatedElement ) member ).isAnnotationPresent( Valid.class );
	}

	private void initClassConstraints(Class<?> clazz, AnnotationIgnores annotationIgnores, BeanMetaDataCache beanMetaDataCache) {
		if ( annotationIgnores.isIgnoreAnnotations( clazz ) ) {
			return;
		}

		// HV-262
		BeanMetaDataImpl<?> cachedMetaData = beanMetaDataCache.getBeanMetaData( clazz );
		List<ConstraintDescriptorImpl<?>> classMetaData;
		if ( cachedMetaData != null && cachedMetaData.getMetaConstraintsAsMap().get( clazz ) != null ) {
			classMetaData = new ArrayList<ConstraintDescriptorImpl<?>>();
			for ( MetaConstraint<?, ?> metaConstraint : cachedMetaData.getMetaConstraintsAsMap().get( clazz ) ) {
				ConstraintDescriptorImpl<?> descriptor = metaConstraint.getDescriptor();
				if ( descriptor.getElementType() == ElementType.TYPE ) {
					classMetaData.add( descriptor );
				}
			}
		}
		else {
			classMetaData = findClassLevelConstraints( clazz );
		}

		for ( ConstraintDescriptorImpl<?> constraintDescription : classMetaData ) {
			MetaConstraint<T, ?> metaConstraint = createMetaConstraint( null, constraintDescription );
			addMetaConstraint( clazz, metaConstraint );
		}
	}

	private <A extends Annotation> MetaConstraint<T, A> createMetaConstraint(Member m, ConstraintDescriptorImpl<A> descriptor) {
		return new MetaConstraint<T, A>( descriptor, new BeanConstraintSite<T>(beanClass, m) );
	}

	private <A extends Annotation> MetaConstraint<T, A> createMetaConstraint(Method method, int parameterIndex, ConstraintDescriptorImpl<A> descriptor) {
		return new MetaConstraint<T, A>( descriptor, new MethodParameterConstraintSite(method, parameterIndex) );
	}
	
	/**
	 * Examines the given annotation to see whether it is a single- or multi-valued constraint annotation.
	 *
	 * @param clazz the class we are currently processing
	 * @param annotation The annotation to examine
	 * @param type the element type on which the annotation/constraint is placed on
	 *
	 * @return A list of constraint descriptors or the empty list in case <code>annotation</code> is neither a
	 *         single nor multi-valued annotation.
	 */
	private <A extends Annotation> List<ConstraintDescriptorImpl<?>> findConstraintAnnotations(Class<?> clazz, A annotation, ElementType type) {
		List<ConstraintDescriptorImpl<?>> constraintDescriptors = new ArrayList<ConstraintDescriptorImpl<?>>();

		List<Annotation> constraints = new ArrayList<Annotation>();
		Class<? extends Annotation> annotationType = annotation.annotationType();
		if ( constraintHelper.isConstraintAnnotation( annotationType )
				|| constraintHelper.isBuiltinConstraint( annotationType ) ) {
			constraints.add( annotation );
		}
		else if ( constraintHelper.isMultiValueConstraint( annotationType ) ) {
			constraints.addAll( constraintHelper.getMultiValueConstraints( annotation ) );
		}

		for ( Annotation constraint : constraints ) {
			final ConstraintDescriptorImpl constraintDescriptor = buildConstraintDescriptor( clazz, constraint, type );
			constraintDescriptors.add( constraintDescriptor );
		}
		return constraintDescriptors;
	}

	@SuppressWarnings("unchecked")
	private <A extends Annotation> ConstraintDescriptorImpl buildConstraintDescriptor(Class<?> clazz, A annotation, ElementType type) {
		ConstraintDescriptorImpl constraintDescriptor;
		ConstraintOrigin definedIn = determineOrigin( clazz );
		if ( clazz.isInterface() && !clazz.equals( beanClass ) ) {
			constraintDescriptor = new ConstraintDescriptorImpl(
					annotation, constraintHelper, clazz, type, definedIn
			);
		}
		else {
			constraintDescriptor = new ConstraintDescriptorImpl( annotation, constraintHelper, type, definedIn );
		}
		return constraintDescriptor;
	}

	/**
	 * Finds all constraint annotations defined for the given class and returns them in a list of
	 * constraint descriptors.
	 *
	 * @param beanClass The class to check for constraints annotations.
	 *
	 * @return A list of constraint descriptors for all constraint specified on the given class.
	 */
	private List<ConstraintDescriptorImpl<?>> findClassLevelConstraints(Class<?> beanClass) {
		List<ConstraintDescriptorImpl<?>> metaData = new ArrayList<ConstraintDescriptorImpl<?>>();
		for ( Annotation annotation : beanClass.getAnnotations() ) {
			metaData.addAll( findConstraintAnnotations( beanClass, annotation, ElementType.TYPE ) );
		}
		return metaData;
	}


	/**
	 * Finds all constraint annotations defined for the given field/method and returns them in a list of
	 * constraint descriptors.
	 *
	 * @param member The fields or method to check for constraints annotations.
	 * @param type The element type the constraint/annotation is placed on.
	 *
	 * @return A list of constraint descriptors for all constraint specified for the given field or method.
	 */
	private List<ConstraintDescriptorImpl<?>> findConstraints(Member member, ElementType type) {
		assert member instanceof Field || member instanceof Method;

		List<ConstraintDescriptorImpl<?>> metaData = new ArrayList<ConstraintDescriptorImpl<?>>();
		for ( Annotation annotation : ( ( AnnotatedElement ) member ).getAnnotations() ) {
			metaData.addAll( findConstraintAnnotations( member.getDeclaringClass(), annotation, type ) );
		}

		return metaData;
	}
	
	private Map<Integer, ParameterMetaData> findParameterConstraints(Method method) {
		
		Map<Integer, ParameterMetaData> metaData = new HashMap<Integer, ParameterMetaData>();
		
		int i = 0;
		for (Annotation[] annotationsOfOneParameter : method.getParameterAnnotations()) {
			
			boolean parameterIsCascading = false;
			List<MetaConstraint<?, ? extends Annotation>> constraintsOfOneParameter = new ArrayList<MetaConstraint<?,? extends Annotation>>();
			
			for (Annotation oneAnnotation : annotationsOfOneParameter) {

				List<ConstraintDescriptorImpl<?>> constraints = findConstraintAnnotations(method.getDeclaringClass(), oneAnnotation, ElementType.PARAMETER);
				
				for (ConstraintDescriptorImpl<?> constraintDescriptorImpl : constraints) {
					ReflectionHelper.setAccessibility( method );
					constraintsOfOneParameter.add( createMetaConstraint(method, i, constraintDescriptorImpl));
				}
				
				if(oneAnnotation.annotationType().equals(Valid.class)) {
					parameterIsCascading = true;
				}
			}
			
			metaData.put(i, new ParameterMetaData(i, constraintsOfOneParameter, parameterIsCascading));
			i++;
		}
		
		return metaData;
	}

	private ConstraintOrigin determineOrigin(Class<?> clazz) {
		if ( clazz.equals( beanClass ) ) {
			return ConstraintOrigin.DEFINED_LOCALLY;
		}
		else {
			return ConstraintOrigin.DEFINED_IN_HIERARCHY;
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "BeanMetaDataImpl" );
		sb.append( "{beanClass=" ).append( beanClass );
		sb.append( ", beanDescriptor=" ).append( beanDescriptor );
		sb.append( ", metaConstraints=" ).append( metaConstraints );
		sb.append( ", methodMetaConstraints=" ).append( methodMetaConstraints );
		sb.append( ", cascadedMembers=" ).append( cascadedMembers );
		sb.append( ", propertyDescriptors=" ).append( propertyDescriptors );
		sb.append( ", defaultGroupSequence=" ).append( defaultGroupSequence );
		sb.append( ", constraintHelper=" ).append( constraintHelper );
		sb.append( '}' );
		return sb.toString();
	}
}
