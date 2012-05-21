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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintDeclarationException;
import javax.validation.metadata.MethodDescriptor;
import javax.validation.metadata.ParameterDescriptor;

import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.MethodDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.MethodConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedMethod;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * An aggregated view of the constraint related meta data for a given method and
 * all the methods in the inheritance hierarchy which it overrides or
 * implements.
 * <p>
 * Instances are retrieved by creating a {@link Builder} and adding all required
 * {@link ConstrainedMethod} objects to it. Instances are read-only after
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
public class MethodMetaData extends AbstractConstraintMetaData {

	private static final Log log = LoggerFactory.make();

	private final Class<?>[] parameterTypes;
	private final List<ParameterMetaData> parameterMetaDataList;

	/**
	 * A declaration exception in case this method contains any illegal method
	 * parameter constraints. Such illegal parameter constraints shall not
	 * hinder standard bean/property validation as defined by the Bean
	 * Validation API. Therefore this exception is created when building up the
	 * meta data for validated beans, but it will only be thrown by the
	 * validation engine when actually a method validation is performed.
	 */
	private final ConstraintDeclarationException parameterConstraintDeclarationException;

	private MethodMetaData(
			String name,
			Class<?> returnType,
			Class<?>[] parameterTypes,
			Set<MetaConstraint<?>> returnValueConstraints,
			List<ParameterMetaData> parameterMetaData,
			ConstraintDeclarationException parameterConstraintDeclarationException,
			boolean isCascading,
			boolean isConstrained) {

		super(
				name,
				returnType,
				returnValueConstraints,
				ConstraintMetaDataKind.METHOD,
				isCascading,
				isConstrained
		);

		this.parameterTypes = parameterTypes;
		this.parameterMetaDataList = Collections.unmodifiableList( parameterMetaData );
		this.parameterConstraintDeclarationException = parameterConstraintDeclarationException;
	}

	/**
	 * Creates new {@link MethodMetaData} instances.
	 *
	 * @author Gunnar Morling
	 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
	 */
	public static class Builder extends MetaDataBuilder {
		private Set<ConstrainedMethod> constrainedMethods = newHashSet();
		private MethodConstraintLocation location;
		private final Set<MetaConstraint<?>> returnValueConstraints = newHashSet();
		private boolean isCascading = false;
		private boolean isConstrained = false;

		/**
		 * Creates a new builder based on the given method meta data.
		 *
		 * @param constrainedMethod The base method for this builder. This is the lowest
		 * method with a given signature within a type hierarchy.
		 * @param constraintHelper the constraint helper
		 */
		public Builder(ConstrainedMethod constrainedMethod, ConstraintHelper constraintHelper) {
			super( constraintHelper );

			location = constrainedMethod.getLocation();
			add( constrainedMethod );
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean accepts(ConstrainedElement constrainedElement) {
			return constrainedElement.getKind() == ConstrainedElementKind.METHOD &&
					ReflectionHelper.haveSameSignature(
							location.getMember(),
							( (ConstrainedMethod) constrainedElement ).getLocation().getMember()
					);
		}

		/**
		 * {@inheritDoc}
		 */
		public void add(ConstrainedElement constrainedElement) {
			ConstrainedMethod constrainedMethod = (ConstrainedMethod) constrainedElement;

			constrainedMethods.add( constrainedMethod );
			isCascading = isCascading || constrainedMethod.isCascading();
			isConstrained = isConstrained || constrainedMethod.isConstrained();
			returnValueConstraints.addAll( constrainedMethod.getConstraints() );
		}

		/**
		 * {@inheritDoc}
		 */
		public MethodMetaData build() {
			Method method = location.getMember();

			return new MethodMetaData(
					method.getName(),
					method.getReturnType(),
					method.getParameterTypes(),
					adaptOriginsAndImplicitGroups( location.getBeanClass(), returnValueConstraints ),
					findParameterMetaData(),
					checkParameterConstraints(),
					isCascading,
					isConstrained
			);
		}

		/**
		 * Finds the one method from the underlying hierarchy with parameter
		 * constraints. If no method in the hierarchy is parameter constrained,
		 * the parameter meta data from this builder's base method is returned.
		 *
		 * @return The parameter meta data for this builder's method.
		 */
		private List<ParameterMetaData> findParameterMetaData() {

			List<ParameterMetaData.Builder> parameterBuilders = null;

			for ( ConstrainedMethod oneMethod : constrainedMethods ) {

				if ( parameterBuilders == null ) {
					parameterBuilders = newArrayList();

					for ( ConstrainedParameter oneParameter : oneMethod.getAllParameterMetaData() ) {
						parameterBuilders.add(
								new ParameterMetaData.Builder(
										location.getBeanClass(),
										oneParameter,
										constraintHelper
								)
						);
					}
				}
				else {
					int i = 0;
					for ( ConstrainedParameter oneParameter : oneMethod.getAllParameterMetaData() ) {
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
		 * Checks that there are no invalid parameter constraints defined at
		 * this builder's methods.
		 *
		 * @return A {@link ConstraintDeclarationException} describing the first
		 *         illegal method parameter constraint found or {@code null}, if
		 *         the methods of this builder have no such illegal constraints.
		 */
		private ConstraintDeclarationException checkParameterConstraints() {

			Set<ConstrainedMethod> methodsWithParameterConstraints = getMethodsWithParameterConstraints(
					constrainedMethods
			);

			if ( methodsWithParameterConstraints.isEmpty() ) {
				return null;
			}
			Set<Class<?>> definingTypes = newHashSet();

			for ( ConstrainedMethod constrainedMethod : methodsWithParameterConstraints ) {
				definingTypes.add( constrainedMethod.getLocation().getBeanClass() );
			}

			if ( definingTypes.size() > 1 ) {
				return new ConstraintDeclarationException(
						"Only the root method of an overridden method in an inheritance hierarchy may be annotated with parameter constraints, " +
								"but there are parameter constraints defined at all of the following overridden methods: " +
								methodsWithParameterConstraints
				);
			}

			ConstrainedMethod constrainedMethod = methodsWithParameterConstraints.iterator().next();

			for ( ConstrainedMethod oneMethod : constrainedMethods ) {

				if ( !constrainedMethod.getLocation().getBeanClass()
						.isAssignableFrom( oneMethod.getLocation().getBeanClass() ) ) {
					return new ConstraintDeclarationException(
							"Only the root method of an overridden method in an inheritance hierarchy may be annotated with parameter constraints. " +
									"The following method itself has no parameter constraints but it is not defined on a sub-type of " +
									constrainedMethod.getLocation().getBeanClass() + ": " + oneMethod
					);
				}
			}

			return null;
		}

		/**
		 * Returns a set with those methods from the given pile of methods that have
		 * at least one constrained parameter or at least one parameter annotated
		 * with {@link javax.validation.Valid}.
		 *
		 * @param methods The methods to search in.
		 *
		 * @return A set with constrained methods. May be empty, but never null.
		 */
		private Set<ConstrainedMethod> getMethodsWithParameterConstraints(Iterable<ConstrainedMethod> methods) {
			Set<ConstrainedMethod> theValue = newHashSet();

			for ( ConstrainedMethod oneMethod : methods ) {
				if ( oneMethod.hasParameterConstraints() ) {
					theValue.add( oneMethod );
				}
			}

			return theValue;
		}

	}

	/**
	 * <p>
	 * Checks the parameter constraints of this method for correctness.
	 * </p>
	 * <p>
	 * The following rules apply for this check:
	 * </p>
	 * <ul>
	 * <li>Only the root method of an overridden method in an inheritance
	 * hierarchy may be annotated with parameter constraints in order to avoid
	 * the strengthening of a method's preconditions by additional parameter
	 * constraints defined at sub-types. If the root method itself has no
	 * parameter constraints, also no parameter constraints may be added in
	 * sub-types.</li>
	 * <li>If there are multiple root methods for an method in an inheritance
	 * hierarchy (e.g. by implementing two interfaces defining the same method)
	 * no parameter constraints for this method are allowed at all in order to
	 * avoid a strengthening of a method's preconditions in parallel types.</li>
	 * </ul>
	 *
	 * @throws ConstraintDeclarationException In case the represented method has an illegal parameter
	 * constraint.
	 */
	public void assertCorrectnessOfMethodParameterConstraints() throws ConstraintDeclarationException {

		if ( parameterConstraintDeclarationException != null ) {
			throw parameterConstraintDeclarationException;
		}
	}

	/**
	 * Returns meta data for the specified parameter of the represented method.
	 *
	 * @param parameterIndex the index of the parameter
	 *
	 * @return Meta data for the specified parameter. Will never be {@code null}.
	 */
	public ParameterMetaData getParameterMetaData(int parameterIndex) {

		if ( parameterIndex < 0 || parameterIndex > parameterMetaDataList.size() - 1 ) {
			throw log.getInvalidMethodParameterIndexException( getName(), parameterIndex );
		}

		return parameterMetaDataList.get( parameterIndex );
	}

	/**
	 * Returns meta data for all parameters of the represented method.
	 *
	 * @return A list with parameter meta data. The length corresponds to the
	 *         number of parameters of the method represented by this meta data
	 *         object, so an empty list may be returned (in case of a
	 *         parameterless method), but never <code>null</code>.
	 */
	public List<ParameterMetaData> getAllParameterMetaData() {
		return parameterMetaDataList;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	public MethodDescriptor asDescriptor(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		return new MethodDescriptorImpl(
				getType(),
				getName(),
				asDescriptors( getConstraints() ),
				isCascading(),
				parametersAsDescriptors( defaultGroupSequenceRedefined, defaultGroupSequence ),
				defaultGroupSequenceRedefined,
				defaultGroupSequence
		);
	}

	private List<ParameterDescriptor> parametersAsDescriptors(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		List<ParameterDescriptor> theValue = newArrayList();

		for ( ParameterMetaData parameterMetaData : parameterMetaDataList ) {
			theValue.add( parameterMetaData.asDescriptor( defaultGroupSequenceRedefined, defaultGroupSequence ) );
		}

		return theValue;
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

		return "MethodMetaData [method=" + getType().getSimpleName() + " " + getName() + "(" + parameters + "), isCascading=" + isCascading() + ", isConstrained="
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
		MethodMetaData other = (MethodMetaData) obj;
		if ( !Arrays.equals( parameterTypes, other.parameterTypes ) ) {
			return false;
		}
		return true;
	}

}
