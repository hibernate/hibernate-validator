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
import java.lang.reflect.Member;
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
import org.hibernate.validator.metadata.BeanMetaConstraint;
import org.hibernate.validator.metadata.ConstraintDescriptorImpl;
import org.hibernate.validator.metadata.ConstraintHelper;
import org.hibernate.validator.metadata.ConstraintOrigin;
import org.hibernate.validator.metadata.MethodMetaConstraint;
import org.hibernate.validator.metadata.MethodMetaData;
import org.hibernate.validator.metadata.ParameterMetaData;
import org.hibernate.validator.metadata.location.BeanConstraintLocation;
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

	/**
	 * Used as prefix for parameter names, if no explicit names are given.
	 */
	public static final String DEFAULT_PARAMETER_NAME_PREFIX = "arg";

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

			List<BeanConstraintLocation> cascades = context.getCascadeConfig().get( clazz );
			List<ConfiguredConstraint<?, BeanConstraintLocation>> constraints = context.getConstraintConfig()
					.get( clazz );
			List<MethodConstraintLocation> methodCascades = context.getMethodCascadeConfig().get( clazz );
			List<ConfiguredConstraint<?, MethodConstraintLocation>> methodConstraints = context.getMethodConstraintConfig()
					.get( clazz );

			Map<Method, List<MethodConstraintLocation>> cascadesByMethod = partition(
					methodCascades, cascadesByMethod()
			);
			Map<Method, List<ConfiguredConstraint<?, MethodConstraintLocation>>> constraintsByMethod = partition(
					methodConstraints, constraintsByMethod()
			);

			Set<Method> allConfiguredMethods = new HashSet<Method>( cascadesByMethod.keySet() );
			allConfiguredMethods.addAll( constraintsByMethod.keySet() );
			Set<MethodMetaData> allMethodMetaData = newHashSet();

			for ( Method oneMethod : allConfiguredMethods ) {

				Map<Integer, List<MethodConstraintLocation>> cascadesByParameter = partition(
						cascadesByMethod.get(
								oneMethod
						), cascadesByParameterIndex()
				);
				Map<Integer, List<ConfiguredConstraint<?, MethodConstraintLocation>>> constraintsByParameter = partition(
						constraintsByMethod.get( oneMethod ), constraintsByParameterIndex()
				);
				List<ParameterMetaData> parameterMetaDatas = newArrayList();

				int i = 0;
				for ( Class<?> parameterType : oneMethod.getParameterTypes() ) {
					String parameterName = DEFAULT_PARAMETER_NAME_PREFIX + i;
					boolean isCascading = cascadesByParameter.containsKey( i );

					parameterMetaDatas.add(
							new ParameterMetaData(
									i,
									parameterType,
									parameterName,
									asMethodMetaConstraints( constraintsByParameter.get( i ) ),
									isCascading
							)
					);

					i++;
				}


				MethodMetaData methodMetaData = new MethodMetaData(
						oneMethod,
						parameterMetaDatas,
						asMethodMetaConstraints( constraintsByParameter.get( null ) ),
						cascadesByParameter.containsKey( null )
				);
				allMethodMetaData.add( methodMetaData );
			}

			configuredBeans.put(
					clazz,
					createBeanConfiguration(
							clazz,
							asBeanMetaConstraints( constraints ),
							getMembers( cascades ),
							allMethodMetaData,
							context.getDefaultSequence( clazz ),
							context.getDefaultGroupSequenceProvider( clazz )
					)
			);
		}
	}

	private Set<BeanMetaConstraint<?>> asBeanMetaConstraints(List<ConfiguredConstraint<?, BeanConstraintLocation>> constraints) {

		Set<BeanMetaConstraint<?>> theValue = newHashSet();

		if ( constraints == null ) {
			return theValue;
		}

		for ( ConfiguredConstraint<?, BeanConstraintLocation> oneConfiguredConstraint : constraints ) {
			theValue.add( asBeanMetaConstraint( oneConfiguredConstraint ) );
		}

		return theValue;
	}

	private <A extends Annotation> BeanMetaConstraint<A> asBeanMetaConstraint(ConfiguredConstraint<A, BeanConstraintLocation> config) {

		ConstraintDescriptorImpl<A> constraintDescriptor = new ConstraintDescriptorImpl<A>(
				config.createAnnotationProxy(),
				constraintHelper,
				config.getLocation().getElementType(),
				ConstraintOrigin.DEFINED_LOCALLY
		);

		return new BeanMetaConstraint<A>( constraintDescriptor, config.getLocation() );
	}

	//TODO GM: use set
	private List<MethodMetaConstraint<?>> asMethodMetaConstraints(List<ConfiguredConstraint<?, MethodConstraintLocation>> constraints) {

		if ( constraints == null ) {
			return Collections.emptyList();
		}

		List<MethodMetaConstraint<?>> theValue = newArrayList();

		for ( ConfiguredConstraint<?, MethodConstraintLocation> oneConfiguredConstraint : constraints ) {
			theValue.add( asMethodMetaConstraint( oneConfiguredConstraint ) );
		}

		return theValue;
	}

	private <A extends Annotation> MethodMetaConstraint<A> asMethodMetaConstraint(ConfiguredConstraint<A, MethodConstraintLocation> config) {

		ConstraintDescriptorImpl<A> constraintDescriptor = new ConstraintDescriptorImpl<A>(
				config.createAnnotationProxy(), constraintHelper, ElementType.METHOD, ConstraintOrigin.DEFINED_LOCALLY
		);

		return new MethodMetaConstraint<A>( constraintDescriptor, config.getLocation() );
	}

	private Set<Member> getMembers(List<BeanConstraintLocation> beanConstraintLocations) {

		if ( beanConstraintLocations == null ) {
			return Collections.emptySet();
		}

		Set<Member> theValue = newHashSet();

		for ( BeanConstraintLocation oneLocation : beanConstraintLocations ) {
			theValue.add( oneLocation.getMember() );
		}

		return theValue;
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
