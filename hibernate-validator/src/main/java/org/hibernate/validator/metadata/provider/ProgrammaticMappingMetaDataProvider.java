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
package org.hibernate.validator.metadata.provider;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.context.impl.ConfiguredConstraint;
import org.hibernate.validator.cfg.context.impl.ConstraintMappingContext;
import org.hibernate.validator.metadata.AnnotationIgnores;
import org.hibernate.validator.metadata.ConstrainableElement;
import org.hibernate.validator.metadata.MetaConstraint;
import org.hibernate.validator.metadata.ConstraintDescriptorImpl;
import org.hibernate.validator.metadata.ConstraintHelper;
import org.hibernate.validator.metadata.ConstraintOrigin;
import org.hibernate.validator.metadata.MethodMetaData;
import org.hibernate.validator.metadata.ParameterMetaData;
import org.hibernate.validator.metadata.ConstrainedField;
import org.hibernate.validator.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.metadata.location.ConstraintLocation;
import org.hibernate.validator.metadata.location.MethodConstraintLocation;
import org.hibernate.validator.util.CollectionHelper.Partitioner;

import static org.hibernate.validator.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.util.CollectionHelper.partition;

/**
 * A {@link MetaDataProvider} based on the programmatic constraint API.
 *
 * @author Gunnar Morling
 */
public class ProgrammaticMappingMetaDataProvider extends MetaDataProviderImplBase {

	public ProgrammaticMappingMetaDataProvider(ConstraintHelper constraintHelper, ConstraintMapping mapping) {

		super( constraintHelper );

		initProgrammaticConfiguration( mapping );
	}

	public AnnotationIgnores getAnnotationIgnores() {
		return null;
	}

	/**
	 * Reads the configuration from {@code mapping} and creates the appropriate meta-data structures.
	 *
	 * @param mapping The constraint configuration created via the programmatic API.
	 */
	private <T> void initProgrammaticConfiguration(ConstraintMapping mapping) {

		ConstraintMappingContext context = ConstraintMappingContext.getFromMapping( mapping );

		for ( Class<?> clazz : context.getConfiguredClasses() ) {

			Set<ConstrainableElement> constrainedElements =
					retrievePropertyMetaData(
							context.getConstraintConfig().get( clazz ),
							context.getCascadeConfig().get( clazz )
					);

			Set<ConstrainableElement> methodMetaData =
					retrieveMethodMetaData(
							context.getMethodCascadeConfig().get( clazz ),
							context.getMethodConstraintConfig().get( clazz )
					);

			constrainedElements.addAll(methodMetaData);
			
			configuredBeans.put(
					clazz,
					createBeanConfiguration(
							clazz,
							constrainedElements,
							context.getDefaultSequence( clazz ),
							context.getDefaultGroupSequenceProvider( clazz )
					)
			);
		}
	}

	private Set<ConstrainableElement> retrievePropertyMetaData(
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

		Set<ConstrainableElement> allPropertyMetaData = newHashSet();
		for ( BeanConstraintLocation oneConfiguredProperty : allConfiguredProperties ) {
			allPropertyMetaData.add(
					new ConstrainedField(
							asMetaConstraints( constraintsByLocation.get( oneConfiguredProperty ) ),
							oneConfiguredProperty,
							cascades.contains( oneConfiguredProperty )
					)
			);
		}
		return allPropertyMetaData;
	}

	private Set<ConstrainableElement> retrieveMethodMetaData(Set<MethodConstraintLocation> methodCascades, Set<ConfiguredConstraint<?, MethodConstraintLocation>> methodConstraints) {

		Map<Method, Set<MethodConstraintLocation>> cascadesByMethod = partition(
				methodCascades, cascadesByMethod()
		);
		Map<Method, Set<ConfiguredConstraint<?, MethodConstraintLocation>>> constraintsByMethod = partition(
				methodConstraints, constraintsByMethod()
		);

		Set<Method> allConfiguredMethods = new HashSet<Method>( cascadesByMethod.keySet() );
		allConfiguredMethods.addAll( constraintsByMethod.keySet() );
		Set<ConstrainableElement> allMethodMetaData = newHashSet();

		for ( Method oneMethod : allConfiguredMethods ) {

			Map<Integer, Set<MethodConstraintLocation>> cascadesByParameter = partition(
					cascadesByMethod.get(
							oneMethod
					), cascadesByParameterIndex()
			);
			Map<Integer, Set<ConfiguredConstraint<?, MethodConstraintLocation>>> constraintsByParameter = partition(
					constraintsByMethod.get( oneMethod ), constraintsByParameterIndex()
			);
			List<ParameterMetaData> parameterMetaDatas = newArrayList();

			int i = 0;
			for ( Class<?> parameterType : oneMethod.getParameterTypes() ) {
				String parameterName = DEFAULT_PARAMETER_NAME_PREFIX + i;
				boolean isCascading = cascadesByParameter.containsKey( i );

				parameterMetaDatas.add(
						new ParameterMetaData(
								new MethodConstraintLocation(oneMethod, i),
								parameterName,
								asMetaConstraints( constraintsByParameter.get( i ) ),
								isCascading
						)
				);

				i++;
			}

			MethodMetaData methodMetaData = new MethodMetaData(
					oneMethod,
					parameterMetaDatas,
					asMetaConstraints( constraintsByParameter.get( null ) ),
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
				config.createAnnotationProxy(),
				constraintHelper,
				config.getLocation().getElementType(),
				ConstraintOrigin.DEFINED_LOCALLY
		);

		return new MetaConstraint<A>( constraintDescriptor, config.getLocation() );
	}

	private Partitioner<Method, MethodConstraintLocation> cascadesByMethod() {
		return new Partitioner<Method, MethodConstraintLocation>() {
			public Method getPartition(MethodConstraintLocation location) {
				return location.getMethod();
			}
		};
	}

	private Partitioner<Integer, MethodConstraintLocation> cascadesByParameterIndex() {
		return new Partitioner<Integer, MethodConstraintLocation>() {
			public Integer getPartition(MethodConstraintLocation location) {
				return location.getParameterIndex();
			}
		};
	}

	private Partitioner<Method, ConfiguredConstraint<?, MethodConstraintLocation>> constraintsByMethod() {
		return new Partitioner<Method, ConfiguredConstraint<?, MethodConstraintLocation>>() {
			public Method getPartition(ConfiguredConstraint<?, MethodConstraintLocation> constraint) {
				return constraint.getLocation().getMethod();
			}
		};
	}

	private Partitioner<Integer, ConfiguredConstraint<?, MethodConstraintLocation>> constraintsByParameterIndex() {
		return new Partitioner<Integer, ConfiguredConstraint<?, MethodConstraintLocation>>() {

			public Integer getPartition(
					ConfiguredConstraint<?, MethodConstraintLocation> v) {

				return v.getLocation().getParameterIndex();
			}
		};
	}

}
