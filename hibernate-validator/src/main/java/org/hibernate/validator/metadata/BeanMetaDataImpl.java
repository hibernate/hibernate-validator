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

import org.hibernate.validator.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.metadata.AggregatedMethodMetaData.Builder;
import org.hibernate.validator.method.metadata.TypeDescriptor;
import org.hibernate.validator.util.LoggerFactory;
import org.hibernate.validator.util.ReflectionHelper;

import static org.hibernate.validator.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.util.ReflectionHelper.computeAllImplementedInterfaces;
import static org.hibernate.validator.util.ReflectionHelper.getMethods;
import static org.hibernate.validator.util.ReflectionHelper.newInstance;

/**
 * This class encapsulates all meta data needed for validation. Implementations of {@code Validator} interface can
 * instantiate an instance of this class and delegate the metadata extraction to it.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public final class BeanMetaDataImpl<T> implements BeanMetaData<T> {

	private static final Logger log = LoggerFactory.make();

	/**
	 * Used as prefix for parameter names, if no explicit names are given.
	 */
	public static final String DEFAULT_PARAMETER_NAME_PREFIX = "arg";

	/**
	 * The root bean class for this meta data.
	 */
	private final Class<T> beanClass;

	/**
	 * Map of all direct constraints which belong to the entity {@code beanClass}. The constraints are mapped to the class
	 * (eg super class or interface) in which they are defined.
	 */
	private final Map<Class<?>, List<BeanMetaConstraint<?>>> metaConstraints = newHashMap();

	/**
	 * Set of all constraints for this bean type (defined on any implemented interfaces or super types)
	 */
	private final Set<BeanMetaConstraint<?>> allMetaConstraints;

	/**
	 * Set of all constraints which are directly defined on the bean or any of the directly implemented interfaces
	 */
	private final Set<BeanMetaConstraint<?>> directMetaConstraints;

	/**
	 * The main element descriptor for {@link #beanClass}.
	 */
	private BeanDescriptorImpl<T> beanDescriptor;

	/**
	 * Contains constrained related meta data for all methods of the type
	 * represented by this bean meta data. Keyed by method, values are an
	 * aggregated view on each method together with all the methods from the
	 * inheritance hierarchy with the same signature.
	 */
	private Map<Method, AggregatedMethodMetaData> methodMetaData;

	/**
	 * Builders used for gathering method meta data. Used only at construction
	 * time.
	 */
	private Set<AggregatedMethodMetaData.Builder> methodMetaDataBuilders = newHashSet();

	/**
	 * List of cascaded members.
	 */
	private Set<Member> cascadedMembers = newHashSet();

	/**
	 * Maps field and method names to their {@code ElementDescriptorImpl}.
	 */
	private Map<String, PropertyDescriptor> propertyDescriptors = newHashMap();

	/**
	 * The default groups sequence for this bean class.
	 */
	private List<Class<?>> defaultGroupSequence = newArrayList();

	/**
	 * The default group sequence provider.
	 *
	 * @see org.hibernate.validator.group.GroupSequenceProvider
	 * @see DefaultGroupSequenceProvider
	 */
	private DefaultGroupSequenceProvider<T> defaultGroupSequenceProvider;

	/**
	 * The class hierarchy for this class starting with the class itself going up the inheritance chain. Interfaces
	 * are not included.
	 */
	private List<Class<?>> classHierarchyWithoutInterfaces;

	/**
	 * Object used to determine whether a given annotation is a constraint. Only used during initialization.
	 */
	private ConstraintHelper constraintHelper;

	/**
	 * A list of all property names in the class (constrained and un-constrained).
	 */
	// Used to avoid ReflectionHelper#containsMember which is slow
	private final Set<String> propertyNames = newHashSet( 30 );

	/**
	 * Constructor used for creating the bean meta data using annotations only
	 *
	 * @param beanClass The bean type for which to create the meta data
	 * @param constraintHelper constraint helper
	 * @param beanMetaDataCache the cache of already configured meta data instances
	 */
	public BeanMetaDataImpl(Class<T> beanClass, ConstraintHelper constraintHelper, BeanMetaDataCache beanMetaDataCache) {
		this(
				beanClass,
				constraintHelper,
				new ArrayList<Class<?>>(),
				null,
				new HashMap<Class<?>, List<BeanMetaConstraint<?>>>(),
				new HashSet<AggregatedMethodMetaData>(),
				new HashSet<Member>(),
				new AnnotationIgnores(),
				beanMetaDataCache
		);
	}

	/**
	 * Constructor used when creating a bean meta data instance via the xml or programmatic API. In this case
	 * additional metadata (the already configured constraints, cascaded members, etc) are passed as well.
	 *
	 * @param beanClass The bean type for which to create the meta data
	 * @param constraintHelper constraint helper
	 * @param defaultGroupSequence programmatic/xml configured default group sequence (overrides annotations)
	 * @param defaultGroupSequenceProvider programmatic configured default group sequence provider class (overrides annotations)
	 * @param constraints programmatic/xml configured constraints
	 * @param methodMetaDatas programmatic configured method constraints
	 * @param cascadedMembers programmatic/xml configured cascaded members
	 * @param annotationIgnores in xml configured ignores for annotations
	 * @param beanMetaDataCache the cache of already configured meta data instances
	 */
	public BeanMetaDataImpl(Class<T> beanClass,
							ConstraintHelper constraintHelper,
							List<Class<?>> defaultGroupSequence,
							Class<? extends DefaultGroupSequenceProvider<?>> defaultGroupSequenceProvider,
							Map<Class<?>, List<BeanMetaConstraint<?>>> constraints,
							Set<AggregatedMethodMetaData> methodMetaDatas,
							Set<Member> cascadedMembers,
							AnnotationIgnores annotationIgnores,
							BeanMetaDataCache beanMetaDataCache) {
		this.beanClass = beanClass;
		this.constraintHelper = constraintHelper;
		this.defaultGroupSequenceProvider = null;
		for ( Member member : cascadedMembers ) {
			addCascadedMember( member );
		}

		classHierarchyWithoutInterfaces = ReflectionHelper.computeClassHierarchy( beanClass, false );

		// start the annotation discovery phase (look for annotations in the whole class hierarchy)
		createMetaData( annotationIgnores, beanMetaDataCache );

		// set the default explicitly specified default group sequence after the discovery process is complete
		if ( !defaultGroupSequence.isEmpty() ) {
			setDefaultGroupSequence( defaultGroupSequence );
		}

		// set the default explicitly specified default group sequence provider after the discovery process is complete
		if ( defaultGroupSequenceProvider != null ) {
			this.defaultGroupSequenceProvider = newGroupSequenceProviderInstance( defaultGroupSequenceProvider );
		}

		// validates that programmatic/xml definition of default group sequence or default group sequence provider
		// doesn't introduce illegal default group sequence definition.
		if ( hasDefaultGroupSequenceProvider() && this.defaultGroupSequence.size() > 1 ) {
			throw new GroupDefinitionException(
					"Default group sequence and default group sequence provider cannot be defined at the same time"
			);
		}

		// add the explicitly configured constraints
		for ( Map.Entry<Class<?>, List<BeanMetaConstraint<?>>> entry : constraints.entrySet() ) {
			Class<?> clazz = entry.getKey();

			// will hold the method constraints (getter and non-getter) of the given class keyed by method
			Map<Method, List<MethodMetaConstraint<?>>> constraintsByMethod = newHashMap();

			for ( BeanMetaConstraint<?> constraint : entry.getValue() ) {

				if ( constraint.getDescriptor().getElementType() == ElementType.METHOD ) {

					List<MethodMetaConstraint<?>> constraintsForMethod = constraintsByMethod.get(
							constraint.getLocation().getMember()
					);
					if ( constraintsForMethod == null ) {
						constraintsForMethod = newArrayList();
						constraintsByMethod.put(
								(Method) constraint.getLocation().getMember(), constraintsForMethod
						);
					}

					constraintsForMethod.add( getAsMethodMetaConstraint( constraint ) );
				}
				//register non-method constraints
				else {
					addMetaConstraint( clazz, constraint );
				}
			}

			// register the constraints for each method in methodMetaConstraints. Constraints at getters will also registered in metaConstraints
			for ( Entry<Method, List<MethodMetaConstraint<?>>> methodAndConstraints : constraintsByMethod
					.entrySet() ) {

				MethodMetaData methodMetaData = new MethodMetaData(
						methodAndConstraints.getKey(),
						methodAndConstraints.getValue(),
						cascadedMembers.contains( methodAndConstraints.getKey() )
				);
				addMethodMetaConstraint( clazz, methodMetaData );
			}

		}

		allMetaConstraints = buildAllConstraintSets();
		directMetaConstraints = buildDirectConstraintSets();

		// add the explicitly configured method constraints, here we need to merge the programmatic and discovered
		// metadata built with the "automatic" discovering.
		if ( !methodMetaDatas.isEmpty() ) {
			for ( AggregatedMethodMetaData aggregatedMethodMetaData : methodMetaDatas ) {
				for ( MethodMetaData methodMetaData : aggregatedMethodMetaData.getAllMethodMetaData() ) {
					Method method = methodMetaData.getMethod();
					addMethodMetaConstraint( method.getDeclaringClass(), methodMetaData );
				}
			}
		}

		this.methodMetaData = Collections.unmodifiableMap( buildMethodMetaData() );

		// reset class members we don't need any longer
		this.methodMetaDataBuilders = null;
		this.constraintHelper = null;
	}

	public Class<T> getBeanClass() {
		return beanClass;
	}

	public BeanDescriptor getBeanDescriptor() {
		return beanDescriptor;
	}

	public TypeDescriptor getTypeDescriptor() {
		return beanDescriptor;
	}

	public Set<Member> getCascadedMembers() {
		return Collections.unmodifiableSet( cascadedMembers );
	}

	public Map<Class<?>, List<BeanMetaConstraint<?>>> getMetaConstraintsAsMap() {
		return Collections.unmodifiableMap( metaConstraints );
	}

	public Set<BeanMetaConstraint<?>> getMetaConstraints() {
		return allMetaConstraints;
	}

	public Set<BeanMetaConstraint<?>> getDirectMetaConstraints() {
		return directMetaConstraints;
	}

	public AggregatedMethodMetaData getMetaDataFor(Method method) {
		return methodMetaData.get( method );
	}

	public Set<AggregatedMethodMetaData> getAllMethodMetaData() {
		return new HashSet<AggregatedMethodMetaData>( methodMetaData.values() );
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

	private Set<BeanMetaConstraint<?>> buildAllConstraintSets() {
		Set<BeanMetaConstraint<?>> constraints = newHashSet();
		for ( List<BeanMetaConstraint<?>> list : metaConstraints.values() ) {
			constraints.addAll( list );
		}
		return Collections.unmodifiableSet( constraints );
	}

	private Set<BeanMetaConstraint<?>> buildDirectConstraintSets() {
		Set<BeanMetaConstraint<?>> constraints = newHashSet();
		// collect all constraints directly defined in this bean
		if ( metaConstraints.get( beanClass ) != null ) {
			constraints.addAll( metaConstraints.get( beanClass ) );
		}
		Set<Class<?>> classAndInterfaces = computeAllImplementedInterfaces( beanClass );
		for ( Class<?> clazz : classAndInterfaces ) {
			if ( metaConstraints.get( clazz ) != null ) {
				constraints.addAll( metaConstraints.get( clazz ) );
			}
		}
		return Collections.unmodifiableSet( constraints );
	}

	/**
	 * Builds up the method meta data for this type by invoking each builder in
	 * {@link #methodMetaDataBuilders}.
	 */
	private Map<Method, AggregatedMethodMetaData> buildMethodMetaData() {

		Map<Method, AggregatedMethodMetaData> theValue = newHashMap();

		for ( AggregatedMethodMetaData.Builder oneBuilder : methodMetaDataBuilders ) {

			AggregatedMethodMetaData aggregatedMethodMetaData = oneBuilder.build();

			//register the aggregated meta data for each underlying method for a quick
			//read access
			for ( MethodMetaData oneMethodMetaData : aggregatedMethodMetaData.getAllMethodMetaData() ) {
				theValue.put( oneMethodMetaData.getMethod(), aggregatedMethodMetaData );
			}
		}

		return theValue;
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

	private void addMetaConstraint(Class<?> clazz, BeanMetaConstraint<?> metaConstraint) {
		// first we add the meta constraint to our meta constraint map
		List<BeanMetaConstraint<?>> constraintList;
		if ( !metaConstraints.containsKey( clazz ) ) {
			constraintList = new ArrayList<BeanMetaConstraint<?>>();
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

		addToBuilder( methodMetaData );

		if ( ReflectionHelper.isGetterMethod( methodMetaData.getMethod() ) ) {

			addToPropertyNameList( methodMetaData.getMethod() );
			ReflectionHelper.setAccessibility( methodMetaData.getMethod() );

			for ( MethodMetaConstraint<?> metaConstraint : methodMetaData ) {

				addMetaConstraint( clazz, getAsBeanMetaConstraint( metaConstraint, methodMetaData.getMethod() ) );
			}

			if ( methodMetaData.isCascading() ) {
				addCascadedMember( methodMetaData.getMethod() );
			}
		}
	}

	/**
	 * <p>
	 * Adds the given method meta data to the aggregated method meta data
	 * builder for this method signature. In case no aggregation builder exists
	 * yet for this method signature, one will be created with the given method
	 * meta data as base entry.
	 * </p>
	 * <p>
	 * As the type hierarchy is traversed bottom-up when building the bean meta
	 * data for a given type, it is ensured that for each method the aggregated
	 * method meta data is based on the lowest implementation of this method in
	 * the hierarchy.
	 * </p>
	 *
	 * @param metaData The method meta data to add.
	 */
	private void addToBuilder(MethodMetaData metaData) {

		// add the given meta data to an existing aggregation builder ...
		for ( Builder oneBuilder : methodMetaDataBuilders ) {

			if ( oneBuilder.accepts( metaData ) ) {
				oneBuilder.addMetaData( metaData );
				return;
			}
		}

		// ... or create a new builder
		Builder newBuilder = new AggregatedMethodMetaData.Builder( metaData );
		methodMetaDataBuilders.add( newBuilder );
	}

	private void addCascadedMember(Member member) {
		ReflectionHelper.setAccessibility( member );
		cascadedMembers.add( member );
		addPropertyDescriptorForMember( member, true );
	}

	public List<Class<?>> getClassHierarchy() {
		return classHierarchyWithoutInterfaces;
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
		for ( Class<?> current : ReflectionHelper.computeClassHierarchy( beanClass, true ) ) {
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
			defaultGroupSequenceProvider = newGroupSequenceProviderInstance( groupSequenceProviderAnnotation.value() );
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
		final Field[] fields = ReflectionHelper.getDeclaredFields( clazz );
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
			boolean metaDataCached = cachedMetaData != null;
			List<ConstraintDescriptorImpl<?>> fieldMetaData;
			if ( metaDataCached && cachedMetaData.getMetaConstraintsAsMap().get( clazz ) != null ) {
				fieldMetaData = new ArrayList<ConstraintDescriptorImpl<?>>();
				for ( BeanMetaConstraint<?> metaConstraint : cachedMetaData.getMetaConstraintsAsMap()
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
				BeanMetaConstraint<?> metaConstraint = createBeanMetaConstraint( clazz, field, constraintDescription );
				addMetaConstraint( clazz, metaConstraint );
			}

			// HV-433 Make sure the field is marked as cascaded in case it was configured via xml/programmatic API or
			// it hosts the @Valid annotation
			boolean isCascadedField = metaDataCached && cachedMetaData.getCascadedMembers().contains( field );
			if ( isCascadedField || field.isAnnotationPresent( Valid.class ) ) {
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
		final Method[] declaredMethods = ReflectionHelper.getDeclaredMethods( clazz );

		for ( Method method : declaredMethods ) {

			// HV-172; ignoring synthetic methods (inserted by the compiler), as they can't have any constraints
			// anyway and possibly hide the actual method with the same signature in the built meta model
			if ( Modifier.isStatic( method.getModifiers() ) || annotationIgnores.isIgnoreAnnotations( method ) || method
					.isSynthetic() ) {
				continue;
			}

			// try to get meta data from cache, otherwise retrieve it from the class
			MethodMetaData methodMetaData = getFromCache( clazz, method, beanMetaDataCache );

			if ( methodMetaData == null ) {
				methodMetaData = findMethodMetaData( method );
			}

			addMethodMetaConstraint( clazz, methodMetaData );
		}
	}

	private MethodMetaData getFromCache(Class<?> clazz, Method method, BeanMetaDataCache beanMetaDataCache) {
		BeanMetaDataImpl<?> cachedBeanMetaData = beanMetaDataCache.getBeanMetaData( clazz );

		if ( cachedBeanMetaData != null ) {
			AggregatedMethodMetaData cachedMethodMetaData = cachedBeanMetaData.getMetaDataFor( method );

			if ( cachedMethodMetaData != null ) {
				return cachedMethodMetaData.getSingleMetaDataFor( method );
			}
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
			for ( MetaConstraint<?> metaConstraint : cachedMetaData.getMetaConstraintsAsMap().get( clazz ) ) {
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
			BeanMetaConstraint<?> metaConstraint = createBeanMetaConstraint( clazz, null, constraintDescription );
			addMetaConstraint( clazz, metaConstraint );
		}
	}

	private <A extends Annotation> BeanMetaConstraint<?> createBeanMetaConstraint(Class<?> declaringClass, Member m, ConstraintDescriptorImpl<A> descriptor) {
		return new BeanMetaConstraint<A>( descriptor, declaringClass, m );
	}

	private <A extends Annotation> MethodMetaConstraint<A> createParameterMetaConstraint(Method method, int parameterIndex, ConstraintDescriptorImpl<A> descriptor) {
		return new MethodMetaConstraint<A>( descriptor, method, parameterIndex );
	}

	private <A extends Annotation> MethodMetaConstraint<A> createReturnValueMetaConstraint(Method method, ConstraintDescriptorImpl<A> descriptor) {
		return new MethodMetaConstraint<A>( descriptor, method );
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
	private MethodMetaData findMethodMetaData(Method method) {

		List<ParameterMetaData> parameterConstraints = getParameterMetaData( method );
		boolean isCascading = isValidAnnotationPresent( method ) || cascadedMembers.contains( method );
		List<MethodMetaConstraint<?>> constraints =
				convertToMetaConstraints( findConstraints( method, ElementType.METHOD ), method );

		return new MethodMetaData( method, parameterConstraints, constraints, isCascading );
	}

	private List<MethodMetaConstraint<?>> convertToMetaConstraints(List<ConstraintDescriptorImpl<?>> constraintsDescriptors, Method method) {

		List<MethodMetaConstraint<?>> constraints = newArrayList();

		for ( ConstraintDescriptorImpl<?> oneDescriptor : constraintsDescriptors ) {
			constraints.add( createReturnValueMetaConstraint( method, oneDescriptor ) );
		}

		return constraints;
	}

	/**
	 * Retrieves constraint related meta data for the parameters of the given
	 * method.
	 *
	 * @param method The method of interest.
	 *
	 * @return A list with parameter meta data for the given method.
	 */
	private List<ParameterMetaData> getParameterMetaData(Method method) {
		List<ParameterMetaData> metaData = newArrayList();

		int i = 0;
		Class<?>[] parameterTypes = method.getParameterTypes();

		for ( Annotation[] annotationsOfOneParameter : method.getParameterAnnotations() ) {

			boolean parameterIsCascading = false;
			String parameterName = DEFAULT_PARAMETER_NAME_PREFIX + i;
			List<MethodMetaConstraint<?>> constraintsOfOneParameter = newArrayList();

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

			metaData.add(
					new ParameterMetaData(
							i, parameterTypes[i], parameterName, constraintsOfOneParameter, parameterIsCascading
					)
			);
			i++;
		}

		return metaData;
	}

	private <A extends Annotation> MethodMetaConstraint<A> getAsMethodMetaConstraint(BeanMetaConstraint<A> beanMetaConstraint) {
		return new MethodMetaConstraint<A>(
				beanMetaConstraint.getDescriptor(), (Method) beanMetaConstraint.getLocation().getMember()
		);
	}

	private <A extends Annotation> BeanMetaConstraint<A> getAsBeanMetaConstraint(MethodMetaConstraint<A> methodMetaConstraint, Method method) {
		return new BeanMetaConstraint<A>(
				methodMetaConstraint.getDescriptor(), methodMetaConstraint.getLocation().getBeanClass(), method
		);
	}

	private ConstraintOrigin determineOrigin(Class<?> clazz) {
		if ( clazz.equals( beanClass ) ) {
			return ConstraintOrigin.DEFINED_LOCALLY;
		}
		else {
			return ConstraintOrigin.DEFINED_IN_HIERARCHY;
		}
	}

	@SuppressWarnings("unchecked")
	private <U extends DefaultGroupSequenceProvider<?>> DefaultGroupSequenceProvider<T> newGroupSequenceProviderInstance(Class<U> providerClass) {
		Method[] providerMethods = getMethods( providerClass );
		for ( Method method : providerMethods ) {
			Class<?>[] paramTypes = method.getParameterTypes();
			if ( "getValidationGroups".equals( method.getName() ) && !method.isBridge()
					&& paramTypes.length == 1 && paramTypes[0].isAssignableFrom( beanClass ) ) {

				return (DefaultGroupSequenceProvider<T>) newInstance(
						providerClass, "the default group sequence provider"
				);
			}
		}

		throw new GroupDefinitionException(
				"The default group sequence provider defined for " + beanClass.getName() + " has the wrong type"
		);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "BeanMetaDataImpl" );
		sb.append( "{beanClass=" ).append( beanClass.getSimpleName() );
		sb.append( ", constraintCount=" ).append( getMetaConstraints().size() );
		sb.append( ", cascadedMemberCount=" ).append( cascadedMembers.size() );
		sb.append( ", defaultGroupSequence=" ).append( getDefaultGroupSequence( null ) );
		sb.append( '}' );
		return sb.toString();
	}
}
