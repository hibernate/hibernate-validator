/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ParameterNameProvider;

import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.internal.cfg.DefaultConstraintMapping;
import org.hibernate.validator.internal.cfg.context.ConfiguredConstraint;
import org.hibernate.validator.internal.cfg.context.ConstraintMappingContext;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.ConstraintOrigin;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ExecutableConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.util.CollectionHelper.Partitioner;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.CollectionHelper.partition;

/**
 * A {@link MetaDataProvider} based on the programmatic constraint API.
 *
 * @author Gunnar Morling
 */
public class ProgrammaticMetaDataProvider extends MetaDataProviderKeyedByClassName {

	private static final Log log = LoggerFactory.make();
	private final AnnotationProcessingOptions annotationProcessingOptions;
	private final ParameterNameProvider parameterNameProvider;

	public ProgrammaticMetaDataProvider(ConstraintHelper constraintHelper,
										ParameterNameProvider parameterNameProvider,
										Set<ConstraintMapping> programmaticMappings) {
		super( constraintHelper );
		Contracts.assertNotNull( programmaticMappings );
		this.parameterNameProvider = parameterNameProvider;
		ConstraintMappingContext mergedContext = createMergedMappingContext( programmaticMappings );
		initProgrammaticConfiguration( mergedContext );
		annotationProcessingOptions = mergedContext.getAnnotationProcessingOptions();
	}

	@Override
	public AnnotationProcessingOptions getAnnotationProcessingOptions() {
		return annotationProcessingOptions;
	}

	/**
	 * Reads the configuration from {@code context} and creates the appropriate meta-data structures.
	 *
	 * @param context the pre-processed configuration information
	 */
	private void initProgrammaticConfiguration(ConstraintMappingContext context) {
		for ( Class<?> clazz : context.getConfiguredClasses() ) {
			initClass( clazz, context );
		}
	}

	private <T> void initClass(Class<T> clazz, ConstraintMappingContext context) {
		Set<ConstrainedElement> constrainedElements =
				retrievePropertyMetaData(
						context.getConstraintConfig().get( clazz ),
						context.getCascadeConfig().get( clazz )
				);

		Set<ConstrainedElement> methodMetaData =
				retrieveMethodMetaData(
						context.getMethodCascadeConfig().get( clazz ),
						context.getMethodConstraintConfig().get( clazz )
				);

		constrainedElements.addAll( methodMetaData );

		DefaultGroupSequenceProvider<? super T> sequenceProvider = getDefaultGroupSequenceProvider( clazz, context );

		addBeanConfiguration(
				clazz,
				createBeanConfiguration(
						ConfigurationSource.API,
						clazz,
						constrainedElements,
						context.getDefaultSequence( clazz ),
						sequenceProvider
				)
		);
	}

	private <T> DefaultGroupSequenceProvider<? super T> getDefaultGroupSequenceProvider(Class<T> beanType, ConstraintMappingContext context) {
		Class<? extends DefaultGroupSequenceProvider<? super T>> providerClass = context.getDefaultGroupSequenceProvider(
				beanType
		);

		//retrieve provider from new annotation
		if ( providerClass != null ) {
			return ReflectionHelper.newInstance( providerClass, "default group sequence provider" );
		}

		return null;
	}

	private Set<ConstrainedElement> retrievePropertyMetaData(
			Set<ConfiguredConstraint<?, BeanConstraintLocation>> constraints,
			Set<BeanConstraintLocation> cascades) {
		Map<BeanConstraintLocation, Set<ConfiguredConstraint<?, BeanConstraintLocation>>> constraintsByLocation = partition(
				constraints,
				constraintsByLocation()
		);

		if ( cascades == null ) {
			cascades = Collections.emptySet();
		}

		Set<BeanConstraintLocation> allConfiguredProperties = new HashSet<BeanConstraintLocation>( cascades );
		allConfiguredProperties.addAll( constraintsByLocation.keySet() );

		Set<ConstrainedElement> allPropertyMetaData = newHashSet();
		for ( BeanConstraintLocation oneConfiguredProperty : allConfiguredProperties ) {
			if ( oneConfiguredProperty.getElementType() == ElementType.FIELD ) {
				allPropertyMetaData.add(
						new ConstrainedField(
								ConfigurationSource.API,
								oneConfiguredProperty,
								asMetaConstraints( constraintsByLocation.get( oneConfiguredProperty ) ),
								Collections.<Class<?>, Class<?>>emptyMap(),
								cascades.contains( oneConfiguredProperty )
						)
				);
			}
			else {
				allPropertyMetaData.add(
						new ConstrainedType(
								ConfigurationSource.API,
								oneConfiguredProperty,
								asMetaConstraints( constraintsByLocation.get( oneConfiguredProperty ) )
						)
				);
			}
		}
		return allPropertyMetaData;
	}

	private Set<ConstrainedElement> retrieveMethodMetaData(Set<ExecutableConstraintLocation> methodCascades,
														   Set<ConfiguredConstraint<?, ExecutableConstraintLocation>> methodConstraints) {
		Map<Method, Set<ExecutableConstraintLocation>> cascadesByMethod = partition(
				methodCascades, cascadesByMethod()
		);
		Map<Method, Set<ConfiguredConstraint<?, ExecutableConstraintLocation>>> constraintsByMethod = partition(
				methodConstraints, constraintsByMethod()
		);

		Set<Method> allConfiguredMethods = new HashSet<Method>( cascadesByMethod.keySet() );
		allConfiguredMethods.addAll( constraintsByMethod.keySet() );
		Set<ConstrainedElement> allMethodMetaData = newHashSet();

		for ( Method method : allConfiguredMethods ) {
			List<String> parameterNames = parameterNameProvider.getParameterNames( method );

			Map<Integer, Set<ExecutableConstraintLocation>> cascadesByParameter = partition(
					cascadesByMethod.get(
							method
					), cascadesByParameterIndex()
			);
			Map<Integer, Set<ConfiguredConstraint<?, ExecutableConstraintLocation>>> constraintsByParameter = partition(
					constraintsByMethod.get( method ), constraintsByParameterIndex()
			);
			List<ConstrainedParameter> parameterMetaDataList = newArrayList();

			for ( int i = 0; i < method.getParameterTypes().length; i++ ) {
				parameterMetaDataList.add(
						new ConstrainedParameter(
								ConfigurationSource.API,
								new ExecutableConstraintLocation( method, i ),
								parameterNames.get( i ),
								asMetaConstraints( constraintsByParameter.get( i ) ),
								Collections.<Class<?>, Class<?>>emptyMap(),
								cascadesByParameter.containsKey( i )
						)
				);
			}

			ConstrainedExecutable methodMetaData = new ConstrainedExecutable(
					ConfigurationSource.API,
					new ExecutableConstraintLocation( method ),
					parameterMetaDataList,
					Collections.<MetaConstraint<?>>emptySet(),
					asMetaConstraints( constraintsByParameter.get( null ) ),
					Collections.<Class<?>, Class<?>>emptyMap(),
					cascadesByParameter.containsKey( null )
			);
			allMethodMetaData.add( methodMetaData );
		}
		return allMethodMetaData;
	}

	private Set<MetaConstraint<?>> asMetaConstraints(Set<? extends ConfiguredConstraint<?, ?>> constraints) {
		if ( constraints == null ) {
			return Collections.emptySet();
		}

		Set<MetaConstraint<?>> theValue = newHashSet();

		for ( ConfiguredConstraint<?, ? extends ConstraintLocation> oneConfiguredConstraint : constraints ) {
			theValue.add( asMetaConstraint( oneConfiguredConstraint ) );
		}

		return theValue;
	}

	private <A extends Annotation> MetaConstraint<A> asMetaConstraint(ConfiguredConstraint<A, ? extends ConstraintLocation> config) {
		ConstraintDescriptorImpl<A> constraintDescriptor = new ConstraintDescriptorImpl<A>(
				config.getLocation().getMember(),
				config.createAnnotationProxy(),
				constraintHelper,
				config.getLocation().getElementType(),
				ConstraintOrigin.DEFINED_LOCALLY
		);

		return new MetaConstraint<A>( constraintDescriptor, config.getLocation() );
	}

	private Partitioner<Method, ExecutableConstraintLocation> cascadesByMethod() {
		return new Partitioner<Method, ExecutableConstraintLocation>() {
			@Override
			public Method getPartition(ExecutableConstraintLocation location) {
				return (Method) location.getMember();
			}
		};
	}

	private Partitioner<Integer, ExecutableConstraintLocation> cascadesByParameterIndex() {
		return new Partitioner<Integer, ExecutableConstraintLocation>() {
			@Override
			public Integer getPartition(ExecutableConstraintLocation location) {
				return location.getParameterIndex();
			}
		};
	}

	private Partitioner<Method, ConfiguredConstraint<?, ExecutableConstraintLocation>> constraintsByMethod() {
		return new Partitioner<Method, ConfiguredConstraint<?, ExecutableConstraintLocation>>() {
			@Override
			public Method getPartition(ConfiguredConstraint<?, ExecutableConstraintLocation> constraint) {
				return (Method) constraint.getLocation().getMember();
			}
		};
	}

	private Partitioner<Integer, ConfiguredConstraint<?, ExecutableConstraintLocation>> constraintsByParameterIndex() {
		return new Partitioner<Integer, ConfiguredConstraint<?, ExecutableConstraintLocation>>() {

			@Override
			public Integer getPartition(
					ConfiguredConstraint<?, ExecutableConstraintLocation> v) {
				return v.getLocation().getParameterIndex();
			}
		};
	}

	/**
	 * Creates a single merged {@code ConstraintMappingContext} in case multiple programmatic mappings are provided.
	 *
	 * @param programmaticMappings set of constraint mappings to merge into a single context
	 *
	 * @return a single merged constraint context
	 */
	private ConstraintMappingContext createMergedMappingContext(Set<ConstraintMapping> programmaticMappings) {
		// if we only have one mapping we can return the context of just this mapping
		if ( programmaticMappings.size() == 1 ) {
			return ( (DefaultConstraintMapping) programmaticMappings.iterator().next() ).getContext();
		}

		ConstraintMappingContext mergedContext = new ConstraintMappingContext();
		for ( ConstraintMapping mapping : programmaticMappings ) {
			ConstraintMappingContext context = ( (DefaultConstraintMapping) mapping ).getContext();

			mergedContext.getAnnotationProcessingOptions().merge( context.getAnnotationProcessingOptions() );

			for ( Set<ConfiguredConstraint<?, BeanConstraintLocation>> propertyConstraints : context.getConstraintConfig()
					.values() ) {
				for ( ConfiguredConstraint<?, BeanConstraintLocation> constraint : propertyConstraints ) {
					mergedContext.addConstraintConfig( constraint );
				}
			}

			for ( Set<BeanConstraintLocation> beanConstraintLocations : context.getCascadeConfig().values() ) {
				for ( BeanConstraintLocation beanLocation : beanConstraintLocations ) {
					mergedContext.addCascadeConfig( beanLocation );
				}
			}

			for ( Set<ConfiguredConstraint<?, ExecutableConstraintLocation>> methodConstraints : context.getMethodConstraintConfig()
					.values() ) {
				for ( ConfiguredConstraint<?, ExecutableConstraintLocation> methodConstraint : methodConstraints ) {
					mergedContext.addMethodConstraintConfig( methodConstraint );
				}
			}

			for ( Set<ExecutableConstraintLocation> cascadedMethodConstraints : context.getMethodCascadeConfig()
					.values() ) {
				for ( ExecutableConstraintLocation methodCascade : cascadedMethodConstraints ) {
					mergedContext.addMethodCascadeConfig( methodCascade );
				}
			}

			mergeGroupSequenceAndGroupSequenceProvider( mergedContext, context );
		}
		return mergedContext;
	}

	private void mergeGroupSequenceAndGroupSequenceProvider(ConstraintMappingContext mergedContext, ConstraintMappingContext context) {
		for ( Class<?> clazz : context.getConfiguredClasses() ) {
			mergeSequenceAndProviderForClass( mergedContext, context, clazz );
		}
	}

	private <T> void mergeSequenceAndProviderForClass(ConstraintMappingContext mergedContext, ConstraintMappingContext context, Class<T> clazz) {
		if ( context.getDefaultGroupSequenceProvider( clazz ) != null ) {
			if ( mergedContext.getDefaultGroupSequenceProvider( clazz ) != null ) {
				throw log.getMultipleDefinitionOfDefaultGroupSequenceProviderException();
			}
			mergedContext.addDefaultGroupSequenceProvider(
					clazz,
					context.getDefaultGroupSequenceProvider( clazz )
			);
		}
		if ( context.getDefaultSequence( clazz ) != null ) {
			if ( mergedContext.getDefaultSequence( clazz ) != null ) {
				throw log.getMultipleDefinitionOfDefaultGroupSequenceException();
			}
			mergedContext.addDefaultGroupSequence(
					clazz,
					context.getDefaultSequence( clazz )
			);
		}
	}

	private Partitioner<BeanConstraintLocation, ConfiguredConstraint<?, BeanConstraintLocation>> constraintsByLocation() {
		return new Partitioner<BeanConstraintLocation, ConfiguredConstraint<?, BeanConstraintLocation>>() {
			@Override
			public BeanConstraintLocation getPartition(ConfiguredConstraint<?, BeanConstraintLocation> constraint) {
				return constraint.getLocation();
			}
		};
	}
}
