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
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.annotation.ElementType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.validation.GroupDefinitionException;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.internal.metadata.aggregated.ConstraintMetaData.ConstraintMetaDataKind;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.BeanDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedMethod;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.util.CollectionHelper.Partitioner;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.method.metadata.MethodDescriptor;
import org.hibernate.validator.method.metadata.TypeDescriptor;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.CollectionHelper.partition;
import static org.hibernate.validator.internal.util.ReflectionHelper.computeAllImplementedInterfaces;
import static org.hibernate.validator.internal.util.ReflectionHelper.getMethods;
import static org.hibernate.validator.internal.util.ReflectionHelper.newInstance;

/**
 * This class encapsulates all meta data needed for validation. Implementations of {@code Validator} interface can
 * instantiate an instance of this class and delegate the metadata extraction to it.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public final class BeanMetaDataImpl<T> implements BeanMetaData<T> {

	private static final Log log = LoggerFactory.make();

	/**
	 * The root bean class for this meta data.
	 */
	private final Class<T> beanClass;

	/**
	 * Set of all constraints for this bean type (defined on any implemented interfaces or super types)
	 */
	private final Set<MetaConstraint<?>> allMetaConstraints;

	/**
	 * Set of all constraints which are directly defined on the bean or any of the directly implemented interfaces
	 */
	private final Set<MetaConstraint<?>> directMetaConstraints;

	/**
	 * Contains constrained related meta data for all methods of the type
	 * represented by this bean meta data. Keyed by method, values are an
	 * aggregated view on each method together with all the methods from the
	 * inheritance hierarchy with the same signature.
	 */
	private final Map<String, MethodMetaData> methodMetaData;

	private final Map<String, PropertyMetaData> propertyMetaData;

	/**
	 * List of cascaded members.
	 */
	private final Set<Member> cascadedMembers;

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
	 * Creates a new {@link BeanMetaDataImpl}
	 *
	 * @param beanClass The Java type represented by this meta data object.
	 * @param defaultGroupSequence The default group sequence.
	 * @param defaultGroupSequenceProvider The default group sequence provider if set.
	 * @param constraintMetaData All constraint meta data relating to the represented type.
	 */
	public BeanMetaDataImpl(Class<T> beanClass,
							List<Class<?>> defaultGroupSequence,
							Class<? extends DefaultGroupSequenceProvider<?>> defaultGroupSequenceProvider,
							Set<ConstraintMetaData> constraintMetaData) {

		this.beanClass = beanClass;
		this.propertyMetaData = newHashMap();

		Set<PropertyMetaData> propertyMetaDataSet = newHashSet();
		Set<MethodMetaData> methodMetaDataSet = newHashSet();

		for ( ConstraintMetaData oneElement : constraintMetaData ) {
			if ( oneElement.getKind() == ConstraintMetaDataKind.PROPERTY ) {
				propertyMetaDataSet.add( (PropertyMetaData) oneElement );
			}
			else {
				methodMetaDataSet.add( (MethodMetaData) oneElement );
			}
		}

		Set<Member> cascadedMembers = newHashSet();
		Set<MetaConstraint<?>> allMetaConstraints = newHashSet();

		for ( PropertyMetaData oneProperty : propertyMetaDataSet ) {

			propertyMetaData.put( oneProperty.getName(), oneProperty );

			if ( oneProperty.isCascading() ) {
				cascadedMembers.addAll( oneProperty.getCascadingMembers() );
			}

			allMetaConstraints.addAll( oneProperty.getConstraints() );
		}

		this.cascadedMembers = Collections.unmodifiableSet( cascadedMembers );
		this.allMetaConstraints = Collections.unmodifiableSet( allMetaConstraints );

		classHierarchyWithoutInterfaces = ReflectionHelper.computeClassHierarchy( beanClass, false );

		setDefaultGroupSequenceOrProvider( defaultGroupSequence, defaultGroupSequenceProvider );

		directMetaConstraints = buildDirectConstraintSets();

		this.methodMetaData = Collections.unmodifiableMap( buildMethodMetaData( methodMetaDataSet ) );
	}

	public Class<T> getBeanClass() {
		return beanClass;
	}

	public BeanDescriptor getBeanDescriptor() {
		return getBeanDescriptorInternal();
	}

	public TypeDescriptor getTypeDescriptor() {
		return getBeanDescriptorInternal();
	}

	public Set<Member> getCascadedMembers() {
		return cascadedMembers;
	}

	public Set<MetaConstraint<?>> getMetaConstraints() {
		return allMetaConstraints;
	}

	public Set<MetaConstraint<?>> getDirectMetaConstraints() {
		return directMetaConstraints;
	}

	public MethodMetaData getMetaDataFor(Method method) {
		return methodMetaData.get( method.getName() + Arrays.toString( method.getParameterTypes() ) );
	}

	public Set<MethodMetaData> getAllMethodMetaData() {
		return new HashSet<MethodMetaData>( methodMetaData.values() );
	}

	public PropertyMetaData getMetaDataFor(String propertyName) {
		return propertyMetaData.get( propertyName );
	}

	public boolean isPropertyPresent(String name) {
		return propertyMetaData.containsKey( name );
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

	public Set<PropertyMetaData> getAllPropertyMetaData() {
		return Collections.unmodifiableSet( new HashSet<PropertyMetaData>( propertyMetaData.values() ) );
	}

	public List<Class<?>> getClassHierarchy() {
		return classHierarchyWithoutInterfaces;
	}

	/**
	 * Returns a bean descriptor representing this meta data object. A new
	 * descriptor instance is created with each invocation. The descriptor might
	 * be cached internally in the future should that need arise.
	 * 
	 * @return A bean descriptor for this meta data object.
	 */
	private BeanDescriptorImpl<T> getBeanDescriptorInternal() {

		return new BeanDescriptorImpl<T>(
				beanClass,
				getClassLevelConstraintsAsDescriptors(),
				getConstrainedPropertiesAsDescriptors(),
				getMethodsAsDescriptors(),
				defaultGroupSequenceIsRedefined(),
				getDefaultGroupSequence( null )
		);
	}

	private Set<ConstraintDescriptorImpl<?>> getClassLevelConstraintsAsDescriptors() {

		Set<MetaConstraint<?>> classLevelConstraints = getClassLevelConstraints( allMetaConstraints );

		Set<ConstraintDescriptorImpl<?>> theValue = newHashSet();

		for ( MetaConstraint<?> oneConstraint : classLevelConstraints ) {
			theValue.add( oneConstraint.getDescriptor() );
		}

		return theValue;
	}

	private Map<String, PropertyDescriptor> getConstrainedPropertiesAsDescriptors() {
		Map<String, PropertyDescriptor> theValue = newHashMap();

		for ( Entry<String, PropertyMetaData> oneProperty : propertyMetaData.entrySet() ) {
			if ( oneProperty.getValue().isConstrained() ) {
				theValue.put(
						oneProperty.getKey(),
						oneProperty.getValue()
								.asDescriptor( defaultGroupSequenceIsRedefined(), getDefaultGroupSequence( null ) )
				);
			}
		}

		return theValue;
	}

	private Map<String, MethodDescriptor> getMethodsAsDescriptors() {
		Map<String, MethodDescriptor> theValue = newHashMap();

		for ( Entry<String, MethodMetaData> oneMethod : methodMetaData.entrySet() ) {
			theValue.put(
					oneMethod.getKey(), oneMethod.getValue().asDescriptor(
					defaultGroupSequenceIsRedefined(),
					getDefaultGroupSequence( null )
			)
			);
		}

		return theValue;
	}

	private void setDefaultGroupSequenceOrProvider(List<Class<?>> defaultGroupSequence, Class<? extends DefaultGroupSequenceProvider<?>> defaultGroupSequenceProvider) {

		if ( defaultGroupSequence != null && defaultGroupSequenceProvider != null ) {
			throw new GroupDefinitionException(
					"Default group sequence and default group sequence provider cannot be defined at the same time."
			);
		}

		if ( defaultGroupSequenceProvider != null ) {
			this.defaultGroupSequenceProvider = newGroupSequenceProviderInstance( defaultGroupSequenceProvider );
		}
		else if ( defaultGroupSequence != null && !defaultGroupSequence.isEmpty() ) {
			setDefaultGroupSequence( defaultGroupSequence );
		}
		else {
			setDefaultGroupSequence( Arrays.<Class<?>>asList( beanClass ) );
		}
	}

	private Set<MetaConstraint<?>> getClassLevelConstraints(Set<MetaConstraint<?>> constraints) {

		Set<MetaConstraint<?>> classLevelConstraints = partition(
				constraints,
				byElementType()
		).get( ElementType.TYPE );

		return classLevelConstraints != null ? classLevelConstraints : Collections.<MetaConstraint<?>>emptySet();
	}

	private Set<MetaConstraint<?>> buildDirectConstraintSets() {

		Set<MetaConstraint<?>> constraints = newHashSet();

		Set<Class<?>> classAndInterfaces = computeAllImplementedInterfaces( beanClass );
		classAndInterfaces.add( beanClass );

		for ( Class<?> clazz : classAndInterfaces ) {
			for ( MetaConstraint<?> oneConstraint : allMetaConstraints ) {
				if ( oneConstraint.getLocation().getBeanClass().equals( clazz ) ) {
					constraints.add( oneConstraint );
				}
			}
		}

		return Collections.unmodifiableSet( constraints );
	}

	/**
	 * Builds up the method meta data for this type
	 */
	private Map<String, MethodMetaData> buildMethodMetaData(Set<MethodMetaData> allMethodMetaData) {

		Map<String, MethodMetaData> theValue = newHashMap();

		for ( MethodMetaData oneAggregatedMethodMetaData : allMethodMetaData ) {
			theValue.put(
					oneAggregatedMethodMetaData.getName() + Arrays.toString( oneAggregatedMethodMetaData.getParameterTypes() ),
					oneAggregatedMethodMetaData
			);
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
			log.tracef(
					"Members of the default group sequence for bean %s are: %s.",
					beanClass.getName(),
					validDefaultGroupSequence
			);
		}

		return validDefaultGroupSequence;
	}

	private boolean hasDefaultGroupSequenceProvider() {
		return defaultGroupSequenceProvider != null;
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

	private Partitioner<ElementType, MetaConstraint<?>> byElementType() {
		return new Partitioner<ElementType, MetaConstraint<?>>() {
			public ElementType getPartition(MetaConstraint<?> constraint) {
				return constraint.getElementType();
			}
		};
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

	public static class BeanMetaDataBuilder<T> {

		private final ConstraintHelper constraintHelper;

		private final Class<T> beanClass;

		private final Set<BuilderDelegate> builders = newHashSet();

		private ConfigurationSource sequenceSource;

		private ConfigurationSource providerSource;

		private List<Class<?>> defaultGroupSequence;

		private Class<? extends DefaultGroupSequenceProvider<?>> defaultGroupSequenceProvider;


		public BeanMetaDataBuilder(ConstraintHelper constraintHelper, Class<T> beanClass) {
			this.beanClass = beanClass;
			this.constraintHelper = constraintHelper;
		}

		public static <T> BeanMetaDataBuilder<T> getInstance(ConstraintHelper constraintHelper, Class<T> beanClass) {
			return new BeanMetaDataBuilder<T>( constraintHelper, beanClass );
		}

		public void add(BeanConfiguration<?> configuration) {

			if ( configuration.getBeanClass().equals( beanClass ) ) {

				if ( configuration.getDefaultGroupSequence() != null &&
						( sequenceSource == null || configuration.getSource()
								.getPriority() >= sequenceSource.getPriority() ) ) {

					sequenceSource = configuration.getSource();
					defaultGroupSequence = configuration.getDefaultGroupSequence();
				}

				if ( configuration.getDefaultGroupSequenceProvider() != null &&
						( providerSource == null || configuration.getSource()
								.getPriority() >= providerSource.getPriority() ) ) {

					providerSource = configuration.getSource();
					defaultGroupSequenceProvider = configuration.getDefaultGroupSequenceProvider();
				}
			}

			for ( ConstrainedElement oneConstrainedElement : configuration.getConstrainedElements() ) {
				addMetaDataToBuilder( oneConstrainedElement, builders );
			}
		}

		private void addMetaDataToBuilder(ConstrainedElement constrainableElement, Set<BuilderDelegate> builders) {

			for ( BuilderDelegate oneBuilder : builders ) {
				boolean foundBuilder = oneBuilder.add( constrainableElement );

				if ( foundBuilder ) {
					return;
				}
			}

			builders.add(
					new BuilderDelegate(
							constrainableElement,
							constraintHelper
					)
			);
		}

		public BeanMetaDataImpl<T> build() {

			Set<ConstraintMetaData> aggregatedElements = newHashSet();

			for ( BuilderDelegate oneBuilder : builders ) {
				aggregatedElements.addAll(
						oneBuilder.build(
								( defaultGroupSequence != null && defaultGroupSequence.size() > 1 ) || defaultGroupSequenceProvider != null,
								defaultGroupSequence
						)
				);
			}

			return new BeanMetaDataImpl<T>(
					beanClass,
					defaultGroupSequence,
					defaultGroupSequenceProvider,
					aggregatedElements
			);
		}
	}

	private static class BuilderDelegate {

		private final ConstraintHelper constraintHelper;

		private MetaDataBuilder propertyBuilder;

		private MethodMetaData.Builder methodBuilder;

		public BuilderDelegate(ConstrainedElement constrainedElement, ConstraintHelper constraintHelper) {

			this.constraintHelper = constraintHelper;

			switch ( constrainedElement.getKind() ) {

				case FIELD:

					ConstrainedField constrainedField = (ConstrainedField) constrainedElement;
					propertyBuilder = new PropertyMetaData.Builder(
							constrainedField,
							constraintHelper
					);
					break;

				case METHOD:

					ConstrainedMethod constrainedMethod = (ConstrainedMethod) constrainedElement;
					methodBuilder = new MethodMetaData.Builder(
							constrainedMethod,
							constraintHelper
					);

					if ( constrainedMethod.isGetterMethod() ) {
						propertyBuilder = new PropertyMetaData.Builder(
								constrainedMethod,
								constraintHelper
						);
					}
					break;

				case TYPE:

					ConstrainedType constrainedType = (ConstrainedType) constrainedElement;
					propertyBuilder = new PropertyMetaData.Builder(
							constrainedType,
							constraintHelper
					);
					break;
			}
		}

		public boolean add(ConstrainedElement constrainedElement) {

			boolean added = false;

			if ( methodBuilder != null && methodBuilder.accepts( constrainedElement ) ) {
				methodBuilder.add( constrainedElement );
				added = true;
			}

			if ( propertyBuilder != null && propertyBuilder.accepts( constrainedElement ) ) {
				propertyBuilder.add( constrainedElement );

				if ( added == false && constrainedElement.getKind() == ConstrainedElementKind.METHOD && methodBuilder == null ) {
					ConstrainedMethod constrainedMethod = (ConstrainedMethod) constrainedElement;
					methodBuilder = new MethodMetaData.Builder(
							constrainedMethod,
							constraintHelper
					);
				}

				added = true;
			}

			return added;
		}

		public Set<ConstraintMetaData> build(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {

			Set<ConstraintMetaData> theValue = newHashSet();

			if ( propertyBuilder != null ) {
				theValue.add( propertyBuilder.build() );
			}

			if ( methodBuilder != null ) {
				theValue.add( methodBuilder.build() );
			}

			return theValue;
		}

	}
}
