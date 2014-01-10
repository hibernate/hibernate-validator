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
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.validation.ElementKind;
import javax.validation.metadata.ParameterDescriptor;

import org.hibernate.validator.internal.metadata.aggregated.rule.MethodConfigurationRule;
import org.hibernate.validator.internal.metadata.aggregated.rule.OverridingMethodMustNotAlterParameterConstraints;
import org.hibernate.validator.internal.metadata.aggregated.rule.ParallelMethodsMustNotDefineGroupConversionForCascadedReturnValue;
import org.hibernate.validator.internal.metadata.aggregated.rule.ParallelMethodsMustNotDefineParameterConstraints;
import org.hibernate.validator.internal.metadata.aggregated.rule.ReturnValueMayOnlyBeMarkedOnceAsCascadedPerHierarchyLine;
import org.hibernate.validator.internal.metadata.aggregated.rule.VoidMethodsMustNotBeReturnValueConstrained;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ExecutableDescriptorImpl;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * An aggregated view of the constraint related meta data for a given method or
 * constructors and in (case of methods) all the methods in the inheritance
 * hierarchy which it overrides or implements.
 * <p>
 * Instances are retrieved by creating a {@link Builder} and adding all required
 * {@link ConstrainedExecutable} objects to it. Instances are read-only after
 * creation.
 * </p>
 * <p>
 * Identity is solely based on the method's name and parameter types, hence sets
 * and similar collections of this type may only be created in the scope of one
 * Java type.
 * </p>
 *
 * @author Gunnar Morling
 */
public class ExecutableMetaData extends AbstractConstraintMetaData {

	private static final Log log = LoggerFactory.make();

	private final Class<?>[] parameterTypes;
	private final List<ParameterMetaData> parameterMetaDataList;
	private final Set<MetaConstraint<?>> crossParameterConstraints;
	private final boolean isGetter;

	/**
	 * An identifier for storing this object in maps etc.
	 */
	private final String identifier;

	private final ReturnValueMetaData returnValueMetaData;

	private ExecutableMetaData(
			String name,
			Type returnType,
			Class<?>[] parameterTypes,
			ElementKind kind,
			Set<MetaConstraint<?>> returnValueConstraints,
			List<ParameterMetaData> parameterMetaData,
			Set<MetaConstraint<?>> crossParameterConstraints,
			Map<Class<?>, Class<?>> returnValueGroupConversions,
			boolean isCascading,
			boolean isConstrained,
			boolean isGetter,
			boolean requiresUnwrapping) {
		super(
				name,
				returnType,
				returnValueConstraints,
				kind,
				isCascading,
				isConstrained,
				requiresUnwrapping
		);

		this.parameterTypes = parameterTypes;
		this.parameterMetaDataList = Collections.unmodifiableList( parameterMetaData );
		this.crossParameterConstraints = Collections.unmodifiableSet( crossParameterConstraints );
		this.identifier = name + Arrays.toString( parameterTypes );
		this.returnValueMetaData = new ReturnValueMetaData(
				returnType,
				returnValueConstraints,
				isCascading,
				returnValueGroupConversions,
				requiresUnwrapping
		);
		this.isGetter = isGetter;
	}

	/**
	 * Returns meta data for the specified parameter of the represented executable.
	 *
	 * @param parameterIndex the index of the parameter
	 *
	 * @return Meta data for the specified parameter. Will never be {@code null}.
	 */
	public ParameterMetaData getParameterMetaData(int parameterIndex) {
		if ( parameterIndex < 0 || parameterIndex > parameterMetaDataList.size() - 1 ) {
			throw log.getInvalidExecutableParameterIndexException(
					ExecutableElement.getExecutableAsString(
							getType().toString() + "#" + getName(),
							parameterTypes
					), parameterTypes.length
			);
		}

		return parameterMetaDataList.get( parameterIndex );
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	/**
	 * Returns an identifier for this meta data object, based on the represented
	 * executable's name and its parameter types.
	 *
	 * @return An identifier for this meta data object.
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the cross-parameter constraints declared for the represented
	 * method or constructor.
	 *
	 * @return the cross-parameter constraints declared for the represented
	 *         method or constructor. May be empty but will never be
	 *         {@code null}.
	 */
	public Set<MetaConstraint<?>> getCrossParameterConstraints() {
		return crossParameterConstraints;
	}

	public ValidatableParametersMetaData getValidatableParametersMetaData() {
		Set<ParameterMetaData> cascadedParameters = newHashSet();

		for ( ParameterMetaData parameterMetaData : parameterMetaDataList ) {
			if ( parameterMetaData.isCascading() ) {
				cascadedParameters.add( parameterMetaData );
			}
		}

		return new ValidatableParametersMetaData( cascadedParameters );
	}

	public ReturnValueMetaData getReturnValueMetaData() {
		return returnValueMetaData;
	}

	@Override
	public ExecutableDescriptorImpl asDescriptor(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		return new ExecutableDescriptorImpl(
				getType(),
				getName(),
				asDescriptors( getCrossParameterConstraints() ),
				returnValueMetaData.asDescriptor(
						defaultGroupSequenceRedefined,
						defaultGroupSequence
				),
				parametersAsDescriptors( defaultGroupSequenceRedefined, defaultGroupSequence ),
				defaultGroupSequenceRedefined,
				isGetter,
				defaultGroupSequence
		);
	}

	private List<ParameterDescriptor> parametersAsDescriptors(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		List<ParameterDescriptor> parameterDescriptorList = newArrayList();

		for ( ParameterMetaData parameterMetaData : parameterMetaDataList ) {
			parameterDescriptorList.add(
					parameterMetaData.asDescriptor(
							defaultGroupSequenceRedefined,
							defaultGroupSequence
					)
			);
		}

		return parameterDescriptorList;
	}

	@Override
	public String toString() {
		StringBuilder parameterBuilder = new StringBuilder();

		for ( Class<?> oneParameterType : getParameterTypes() ) {
			parameterBuilder.append( oneParameterType.getSimpleName() );
			parameterBuilder.append( ", " );
		}

		String parameters =
				parameterBuilder.length() > 0 ?
						parameterBuilder.substring( 0, parameterBuilder.length() - 2 ) :
						parameterBuilder.toString();

		return "ExecutableMetaData [executable=" + getType() + " " + getName() + "(" + parameters + "), isCascading=" + isCascading() + ", isConstrained="
				+ isConstrained() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode( parameterTypes );
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !super.equals( obj ) ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		ExecutableMetaData other = (ExecutableMetaData) obj;
		if ( !Arrays.equals( parameterTypes, other.parameterTypes ) ) {
			return false;
		}
		return true;
	}

	/**
	 * Creates new {@link ExecutableMetaData} instances.
	 *
	 * @author Gunnar Morling
	 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
	 */
	public static class Builder extends MetaDataBuilder {

		/**
		 * The rules applying for the definition of executable constraints.
		 */
		private static final Set<MethodConfigurationRule> rules = Collections.unmodifiableSet(
				CollectionHelper.<MethodConfigurationRule>asSet(
						new OverridingMethodMustNotAlterParameterConstraints(),
						new ParallelMethodsMustNotDefineParameterConstraints(),
						new VoidMethodsMustNotBeReturnValueConstrained(),
						new ReturnValueMayOnlyBeMarkedOnceAsCascadedPerHierarchyLine(),
						new ParallelMethodsMustNotDefineGroupConversionForCascadedReturnValue()
				)
		);

		/**
		 * Either CONSTRUCTOR or METHOD.
		 */
		private final ConstrainedElement.ConstrainedElementKind kind;
		private final Set<ConstrainedExecutable> constrainedExecutables = newHashSet();
		private final ExecutableElement executable;
		private final Set<MetaConstraint<?>> crossParameterConstraints = newHashSet();
		private boolean isConstrained = false;

		/**
		 * Holds a merged representation of the configurations for one method
		 * from the hierarchy contributed by the different meta data providers.
		 * Used to check for violations of the Liskov substitution principle by
		 * e.g. adding parameter constraints in sub type methods.
		 */
		private final Map<Class<?>, ConstrainedExecutable> executablesByDeclaringType = newHashMap();

		private final ExecutableHelper executableHelper;

		/**
		 * Creates a new builder based on the given executable meta data.
		 *
		 * @param constrainedExecutable The base executable for this builder. This is the lowest
		 * executable with a given signature within a type hierarchy.
		 * @param constraintHelper the constraint helper
		 * @param executableHelper the executable helper
		 */
		public Builder(Class<?> beanClass, ConstrainedExecutable constrainedExecutable, ConstraintHelper constraintHelper, ExecutableHelper executableHelper) {
			super( beanClass, constraintHelper );

			this.executableHelper = executableHelper;
			kind = constrainedExecutable.getKind();
			executable = constrainedExecutable.getExecutable();
			add( constrainedExecutable );
		}

		@Override
		public boolean accepts(ConstrainedElement constrainedElement) {
			if ( kind != constrainedElement.getKind() ) {
				return false;
			}

			ExecutableElement executableElement = ( (ConstrainedExecutable) constrainedElement ).getExecutable();

			//are the locations equal (created by different builders) or
			//does one of the executables override the other one?
			return
					executable.equals( executableElement ) ||
							executableHelper.overrides( executable, executableElement ) ||
							executableHelper.overrides( executableElement, executable );
		}

		@Override
		public void add(ConstrainedElement constrainedElement) {
			super.add( constrainedElement );
			ConstrainedExecutable constrainedExecutable = (ConstrainedExecutable) constrainedElement;

			constrainedExecutables.add( constrainedExecutable );
			isConstrained = isConstrained || constrainedExecutable.isConstrained();
			crossParameterConstraints.addAll( constrainedExecutable.getCrossParameterConstraints() );

			addToExecutablesByDeclaringType( constrainedExecutable );
		}

		/**
		 * Merges the given executable with the metadata contributed by other
		 * providers for the same executable in the hierarchy.
		 *
		 * @param executable The executable to merge.
		 */
		private void addToExecutablesByDeclaringType(ConstrainedExecutable executable) {
			Class<?> beanClass = executable.getLocation().getDeclaringClass();
			ConstrainedExecutable mergedExecutable = executablesByDeclaringType.get( beanClass );

			if ( mergedExecutable != null ) {
				mergedExecutable = mergedExecutable.merge( executable );
			}
			else {
				mergedExecutable = executable;
			}

			executablesByDeclaringType.put( beanClass, mergedExecutable.merge( executable ) );
		}

		@Override
		public ExecutableMetaData build() {
			assertCorrectnessOfConfiguration();

			return new ExecutableMetaData(
					executable.getSimpleName(),
					ReflectionHelper.typeOf( executable.getMember() ),
					executable.getParameterTypes(),
					kind == ConstrainedElement.ConstrainedElementKind.CONSTRUCTOR ? ElementKind.CONSTRUCTOR : ElementKind.METHOD,
					adaptOriginsAndImplicitGroups( getConstraints() ),
					findParameterMetaData(),
					adaptOriginsAndImplicitGroups( crossParameterConstraints ),
					getGroupConversions(),
					isCascading(),
					isConstrained,
					executable.isGetterMethod(),
					requiresUnwrapping()
			);
		}

		/**
		 * Finds the one executable from the underlying hierarchy with parameter
		 * constraints. If no executable in the hierarchy is parameter constrained,
		 * the parameter meta data from this builder's base executable is returned.
		 *
		 * @return The parameter meta data for this builder's executable.
		 */
		private List<ParameterMetaData> findParameterMetaData() {
			List<ParameterMetaData.Builder> parameterBuilders = null;

			for ( ConstrainedExecutable oneExecutable : constrainedExecutables ) {
				if ( parameterBuilders == null ) {
					parameterBuilders = newArrayList();

					for ( ConstrainedParameter oneParameter : oneExecutable.getAllParameterMetaData() ) {
						parameterBuilders.add(
								new ParameterMetaData.Builder(
										executable.getMember().getDeclaringClass(),
										oneParameter,
										constraintHelper
								)
						);
					}
				}
				else {
					int i = 0;
					for ( ConstrainedParameter oneParameter : oneExecutable.getAllParameterMetaData() ) {
						parameterBuilders.get( i ).add( oneParameter );
						i++;
					}
				}
			}

			List<ParameterMetaData> parameterMetaDatas = newArrayList();

			for ( ParameterMetaData.Builder oneBuilder : parameterBuilders ) {
				parameterMetaDatas.add( oneBuilder.build() );
			}

			return parameterMetaDatas;
		}

		/**
		 * <p>
		 * Checks the configuration of this method for correctness as per the
		 * rules outlined in the Bean Validation specification, section 4.5.5
		 * ("Method constraints in inheritance hierarchies").
		 * </p>
		 * <p>
		 * In particular, overriding methods in sub-types may not add parameter
		 * constraints and the return value of an overriding method may not be
		 * marked as cascaded if the return value is marked as cascaded already
		 * on the overridden method.
		 * </p>
		 *
		 * @throws javax.validation.ConstraintDeclarationException In case any of the rules mandated by the
		 * specification are violated.
		 */
		private void assertCorrectnessOfConfiguration() {
			for ( Entry<Class<?>, ConstrainedExecutable> entry : executablesByDeclaringType.entrySet() ) {
				for ( Entry<Class<?>, ConstrainedExecutable> otherEntry : executablesByDeclaringType.entrySet() ) {
					for ( MethodConfigurationRule rule : rules ) {
						rule.apply( entry.getValue(), otherEntry.getValue() );
					}
				}
			}
		}
	}
}
