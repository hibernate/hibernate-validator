/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.annotation.ElementType;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.validation.ElementKind;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstructorDescriptor;
import javax.validation.metadata.MethodType;
import javax.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.internal.engine.groups.Sequence;
import org.hibernate.validator.internal.engine.groups.ValidationOrder;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.BeanDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ExecutableDescriptorImpl;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.CollectionHelper.Partitioner;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;
import org.hibernate.validator.internal.util.classhierarchy.Filters;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.CollectionHelper.partition;

/**
 * This class encapsulates all meta data needed for validation. Implementations of {@code Validator} interface can
 * instantiate an instance of this class and delegate the metadata extraction to it.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public final class BeanMetaDataImpl<T> implements BeanMetaData<T> {

	private static final Log log = LoggerFactory.make();

	/**
	 * Represents the "sequence" of just Default.class.
	 */
	private static final List<Class<?>> DEFAULT_GROUP_SEQUENCE = Collections.<Class<?>>singletonList( Default.class );

	private final ValidationOrderGenerator validationOrderGenerator;

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
	 * Contains constrained related meta data for all methods and constructors of the type represented by this bean meta
	 * data. Keyed by executable, values are an aggregated view on each executable together with all the executables
	 * from the inheritance hierarchy with the same signature.
	 * <p>
	 * An entry will be stored once under the signature of the represented method and all the methods it overrides
	 * (there will only be more than one entry in case of generics in the parameters, e.g. in case of a super-type
	 * method {@code foo(T)} and an overriding sub-type method {@code foo(String)} two entries for the same executable
	 * meta-data will be stored).
	 */
	private final Map<String, ExecutableMetaData> executableMetaDataMap;

	/**
	 * Property meta data keyed against the property name
	 */
	private final Map<String, PropertyMetaData> propertyMetaDataMap;

	/**
	 * The cascaded properties of this bean.
	 */
	private final Set<Cascadable> cascadedProperties;

	/**
	 * The bean descriptor for this bean.
	 */
	private final BeanDescriptor beanDescriptor;

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
	private DefaultGroupSequenceProvider<? super T> defaultGroupSequenceProvider;

	private ValidationOrder validationOrder;
	/**
	 * The class hierarchy for this class starting with the class itself going up the inheritance chain. Interfaces
	 * are not included.
	 */
	private final List<Class<? super T>> classHierarchyWithoutInterfaces;

	/**
	 * Creates a new {@link BeanMetaDataImpl}
	 *
	 * @param beanClass The Java type represented by this meta data object.
	 * @param defaultGroupSequence The default group sequence.
	 * @param defaultGroupSequenceProvider The default group sequence provider if set.
	 * @param constraintMetaDataSet All constraint meta data relating to the represented type.
	 */
	public BeanMetaDataImpl(Class<T> beanClass,
							List<Class<?>> defaultGroupSequence,
							DefaultGroupSequenceProvider<? super T> defaultGroupSequenceProvider,
							Set<ConstraintMetaData> constraintMetaDataSet,
							ValidationOrderGenerator validationOrderGenerator) {

		this.validationOrderGenerator = validationOrderGenerator;
		this.beanClass = beanClass;
		this.propertyMetaDataMap = newHashMap();

		Set<PropertyMetaData> propertyMetaDataSet = newHashSet();
		Set<ExecutableMetaData> executableMetaDataSet = newHashSet();

		for ( ConstraintMetaData constraintMetaData : constraintMetaDataSet ) {
			if ( constraintMetaData.getKind() == ElementKind.PROPERTY ) {
				propertyMetaDataSet.add( (PropertyMetaData) constraintMetaData );
			}
			else {
				executableMetaDataSet.add( (ExecutableMetaData) constraintMetaData );
			}
		}

		Set<Cascadable> cascadedProperties = newHashSet();
		Set<MetaConstraint<?>> allMetaConstraints = newHashSet();

		for ( PropertyMetaData propertyMetaData : propertyMetaDataSet ) {
			propertyMetaDataMap.put( propertyMetaData.getName(), propertyMetaData );

			if ( propertyMetaData.isCascading() ) {
				cascadedProperties.add( propertyMetaData );
			}
			else {
				allMetaConstraints.addAll( propertyMetaData.getTypeArgumentsConstraints() );
			}

			allMetaConstraints.addAll( propertyMetaData.getConstraints() );
		}

		this.cascadedProperties = Collections.unmodifiableSet( cascadedProperties );
		this.allMetaConstraints = Collections.unmodifiableSet( allMetaConstraints );

		this.classHierarchyWithoutInterfaces = ClassHierarchyHelper.getHierarchy(
				beanClass,
				Filters.excludeInterfaces()
		);

		setDefaultGroupSequenceOrProvider( defaultGroupSequence, defaultGroupSequenceProvider, validationOrderGenerator );

		this.directMetaConstraints = getDirectConstraints();

		this.executableMetaDataMap = Collections.unmodifiableMap( bySignature( executableMetaDataSet ) );

		this.beanDescriptor = new BeanDescriptorImpl(
				beanClass,
				getClassLevelConstraintsAsDescriptors(),
				getConstrainedPropertiesAsDescriptors(),
				getConstrainedMethodsAsDescriptors(),
				getConstrainedConstructorsAsDescriptors(),
				defaultGroupSequenceIsRedefined(),
				getDefaultGroupSequence( null )
		);
	}

	@Override
	public Class<T> getBeanClass() {
		return beanClass;
	}

	@Override
	public boolean hasConstraints() {
		if ( beanDescriptor.isBeanConstrained()
				|| !beanDescriptor.getConstrainedConstructors().isEmpty()
				|| !beanDescriptor.getConstrainedMethods( MethodType.NON_GETTER, MethodType.GETTER ).isEmpty() ) {
			return true;
		}

		return false;
	}

	@Override
	public BeanDescriptor getBeanDescriptor() {
		return beanDescriptor;
	}

	@Override
	public Set<Cascadable> getCascadables() {
		return cascadedProperties;
	}

	@Override
	public PropertyMetaData getMetaDataFor(String propertyName) {
		return propertyMetaDataMap.get( propertyName );
	}

	@Override
	public Set<MetaConstraint<?>> getMetaConstraints() {
		return allMetaConstraints;
	}

	@Override
	public Set<MetaConstraint<?>> getDirectMetaConstraints() {
		return directMetaConstraints;
	}

	@Override
	public ExecutableMetaData getMetaDataFor(ExecutableElement executable) {
		return executableMetaDataMap.get( executable.getSignature() );
	}

	@Override
	public List<Class<?>> getDefaultGroupSequence(T beanState) {
		if ( hasDefaultGroupSequenceProvider() ) {
			List<Class<?>> providerDefaultGroupSequence = defaultGroupSequenceProvider.getValidationGroups( beanState );
			return getValidDefaultGroupSequence( providerDefaultGroupSequence );
		}

		return Collections.unmodifiableList( defaultGroupSequence );
	}

	@Override
	public Iterator<Sequence> getDefaultValidationSequence(T beanState) {
		if ( hasDefaultGroupSequenceProvider() ) {
			List<Class<?>> providerDefaultGroupSequence = defaultGroupSequenceProvider.getValidationGroups( beanState );
			return validationOrderGenerator.getDefaultValidationOrder(
						beanClass,
						getValidDefaultGroupSequence( providerDefaultGroupSequence )
					)
					.getSequenceIterator();
		}
		else {
			return validationOrder.getSequenceIterator();
		}
	}

	@Override
	public boolean defaultGroupSequenceIsRedefined() {
		return defaultGroupSequence.size() > 1 || hasDefaultGroupSequenceProvider();
	}

	@Override
	public List<Class<? super T>> getClassHierarchy() {
		return classHierarchyWithoutInterfaces;
	}

	private Set<ConstraintDescriptorImpl<?>> getClassLevelConstraintsAsDescriptors() {
		Set<MetaConstraint<?>> classLevelConstraints = getClassLevelConstraints( allMetaConstraints );

		Set<ConstraintDescriptorImpl<?>> theValue = newHashSet();

		for ( MetaConstraint<?> metaConstraint : classLevelConstraints ) {
			theValue.add( metaConstraint.getDescriptor() );
		}

		return theValue;
	}

	private Map<String, PropertyDescriptor> getConstrainedPropertiesAsDescriptors() {
		Map<String, PropertyDescriptor> theValue = newHashMap();

		for ( Entry<String, PropertyMetaData> entry : propertyMetaDataMap.entrySet() ) {
			if ( entry.getValue().isConstrained() && entry.getValue().getName() != null ) {
				theValue.put(
						entry.getKey(),
						entry.getValue().asDescriptor(
								defaultGroupSequenceIsRedefined(),
								getDefaultGroupSequence( null )
						)
				);
			}
		}

		return theValue;
	}

	private Map<String, ExecutableDescriptorImpl> getConstrainedMethodsAsDescriptors() {
		Map<String, ExecutableDescriptorImpl> constrainedMethodDescriptors = newHashMap();

		for ( ExecutableMetaData executableMetaData : executableMetaDataMap.values() ) {
			if ( executableMetaData.getKind() == ElementKind.METHOD
					&& executableMetaData.isConstrained() ) {
				ExecutableDescriptorImpl descriptor = executableMetaData.asDescriptor(
								defaultGroupSequenceIsRedefined(),
								getDefaultGroupSequence( null )
						);

				for ( String signature : executableMetaData.getSignatures() ) {
					constrainedMethodDescriptors.put( signature, descriptor );
				}
			}
		}

		return constrainedMethodDescriptors;
	}

	private Map<String, ConstructorDescriptor> getConstrainedConstructorsAsDescriptors() {
		Map<String, ConstructorDescriptor> constrainedMethodDescriptors = newHashMap();

		for ( ExecutableMetaData executableMetaData : executableMetaDataMap.values() ) {
			if ( executableMetaData.getKind() == ElementKind.CONSTRUCTOR && executableMetaData.isConstrained() ) {
				constrainedMethodDescriptors.put(
						// constructors never override, so there will be exactly one identifier
						executableMetaData.getSignatures().iterator().next(),
						executableMetaData.asDescriptor(
								defaultGroupSequenceIsRedefined(),
								getDefaultGroupSequence( null )
						)
				);
			}
		}

		return constrainedMethodDescriptors;
	}

	private void setDefaultGroupSequenceOrProvider(List<Class<?>> defaultGroupSequence, DefaultGroupSequenceProvider<? super T> defaultGroupSequenceProvider, ValidationOrderGenerator validationOrderGenerator) {
		if ( defaultGroupSequence != null && defaultGroupSequenceProvider != null ) {
			throw log.getInvalidDefaultGroupSequenceDefinitionException();
		}

		if ( defaultGroupSequenceProvider != null ) {
			this.defaultGroupSequenceProvider = defaultGroupSequenceProvider;
			this.validationOrder = null;
		}
		else if ( defaultGroupSequence != null && !defaultGroupSequence.isEmpty() ) {
			setDefaultGroupSequence( defaultGroupSequence );
			this.validationOrder = validationOrderGenerator.getDefaultValidationOrder( beanClass, this.defaultGroupSequence );
		}
		else {
			this.defaultGroupSequence = DEFAULT_GROUP_SEQUENCE;
			this.validationOrder = ValidationOrder.DEFAULT_SEQUENCE;
		}
	}

	private Set<MetaConstraint<?>> getClassLevelConstraints(Set<MetaConstraint<?>> constraints) {
		Set<MetaConstraint<?>> classLevelConstraints = partition(
				constraints,
				byElementType()
		).get( ElementType.TYPE );

		return classLevelConstraints != null ? classLevelConstraints : Collections.<MetaConstraint<?>>emptySet();
	}

	private Set<MetaConstraint<?>> getDirectConstraints() {
		Set<MetaConstraint<?>> constraints = newHashSet();

		Set<Class<?>> classAndInterfaces = newHashSet();
		classAndInterfaces.add( beanClass );
		classAndInterfaces.addAll( ClassHierarchyHelper.getDirectlyImplementedInterfaces( beanClass ) );

		for ( Class<?> clazz : classAndInterfaces ) {
			for ( MetaConstraint<?> metaConstraint : allMetaConstraints ) {
				if ( metaConstraint.getLocation().getDeclaringClass().equals( clazz ) ) {
					constraints.add( metaConstraint );
				}
			}
		}

		return Collections.unmodifiableSet( constraints );
	}

	/**
	 * Builds up the method meta data for this type; each meta-data entry will be stored under the signature of the
	 * represented method and all the methods it overrides.
	 */
	private Map<String, ExecutableMetaData> bySignature(Set<ExecutableMetaData> executables) {
		Map<String, ExecutableMetaData> theValue = newHashMap();

		for ( ExecutableMetaData executableMetaData : executables ) {
			for ( String signature : executableMetaData.getSignatures() ) {
				theValue.put( signature, executableMetaData );
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
					throw log.getNoDefaultGroupInGroupSequenceException();
				}
				else {
					validDefaultGroupSequence.add( group );
				}
			}
		}
		if ( !groupSequenceContainsDefault ) {
			throw log.getBeanClassMustBePartOfRedefinedDefaultGroupSequenceException( beanClass.getName() );
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

	private Partitioner<ElementType, MetaConstraint<?>> byElementType() {
		return new Partitioner<ElementType, MetaConstraint<?>>() {
			@Override
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
		sb.append( ", cascadedPropertiesCount=" ).append( cascadedProperties.size() );
		sb.append( ", defaultGroupSequence=" ).append( getDefaultGroupSequence( null ) );
		sb.append( '}' );
		return sb.toString();
	}

	public static class BeanMetaDataBuilder<T> {

		private final ConstraintHelper constraintHelper;

		private final ValidationOrderGenerator validationOrderGenerator;

		private final Class<T> beanClass;

		private final Set<BuilderDelegate> builders = newHashSet();

		private final ExecutableHelper executableHelper;

		private ConfigurationSource sequenceSource;

		private ConfigurationSource providerSource;

		private List<Class<?>> defaultGroupSequence;

		private DefaultGroupSequenceProvider<? super T> defaultGroupSequenceProvider;

		private BeanMetaDataBuilder(
				ConstraintHelper constraintHelper,
				ExecutableHelper executableHelper,
				ValidationOrderGenerator validationOrderGenerator,
				Class<T> beanClass) {
			this.beanClass = beanClass;
			this.constraintHelper = constraintHelper;
			this.validationOrderGenerator = validationOrderGenerator;
			this.executableHelper = executableHelper;
		}

		public static <T> BeanMetaDataBuilder<T> getInstance(
				ConstraintHelper constraintHelper,
				ExecutableHelper executableHelper,
				ValidationOrderGenerator validationOrderGenerator,
				Class<T> beanClass) {
			return new BeanMetaDataBuilder<T>(
					constraintHelper,
					executableHelper,
					validationOrderGenerator,
					beanClass
			);
		}

		public void add(BeanConfiguration<? super T> configuration) {
			if ( configuration.getBeanClass().equals( beanClass ) ) {
				if ( configuration.getDefaultGroupSequence() != null
						&& ( sequenceSource == null || configuration.getSource()
						.getPriority() >= sequenceSource.getPriority() ) ) {

					sequenceSource = configuration.getSource();
					defaultGroupSequence = configuration.getDefaultGroupSequence();
				}

				if ( configuration.getDefaultGroupSequenceProvider() != null
						&& ( providerSource == null || configuration.getSource()
						.getPriority() >= providerSource.getPriority() ) ) {

					providerSource = configuration.getSource();
					defaultGroupSequenceProvider = configuration.getDefaultGroupSequenceProvider();
				}
			}

			for ( ConstrainedElement constrainedElement : configuration.getConstrainedElements() ) {
				addMetaDataToBuilder( constrainedElement, builders );
			}
		}

		private void addMetaDataToBuilder(ConstrainedElement constrainableElement, Set<BuilderDelegate> builders) {
			for ( BuilderDelegate builder : builders ) {
				boolean foundBuilder = builder.add( constrainableElement );

				if ( foundBuilder ) {
					return;
				}
			}

			builders.add(
					new BuilderDelegate(
							beanClass,
							constrainableElement,
							constraintHelper,
							executableHelper
					)
			);
		}

		public BeanMetaDataImpl<T> build() {
			Set<ConstraintMetaData> aggregatedElements = newHashSet();

			for ( BuilderDelegate builder : builders ) {
				aggregatedElements.addAll( builder.build() );
			}

			return new BeanMetaDataImpl<T>(
					beanClass,
					defaultGroupSequence,
					defaultGroupSequenceProvider,
					aggregatedElements,
					validationOrderGenerator
			);
		}
	}

	private static class BuilderDelegate {
		private final Class<?> beanClass;
		private final ConstraintHelper constraintHelper;
		private final ExecutableHelper executableHelper;
		private MetaDataBuilder propertyBuilder;
		private ExecutableMetaData.Builder methodBuilder;

		public BuilderDelegate(Class<?> beanClass, ConstrainedElement constrainedElement, ConstraintHelper constraintHelper, ExecutableHelper executableHelper) {
			this.beanClass = beanClass;
			this.constraintHelper = constraintHelper;
			this.executableHelper = executableHelper;

			switch ( constrainedElement.getKind() ) {
				case FIELD:
					ConstrainedField constrainedField = (ConstrainedField) constrainedElement;
					propertyBuilder = new PropertyMetaData.Builder(
							beanClass,
							constrainedField,
							constraintHelper
					);
					break;
				case CONSTRUCTOR:
				case METHOD:
					ConstrainedExecutable constrainedExecutable = (ConstrainedExecutable) constrainedElement;
					Member member = constrainedExecutable.getExecutable().getMember();

					// HV-890 Not adding meta-data for private super-type methods to the method meta-data of this bean;
					// It is not needed and it may conflict with sub-type methods of the same signature
					if ( !Modifier.isPrivate( member.getModifiers() ) || beanClass == member.getDeclaringClass() ) {
						methodBuilder = new ExecutableMetaData.Builder(
								beanClass,
								constrainedExecutable,
								constraintHelper,
								executableHelper
						);
					}

					if ( constrainedExecutable.isGetterMethod() ) {
						propertyBuilder = new PropertyMetaData.Builder(
								beanClass,
								constrainedExecutable,
								constraintHelper
						);
					}
					break;
				case TYPE:
					ConstrainedType constrainedType = (ConstrainedType) constrainedElement;
					propertyBuilder = new PropertyMetaData.Builder(
							beanClass,
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

				if ( !added && constrainedElement.getKind() == ConstrainedElementKind.METHOD && methodBuilder == null ) {
					ConstrainedExecutable constrainedMethod = (ConstrainedExecutable) constrainedElement;
					methodBuilder = new ExecutableMetaData.Builder(
							beanClass,
							constrainedMethod,
							constraintHelper,
							executableHelper
					);
				}

				added = true;
			}

			return added;
		}

		public Set<ConstraintMetaData> build() {
			Set<ConstraintMetaData> metaDataSet = newHashSet();

			if ( propertyBuilder != null ) {
				metaDataSet.add( propertyBuilder.build() );
			}

			if ( methodBuilder != null ) {
				metaDataSet.add( methodBuilder.build() );
			}

			return metaDataSet;
		}
	}
}
