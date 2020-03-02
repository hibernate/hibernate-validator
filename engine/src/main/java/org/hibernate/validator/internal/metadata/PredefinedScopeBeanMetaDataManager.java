/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

import java.lang.annotation.ElementType;
import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.ConstructorDescriptor;
import jakarta.validation.metadata.ElementDescriptor.ConstraintFinder;
import jakarta.validation.metadata.MethodDescriptor;
import jakarta.validation.metadata.MethodType;
import jakarta.validation.metadata.PropertyDescriptor;
import jakarta.validation.metadata.Scope;

import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.engine.MethodValidationConfiguration;
import org.hibernate.validator.internal.engine.groups.Sequence;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataBuilder;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataImpl;
import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;
import org.hibernate.validator.internal.metadata.aggregated.PropertyMetaData;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.provider.AnnotationMetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;
import org.hibernate.validator.internal.util.classhierarchy.Filters;
import org.hibernate.validator.metadata.BeanMetaDataClassNormalizer;

public class PredefinedScopeBeanMetaDataManager implements BeanMetaDataManager {

	private final BeanMetaDataClassNormalizer beanMetaDataClassNormalizer;

	/**
	 * Used to cache the constraint meta data for validated entities.
	 */
	private final ConcurrentMap<Class<?>, BeanMetaData<?>> beanMetaDataMap = new ConcurrentHashMap<>();

	public PredefinedScopeBeanMetaDataManager(ConstraintCreationContext constraintCreationContext,
			ExecutableHelper executableHelper,
			ExecutableParameterNameProvider parameterNameProvider,
			JavaBeanHelper javaBeanHelper,
			ValidationOrderGenerator validationOrderGenerator,
			List<MetaDataProvider> optionalMetaDataProviders,
			MethodValidationConfiguration methodValidationConfiguration,
			BeanMetaDataClassNormalizer beanMetaDataClassNormalizer,
			Set<Class<?>> beanClassesToInitialize) {
		AnnotationProcessingOptions annotationProcessingOptions = getAnnotationProcessingOptionsFromNonDefaultProviders( optionalMetaDataProviders );
		AnnotationMetaDataProvider defaultProvider = new AnnotationMetaDataProvider(
				constraintCreationContext,
				javaBeanHelper,
				annotationProcessingOptions
		);

		List<MetaDataProvider> metaDataProviders = new ArrayList<>( optionalMetaDataProviders.size() + 1 );
		// We add the annotation based metadata provider at the first position so that the entire metadata model is assembled
		// first.
		// The other optional metadata providers will then contribute their additional metadata to the preexisting model.
		// This helps to mitigate issues like HV-1450.
		metaDataProviders.add( defaultProvider );
		metaDataProviders.addAll( optionalMetaDataProviders );

		for ( Class<?> validatedClass : beanClassesToInitialize ) {
			Class<?> normalizedValidatedClass = beanMetaDataClassNormalizer.normalize( validatedClass );

			@SuppressWarnings("unchecked")
			List<Class<?>> classHierarchy = (List<Class<?>>) (Object) ClassHierarchyHelper.getHierarchy( normalizedValidatedClass, Filters.excludeInterfaces() );

			// note that the hierarchy also contains the initial class
			for ( Class<?> hierarchyElement : classHierarchy ) {
				if ( this.beanMetaDataMap.containsKey( hierarchyElement ) ) {
					continue;
				}

				this.beanMetaDataMap.put( hierarchyElement,
						createBeanMetaData( constraintCreationContext, executableHelper, parameterNameProvider,
								javaBeanHelper, validationOrderGenerator, optionalMetaDataProviders, methodValidationConfiguration,
								metaDataProviders, hierarchyElement ) );
			}
		}

		this.beanMetaDataClassNormalizer = beanMetaDataClassNormalizer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> BeanMetaData<T> getBeanMetaData(Class<T> beanClass) {
		Class<?> normalizedBeanClass = beanMetaDataClassNormalizer.normalize( beanClass );
		BeanMetaData<T> beanMetaData = (BeanMetaData<T>) beanMetaDataMap.get( normalizedBeanClass );
		if ( beanMetaData == null ) {
			// note that if at least one element of the hierarchy is constrained, the child classes should really be initialized
			// otherwise they will be considered unconstrained.
			beanMetaData = (BeanMetaData<T>) beanMetaDataMap.computeIfAbsent( normalizedBeanClass, UninitializedBeanMetaData::new );
		}
		return beanMetaData;
	}

	@Override
	public void clear() {
		beanMetaDataMap.clear();
	}

	/**
	 * Creates a {@link org.hibernate.validator.internal.metadata.aggregated.BeanMetaData} containing the meta data from all meta
	 * data providers for the given type and its hierarchy.
	 *
	 * @param <T> The type of interest.
	 * @param clazz The type's class.
	 *
	 * @return A bean meta data object for the given type.
	 */
	private static <T> BeanMetaDataImpl<T> createBeanMetaData(ConstraintCreationContext constraintCreationContext,
			ExecutableHelper executableHelper,
			ExecutableParameterNameProvider parameterNameProvider,
			JavaBeanHelper javaBeanHelper,
			ValidationOrderGenerator validationOrderGenerator,
			List<MetaDataProvider> optionalMetaDataProviders,
			MethodValidationConfiguration methodValidationConfiguration,
			List<MetaDataProvider> metaDataProviders,
			Class<T> clazz) {
		BeanMetaDataBuilder<T> builder = BeanMetaDataBuilder.getInstance(
				constraintCreationContext, executableHelper, parameterNameProvider,
				validationOrderGenerator, clazz, methodValidationConfiguration );

		for ( MetaDataProvider provider : metaDataProviders ) {
			for ( BeanConfiguration<? super T> beanConfiguration : getBeanConfigurationForHierarchy( provider, clazz ) ) {
				builder.add( beanConfiguration );
			}
		}

		return builder.build();
	}

	/**
	 * @return returns the annotation ignores from the non annotation based meta data providers
	 */
	private static AnnotationProcessingOptions getAnnotationProcessingOptionsFromNonDefaultProviders(List<MetaDataProvider> optionalMetaDataProviders) {
		AnnotationProcessingOptions options = new AnnotationProcessingOptionsImpl();
		for ( MetaDataProvider metaDataProvider : optionalMetaDataProviders ) {
			options.merge( metaDataProvider.getAnnotationProcessingOptions() );
		}

		return options;
	}

	/**
	 * Returns a list with the configurations for all types contained in the given type's hierarchy (including
	 * implemented interfaces) starting at the specified type.
	 *
	 * @param beanClass The type of interest.
	 * @param <T> The type of the class to get the configurations for.
	 * @return A set with the configurations for the complete hierarchy of the given type. May be empty, but never
	 * {@code null}.
	 */
	private static <T> List<BeanConfiguration<? super T>> getBeanConfigurationForHierarchy(MetaDataProvider provider, Class<T> beanClass) {
		List<BeanConfiguration<? super T>> configurations = newArrayList();

		for ( Class<? super T> clazz : ClassHierarchyHelper.getHierarchy( beanClass ) ) {
			BeanConfiguration<? super T> configuration = provider.getBeanConfiguration( clazz );
			if ( configuration != null ) {
				configurations.add( configuration );
			}
		}

		return configurations;
	}

	private static class UninitializedBeanMetaData<T> implements BeanMetaData<T> {

		private final Class<T> beanClass;

		private final BeanDescriptor beanDescriptor;

		private final List<Class<? super T>> classHierarchy;

		@SuppressWarnings("unchecked")
		private UninitializedBeanMetaData(Class<T> beanClass) {
			this.beanClass = beanClass;
			this.classHierarchy = (List<Class<? super T>>) (Object) ClassHierarchyHelper.getHierarchy( beanClass, Filters.excludeInterfaces() );
			this.beanDescriptor = new UninitializedBeanDescriptor( beanClass );
		}

		@Override
		public Iterable<Cascadable> getCascadables() {
			return Collections.emptyList();
		}

		@Override
		public boolean hasCascadables() {
			return false;
		}

		@Override
		public Class<T> getBeanClass() {
			return beanClass;
		}

		@Override
		public boolean hasConstraints() {
			return false;
		}

		@Override
		public BeanDescriptor getBeanDescriptor() {
			return beanDescriptor;
		}

		@Override
		public PropertyMetaData getMetaDataFor(String propertyName) {
			throw new IllegalStateException( "Metadata has not been initialized for bean of type " + beanClass.getName() );
		}

		@Override
		public List<Class<?>> getDefaultGroupSequence(T beanState) {
			throw new IllegalStateException( "Metadata has not been initialized for bean of type " + beanClass.getName() );
		}

		@Override
		public Iterator<Sequence> getDefaultValidationSequence(T beanState) {
			throw new IllegalStateException( "Metadata has not been initialized for bean of type " + beanClass.getName() );
		}

		@Override
		public boolean isDefaultGroupSequenceRedefined() {
			return false;
		}

		@Override
		public Set<MetaConstraint<?>> getMetaConstraints() {
			return Collections.emptySet();
		}

		@Override
		public Set<MetaConstraint<?>> getDirectMetaConstraints() {
			return Collections.emptySet();
		}

		@Override
		public Optional<ExecutableMetaData> getMetaDataFor(Executable executable) throws IllegalArgumentException {
			return Optional.empty();
		}

		@Override
		public List<Class<? super T>> getClassHierarchy() {
			return classHierarchy;
		}
	}

	private static class UninitializedBeanDescriptor implements BeanDescriptor {

		private final Class<?> elementClass;

		private UninitializedBeanDescriptor(Class<?> elementClass) {
			this.elementClass = elementClass;
		}

		@Override
		public boolean hasConstraints() {
			return false;
		}

		@Override
		public Class<?> getElementClass() {
			return elementClass;
		}

		@Override
		public Set<ConstraintDescriptor<?>> getConstraintDescriptors() {
			return Collections.emptySet();
		}

		@Override
		public ConstraintFinder findConstraints() {
			return UninitializedConstaintFinder.INSTANCE;
		}

		@Override
		public boolean isBeanConstrained() {
			return false;
		}

		@Override
		public PropertyDescriptor getConstraintsForProperty(String propertyName) {
			return null;
		}

		@Override
		public Set<PropertyDescriptor> getConstrainedProperties() {
			return Collections.emptySet();
		}

		@Override
		public MethodDescriptor getConstraintsForMethod(String methodName, Class<?>... parameterTypes) {
			return null;
		}

		@Override
		public Set<MethodDescriptor> getConstrainedMethods(MethodType methodType, MethodType... methodTypes) {
			return Collections.emptySet();
		}

		@Override
		public ConstructorDescriptor getConstraintsForConstructor(Class<?>... parameterTypes) {
			return null;
		}

		@Override
		public Set<ConstructorDescriptor> getConstrainedConstructors() {
			return Collections.emptySet();
		}
	}

	private static class UninitializedConstaintFinder implements ConstraintFinder {

		private static final UninitializedConstaintFinder INSTANCE = new UninitializedConstaintFinder();

		@Override
		public ConstraintFinder unorderedAndMatchingGroups(Class<?>... groups) {
			return this;
		}

		@Override
		public ConstraintFinder lookingAt(Scope scope) {
			return this;
		}

		@Override
		public ConstraintFinder declaredOn(ElementType... types) {
			return this;
		}

		@Override
		public Set<ConstraintDescriptor<?>> getConstraintDescriptors() {
			return Collections.emptySet();
		}

		@Override
		public boolean hasConstraints() {
			return false;
		}
	}
}
