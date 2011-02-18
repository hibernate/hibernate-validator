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
import javax.validation.ConstraintDeclarationException;
import javax.validation.GroupDefinitionException;
import javax.validation.GroupSequence;
import javax.validation.Valid;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.slf4j.Logger;

import org.hibernate.validator.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.util.LoggerFactory;
import org.hibernate.validator.util.ReflectionHelper;

import static org.hibernate.validator.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.util.ReflectionHelper.newInstance;

/**
 * This class encapsulates all meta data needed for validation. Implementations of {@code Validator} interface can
 * instantiate an instance of this class and delegate the metadata extraction to it.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public final class BeanMetaDataImpl<T> implements BeanMetaData<T> {

	/**
	 * Used as prefix for parameter names, if no explicit names are given.
	 */
	private static final String DEFAULT_PARAMETER_NAME_PREFIX = "arg";

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
	private Map<Class<?>, List<BeanMetaConstraint<T, ? extends Annotation>>> metaConstraints = new HashMap<Class<?>, List<BeanMetaConstraint<T, ? extends Annotation>>>();

	private Map<Class<?>, Map<Method, MethodMetaData>> methodMetaConstraints = new HashMap<Class<?>, Map<Method, MethodMetaData>>();

	/**
	 * Contains meta data for all method's of this type (including the method's
	 * from its super types). Used only at construction time to determine whether
	 * there are any illegal parameter constraints for overridden methods in an
	 * inheritance tree.
	 */
	private final Set<MethodMetaData> allMethods = newHashSet();

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
	 * The default group sequence provider.
	 *
	 * @see org.hibernate.validator.group.GroupSequenceProvider
	 * @see DefaultGroupSequenceProvider
	 */
	private DefaultGroupSequenceProvider<T> defaultGroupSequenceProvider;

	/**
	 * Object keeping track of all constraints.
	 */
	private final ConstraintHelper constraintHelper;

	/**
	 * A list of all property names in the class (constrained and un-constrained).
	 */
	// Used to avoid ReflectionHelper#containsMember which is slow
	private final Set<String> propertyNames = new HashSet<String>( 30 );

	/**
	 * A declaration exception in case the represented bean contains any illegal
	 * method parameter constraints. Such illegal parameter constraints shall
	 * not hinder standard bean/property validation of this type as defined by
	 * the Bean Validation API. Therefore this exception is created when
	 * instantiating this meta data object, but it will only be thrown by the
	 * validation engine when actually a method validation is performed.
	 */
	private final ConstraintDeclarationException parameterConstraintDeclarationException;

	public BeanMetaDataImpl(Class<T> beanClass, ConstraintHelper constraintHelper, BeanMetaDataCache beanMetaDataCache) {
		this(
				beanClass,
				constraintHelper,
				new ArrayList<Class<?>>(),
				new HashMap<Class<?>, List<BeanMetaConstraint<T, ?>>>(),
				new ArrayList<Member>(),
				new AnnotationIgnores(),
				beanMetaDataCache
		);
	}

	public BeanMetaDataImpl(Class<T> beanClass,
							ConstraintHelper constraintHelper,
							List<Class<?>> defaultGroupSequence,
							Map<Class<?>, List<BeanMetaConstraint<T, ?>>> constraints,
							List<Member> cascadedMembers,
							AnnotationIgnores annotationIgnores,
							BeanMetaDataCache beanMetaDataCache) {
		this.beanClass = beanClass;
		this.constraintHelper = constraintHelper;
		this.defaultGroupSequenceProvider = null;

		createMetaData( annotationIgnores, beanMetaDataCache );
		if ( !defaultGroupSequence.isEmpty() ) {
			setDefaultGroupSequence( defaultGroupSequence );
		}
		for ( Map.Entry<Class<?>, List<BeanMetaConstraint<T, ?>>> entry : constraints.entrySet() ) {
			Class<?> clazz = entry.getKey();

			// will hold the method constraints (getter and non-getter) of the given class keyed by method
			Map<Method, List<BeanMetaConstraint<?, ? extends Annotation>>> constraintsByMethod = newHashMap();

			for ( BeanMetaConstraint<T, ?> constraint : entry.getValue() ) {

				if ( constraint.getDescriptor().getElementType() == ElementType.METHOD ) {

					List<BeanMetaConstraint<?, ? extends Annotation>> constraintsForMethod = constraintsByMethod.get(
							constraint.getLocation().getMember()
					);
					if ( constraintsForMethod == null ) {
						constraintsForMethod = newArrayList();
						constraintsByMethod.put(
								(Method) constraint.getLocation().getMember(), constraintsForMethod
						);
					}
					constraintsForMethod.add( constraint );
				}
				//register non-method constraints
				else {
					addMetaConstraint( clazz, constraint );
				}
			}

			//register the constraints for each method in methodMetaConstraints. Constraints at getters will also registered in metaConstraints
			for ( Entry<Method, List<BeanMetaConstraint<?, ? extends Annotation>>> methodAndConstraints : constraintsByMethod
					.entrySet() ) {

				MethodMetaData methodMetaData = new MethodMetaData(
						methodAndConstraints.getKey(),
						methodAndConstraints.getValue(),
						cascadedMembers.contains( methodAndConstraints.getKey() )
				);
				addMethodMetaConstraint( clazz, methodMetaData );
			}

		}
		for ( Member member : cascadedMembers ) {
			// in case a method was specified as cascaded but did not have any constraints we have to register it here
			if ( member instanceof Method && getMetaDataForMethod( (Method) member ) == null ) {
				MethodMetaData methodMetaData = new MethodMetaData(
						(Method) member,
						Collections.<BeanMetaConstraint<?, ? extends Annotation>>emptyList(),
						true
				);
				addMethodMetaConstraint( member.getDeclaringClass(), methodMetaData );
			}
			else {
				addCascadedMember( member );
			}
		}

		parameterConstraintDeclarationException = checkParameterConstraints();
	}

	/**
	 * Checks that there are no invalid parameter constraints defined at this
	 * type's methods.
	 *
	 * @return A {@link ConstraintDeclarationException} describing the first illegal
	 *         method parameter constraint found or <code>null</code>, if this type has no
	 *         such illegal constraints.
	 */
	private ConstraintDeclarationException checkParameterConstraints() {

		for ( MethodMetaData oneMethod : getMethodsWithParameterConstraints( allMethods ) ) {

			Set<MethodMetaData> methodsWithSameSignature = getMethodsWithSameSignature( allMethods, oneMethod );
			Set<MethodMetaData> methodsWithSameSignatureAndParameterConstraints = getMethodsWithParameterConstraints(
					methodsWithSameSignature
			);

			if ( methodsWithSameSignatureAndParameterConstraints.size() > 1 ) {
				return new ConstraintDeclarationException(
						"Only the root method of an overridden method in an inheritance hierarchy may be annotated with parameter constraints, " +
								"but there are parameter constraints defined at all of the following overridden methods: " +
								methodsWithSameSignatureAndParameterConstraints
				);
			}

			for ( MethodMetaData oneMethodWithSameSignature : methodsWithSameSignature ) {
				if ( !oneMethod.getMethod().getDeclaringClass()
						.isAssignableFrom( oneMethodWithSameSignature.getMethod().getDeclaringClass() ) ) {
					return new ConstraintDeclarationException(
							"Only the root method of an overridden method in an inheritance hierarchy may be annotated with parameter constraints. " +
									"The following method itself has no parameter constraints but it is not defined on a sub-type of " +
									oneMethod.getMethod().getDeclaringClass() +
									": " + oneMethodWithSameSignature
					);
				}
			}
		}

		return null;
	}

	public void assertMethodParameterConstraintsCorrectness() {

		if ( parameterConstraintDeclarationException != null ) {
			throw parameterConstraintDeclarationException;
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

	public Map<Class<?>, List<BeanMetaConstraint<T, ? extends Annotation>>> getMetaConstraintsAsMap() {
		return Collections.unmodifiableMap( metaConstraints );
	}

	public List<BeanMetaConstraint<T, ? extends Annotation>> getMetaConstraintsAsList() {
		List<BeanMetaConstraint<T, ? extends Annotation>> constraintList = new ArrayList<BeanMetaConstraint<T, ? extends Annotation>>();
		for ( List<BeanMetaConstraint<T, ? extends Annotation>> list : metaConstraints.values() ) {
			constraintList.addAll( list );
		}
		return Collections.unmodifiableList( constraintList );
	}

	public Map<Class<?>, MethodMetaData> getMetaDataForMethod(Method method) {

		Map<Class<?>, MethodMetaData> theValue = new HashMap<Class<?>, MethodMetaData>();

		for ( Entry<Class<?>, Map<Method, MethodMetaData>> methodsOfOneClass : methodMetaConstraints.entrySet() ) {
			for ( Entry<Method, MethodMetaData> oneMethodEntry : methodsOfOneClass.getValue().entrySet() ) {

				if ( ReflectionHelper.haveSameSignature( method, oneMethodEntry.getKey() ) ) {
					theValue.put( methodsOfOneClass.getKey(), oneMethodEntry.getValue() );
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

	public List<Class<?>> getDefaultGroupSequence(T beanState) {
		if ( hasDefaultGroupSequenceProvider() ) {
			List<Class<?>> providerDefaultGroupSequence = defaultGroupSequenceProvider.getValidationGroups( beanState );
			return getValidDefaultGroupSequence( providerDefaultGroupSequence );
		}

		return Collections.unmodifiableList( defaultGroupSequence );
	}

	public boolean defaultGroupSequenceIsRedefined() {
		return defaultGroupSequence.size() > 1 || hasDefaultGroupSequenceProvider();
	}

	private boolean hasDefaultGroupSequenceProvider() {
		return defaultGroupSequenceProvider != null;
	}

	public Set<PropertyDescriptor> getConstrainedProperties() {
		return Collections.unmodifiableSet( new HashSet<PropertyDescriptor>( propertyDescriptors.values() ) );
	}

	private void setDefaultGroupSequence(List<Class<?>> groupSequence) {
		defaultGroupSequence = getValidDefaultGroupSequence( groupSequence );
	}

	private List<Class<?>> getValidDefaultGroupSequence(List<Class<?>> groupSequence) {
		List<Class<?>> validDefaultGroupSequence = new ArrayList<Class<?>>();

		boolean groupSequenceContainsDefault = false;
		if ( groupSequence != null ) {
			for ( Class<?> group : groupSequence ) {
				if ( group.getName().equals( beanClass.getName() ) ) {
					validDefaultGroupSequence.add( Default.class );
					groupSequenceContainsDefault = true;
				}
				else if ( group.getName().equals( Default.class.getName() ) ) {
					throw new GroupDefinitionException( "'Default.class' cannot appear in default group sequence list." );
				}
				else {
					validDefaultGroupSequence.add( group );
				}
			}
		}
		if ( !groupSequenceContainsDefault ) {
			throw new GroupDefinitionException( beanClass.getName() + " must be part of the redefined default group sequence." );
		}
		if ( log.isTraceEnabled() ) {
			log.trace(
					"Members of the default group sequence for bean {} are: {}",
					beanClass.getName(),
					validDefaultGroupSequence
			);
		}

		return validDefaultGroupSequence;
	}

	private void addMetaConstraint(Class<?> clazz, BeanMetaConstraint<T, ? extends Annotation> metaConstraint) {
		// first we add the meta constraint to our meta constraint map
		List<BeanMetaConstraint<T, ? extends Annotation>> constraintList;
		if ( !metaConstraints.containsKey( clazz ) ) {
			constraintList = new ArrayList<BeanMetaConstraint<T, ? extends Annotation>>();
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
			PropertyDescriptorImpl propertyDescriptor = (PropertyDescriptorImpl) propertyDescriptors.get(
					metaConstraint.getLocation().getPropertyName()
			);
			if ( propertyDescriptor == null ) {
				Member member = metaConstraint.getLocation().getMember();
				propertyDescriptor = addPropertyDescriptorForMember( member, isValidAnnotationPresent( member ) );
			}
			propertyDescriptor.addConstraintDescriptor( metaConstraint.getDescriptor() );
		}
	}

	private void addMethodMetaConstraint(Class<?> clazz, MethodMetaData methodMetaData) {

		allMethods.add( methodMetaData );

		Map<Method, MethodMetaData> constraintsOfClass = methodMetaConstraints.get( clazz );

		if ( constraintsOfClass == null ) {
			constraintsOfClass = new HashMap<Method, MethodMetaData>();
			methodMetaConstraints.put( clazz, constraintsOfClass );
		}

		constraintsOfClass.put( methodMetaData.getMethod(), methodMetaData );

		if ( ReflectionHelper.isGetterMethod( methodMetaData.getMethod() ) ) {

			addToPropertyNameList( methodMetaData.getMethod() );
			ReflectionHelper.setAccessibility( methodMetaData.getMethod() );

			for ( BeanMetaConstraint<?, ? extends Annotation> metaConstraint : methodMetaData ) {
				addMetaConstraint( clazz, (BeanMetaConstraint<T, ? extends Annotation>) metaConstraint );
			}

			if ( methodMetaData.isCascading() ) {
				addCascadedMember( methodMetaData.getMethod() );
			}
		}
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
	}

	/**
	 * Checks whether there is a default group sequence defined for this class.
	 * See HV-113.
	 */
	@SuppressWarnings("unchecked")
	private void initDefaultGroupSequence() {
		List<Class<?>> groupSequence = new ArrayList<Class<?>>();
		GroupSequenceProvider groupSequenceProviderAnnotation = beanClass.getAnnotation( GroupSequenceProvider.class );
		GroupSequence groupSequenceAnnotation = beanClass.getAnnotation( GroupSequence.class );

		if ( groupSequenceAnnotation != null && groupSequenceProviderAnnotation != null ) {
			throw new GroupDefinitionException(
					"GroupSequence and GroupSequenceProvider annotations cannot be used at the same time"
			);
		}

		if ( groupSequenceProviderAnnotation != null ) {

			//Note: this leaves space for ClassCastExceptions when invoking getValidationGroups()
			//on a provider which actually is not parametrized with T; we better check whether the
			//the specified provider actually is for type T in order to throw a clear exception
			defaultGroupSequenceProvider = (DefaultGroupSequenceProvider<T>) newInstance(
					groupSequenceProviderAnnotation.value(), "the default group sequence provider"
			);
		}
		else if ( groupSequenceAnnotation != null ) {
			groupSequence.addAll( Arrays.asList( groupSequenceAnnotation.value() ) );
			setDefaultGroupSequence( groupSequence );
		}
		else {
			groupSequence.add( beanClass );
			setDefaultGroupSequence( groupSequence );
		}
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
				for ( BeanMetaConstraint<?, ?> metaConstraint : cachedMetaData.getMetaConstraintsAsMap()
						.get( clazz ) ) {
					ConstraintDescriptorImpl<?> descriptor = metaConstraint.getDescriptor();
					if ( descriptor.getElementType() == ElementType.FIELD
							&& metaConstraint.getLocation()
							.getPropertyName()
							.equals( ReflectionHelper.getPropertyName( field ) ) ) {
						fieldMetaData.add( descriptor );
					}
				}
			}
			else {
				fieldMetaData = findConstraints( field, ElementType.FIELD );
			}

			for ( ConstraintDescriptorImpl<?> constraintDescription : fieldMetaData ) {
				ReflectionHelper.setAccessibility( field );
				BeanMetaConstraint<T, ?> metaConstraint = createBeanMetaConstraint( field, constraintDescription );
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

			// HV-172
			if ( Modifier.isStatic( method.getModifiers() ) || annotationIgnores.isIgnoreAnnotations( method ) ) {
				continue;
			}

			// try to get meta data from cache, otherwise retrieve it from the class
			MethodMetaData methodMetaData = getFromCache( clazz, method, beanMetaDataCache );

			if ( methodMetaData == null ) {
				methodMetaData = getMethodMetaData( method );
			}

			addMethodMetaConstraint( clazz, methodMetaData );
		}
	}

	private MethodMetaData getFromCache(Class<?> clazz, Method method, BeanMetaDataCache beanMetaDataCache) {

		BeanMetaDataImpl<?> cachedBeanMetaData = beanMetaDataCache.getBeanMetaData( clazz );

		if ( cachedBeanMetaData != null ) {
			return cachedBeanMetaData.getMetaDataForMethod( method ).get( clazz );
		}

		return null;
	}

	private PropertyDescriptorImpl addPropertyDescriptorForMember(Member member, boolean isCascaded) {
		String name = ReflectionHelper.getPropertyName( member );
		PropertyDescriptorImpl propertyDescriptor = (PropertyDescriptorImpl) propertyDescriptors.get(
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
		return ( (AnnotatedElement) member ).isAnnotationPresent( Valid.class );
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
			BeanMetaConstraint<T, ?> metaConstraint = createBeanMetaConstraint( null, constraintDescription );
			addMetaConstraint( clazz, metaConstraint );
		}
	}

	private <A extends Annotation> BeanMetaConstraint<T, ?> createBeanMetaConstraint(Member m, ConstraintDescriptorImpl<A> descriptor) {
		return new BeanMetaConstraint<T, A>( descriptor, beanClass, m );
	}

	private <A extends Annotation> ParameterMetaConstraint<T, A> createParameterMetaConstraint(Method method, int parameterIndex, ConstraintDescriptorImpl<A> descriptor) {
		return new ParameterMetaConstraint<T, A>( descriptor, method, parameterIndex );
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
			final ConstraintDescriptorImpl<?> constraintDescriptor = buildConstraintDescriptor(
					clazz, constraint, type
			);
			constraintDescriptors.add( constraintDescriptor );
		}
		return constraintDescriptors;
	}

	private <A extends Annotation> ConstraintDescriptorImpl<A> buildConstraintDescriptor(Class<?> clazz, A annotation, ElementType type) {
		ConstraintDescriptorImpl<A> constraintDescriptor;
		ConstraintOrigin definedIn = determineOrigin( clazz );
		if ( clazz.isInterface() && !clazz.equals( beanClass ) ) {
			constraintDescriptor = new ConstraintDescriptorImpl<A>(
					annotation, constraintHelper, clazz, type, definedIn
			);
		}
		else {
			constraintDescriptor = new ConstraintDescriptorImpl<A>( annotation, constraintHelper, type, definedIn );
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
		for ( Annotation annotation : ( (AnnotatedElement) member ).getAnnotations() ) {
			metaData.addAll( findConstraintAnnotations( member.getDeclaringClass(), annotation, type ) );
		}

		return metaData;
	}

	/**
	 * Finds all constraint annotations defined for the given method.
	 *
	 * @param method The method to check for constraints annotations.
	 *
	 * @return A meta data object describing the constraints specified for the
	 *         given method.
	 */
	private MethodMetaData getMethodMetaData(Method method) {

		Map<Integer, ParameterMetaData> parameterConstraints = getParameterMetaData( method );
		boolean isCascading = isValidAnnotationPresent( method );
		List<BeanMetaConstraint<?, ? extends Annotation>> constraints =
				convertToMetaConstraints( findConstraints( method, ElementType.METHOD ), method );

		return new MethodMetaData( method, parameterConstraints, constraints, isCascading );
	}

	private List<BeanMetaConstraint<?, ? extends Annotation>> convertToMetaConstraints(List<ConstraintDescriptorImpl<?>> constraintsDescriptors, Method method) {

		List<BeanMetaConstraint<?, ? extends Annotation>> constraints = new ArrayList<BeanMetaConstraint<?, ? extends Annotation>>();

		for ( ConstraintDescriptorImpl<?> oneDescriptor : constraintsDescriptors ) {
			constraints.add( createBeanMetaConstraint( method, oneDescriptor ) );
		}

		return constraints;
	}

	/**
	 * Retrieves constraint related meta data for the parameters of the given
	 * method.
	 *
	 * @param method The method of interest.
	 *
	 * @return A map with parameter meta data for the given method, keyed by
	 *         parameter index.
	 */
	private Map<Integer, ParameterMetaData> getParameterMetaData(Method method) {

		Map<Integer, ParameterMetaData> metaData = newHashMap();

		int i = 0;
		for ( Annotation[] annotationsOfOneParameter : method.getParameterAnnotations() ) {

			boolean parameterIsCascading = false;
			String parameterName = DEFAULT_PARAMETER_NAME_PREFIX + i;
			List<MetaConstraint<?, ? extends Annotation>> constraintsOfOneParameter = newArrayList();

			for ( Annotation oneAnnotation : annotationsOfOneParameter ) {

				//1. collect constraints if this annotation is a constraint annotation
				List<ConstraintDescriptorImpl<?>> constraints = findConstraintAnnotations(
						method.getDeclaringClass(), oneAnnotation, ElementType.PARAMETER
				);
				for ( ConstraintDescriptorImpl<?> constraintDescriptorImpl : constraints ) {
					constraintsOfOneParameter.add(
							createParameterMetaConstraint(
									method, i, constraintDescriptorImpl
							)
					);
				}

				//2. mark parameter as cascading if this annotation is the @Valid annotation
				if ( oneAnnotation.annotationType().equals( Valid.class ) ) {
					parameterIsCascading = true;
				}
			}

			metaData.put(
					i, new ParameterMetaData( i, parameterName, constraintsOfOneParameter, parameterIsCascading )
			);
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

	/**
	 * Returns a set with those methods from the given pile of methods that have
	 * the same signature as the specified one. If the given method itself is part
	 * of the specified pile of methods, it also will be contained in the result
	 * set.
	 *
	 * @param methods The methods to search in.
	 * @param methodToCheck The method to compare against.
	 *
	 * @return A set with methods with the same signature as the given one. May
	 *         be empty, but never null.
	 */
	private Set<MethodMetaData> getMethodsWithSameSignature(Iterable<MethodMetaData> methods, MethodMetaData methodToCheck) {

		Set<MethodMetaData> theValue = newHashSet();

		for ( MethodMetaData oneMethod : methods ) {
			if ( ReflectionHelper.haveSameSignature( oneMethod.getMethod(), methodToCheck.getMethod() ) ) {
				theValue.add( oneMethod );
			}
		}
		return theValue;
	}

	/**
	 * Returns a set with those methods from the given pile of methods that have
	 * at least one constrained parameter or at least one parameter annotated
	 * with {@link Valid}.
	 *
	 * @param methods The methods to search in.
	 *
	 * @return A set with constrained methods. May be empty, but never null.
	 */
	private Set<MethodMetaData> getMethodsWithParameterConstraints(Iterable<MethodMetaData> methods) {

		Set<MethodMetaData> theValue = newHashSet();

		for ( MethodMetaData oneMethod : methods ) {
			if ( oneMethod.hasParameterConstraints() ) {
				theValue.add( oneMethod );
			}
		}

		return theValue;
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
		sb.append( ", defaultGroupSequence=" ).append( getDefaultGroupSequence( null ) );
		sb.append( ", constraintHelper=" ).append( constraintHelper );
		sb.append( '}' );
		return sb.toString();
	}
}
