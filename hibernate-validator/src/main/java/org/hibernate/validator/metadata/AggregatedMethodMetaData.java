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
package org.hibernate.validator.metadata;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintDeclarationException;
import javax.validation.Valid;

import org.hibernate.validator.util.ReflectionHelper;

import static org.hibernate.validator.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.util.CollectionHelper.newHashSet;

/**
 * <p>
 * An aggregated view of the constraint related meta data for a given method and
 * all the methods in the inheritance hierarchy which it overrides or
 * implements.
 * </p>
 * <p>
 * Instances are retrieved by creating a {@link Builder} and adding all required
 * {@link MethodMetaData} objects to it. Instances are read-only after creation.
 * </p>
 *
 * @author Gunnar Morling
 */
public class AggregatedMethodMetaData implements Iterable<MethodMetaConstraint<?>> {

	private final Method method;

	private final Map<Class<?>, MethodMetaData> metaDataByDefiningType;

	private final boolean isCascading;

	private final boolean isConstrained;

	private final List<MethodMetaConstraint<?>> returnValueConstraints;

	private final List<ParameterMetaData> parameterMetaData;

	/**
	 * A declaration exception in case this method contains any illegal method
	 * parameter constraints. Such illegal parameter constraints shall not
	 * hinder standard bean/property validation as defined by the Bean
	 * Validation API. Therefore this exception is created when building up the
	 * meta data for validated beans, but it will only be thrown by the
	 * validation engine when actually a method validation is performed.
	 */
	private final ConstraintDeclarationException parameterConstraintDeclarationException;

	private AggregatedMethodMetaData(
			Builder builder,
			List<MethodMetaConstraint<?>> returnValueConstraints,
			List<ParameterMetaData> parameterMetaData,
			ConstraintDeclarationException parameterConstraintDeclarationException) {

		method = builder.method;
		metaDataByDefiningType = Collections.unmodifiableMap( builder.metaDataByDefiningType );
		isCascading = builder.isCascading;
		isConstrained = builder.isConstrained;

		this.returnValueConstraints = Collections.unmodifiableList( returnValueConstraints );
		this.parameterMetaData = Collections.unmodifiableList( parameterMetaData );
		this.parameterConstraintDeclarationException = parameterConstraintDeclarationException;
	}

	/**
	 * Creates new {@link AggregatedMethodMetaData} instances.
	 *
	 * @author Gunnar Morling
	 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
	 */
	public static class Builder {

		private final Method method;

		private final Map<Class<?>, MethodMetaData> metaDataByDefiningType = newHashMap();

		private boolean isCascading;

		private boolean isConstrained;

		/**
		 * Creates a new builder based on the given method meta data.
		 *
		 * @param metaData The base method for this builder. This is the lowest
		 * method with a given signature within a type hierarchy.
		 */
		public Builder(MethodMetaData metaData) {

			method = metaData.getMethod();
			metaDataByDefiningType.put( method.getDeclaringClass(), metaData );
			isCascading = metaData.isCascading();
			isConstrained = metaData.isConstrained();
		}

		/**
		 * Whether the given method can be added to this builder or not. This is
		 * the case if the given method has the same signature as this builder's
		 * base method (and originates from the same type hierarchy, which
		 * currently is not checked).
		 *
		 * @param metaData The method of interest.
		 *
		 * @return <code>True</code>, if the given method can be added to this
		 *         builder, <code>false</code> otherwise.
		 */
		public boolean accepts(MethodMetaData metaData) {
			return ReflectionHelper.haveSameSignature( method, metaData.getMethod() );
		}

		/**
		 * Adds the given method to this builder. It must be checked with
		 * {@link #accepts(MethodMetaData)} before, whether this is allowed or
		 * not.
		 *
		 * @param metaData The meta data to add.
		 */
		public void addMetaData(MethodMetaData metaData) {

			MethodMetaData existingMetaData =
					metaDataByDefiningType.get( metaData.getMethod().getDeclaringClass() );

			if ( existingMetaData != null ) {
				metaData = existingMetaData.merge( metaData );
			}

			metaDataByDefiningType.put( metaData.getMethod().getDeclaringClass(), metaData );
			isCascading = isCascading || metaData.isCascading();
			isConstrained = isConstrained || metaData.isConstrained();
		}

		/**
		 * Creates a new, read-only {@link AggregatedMethodMetaData} object from
		 * this builder.
		 *
		 * @return An {@code AggregatedMethodMetaData} object
		 */
		public AggregatedMethodMetaData build() {
			return new AggregatedMethodMetaData(
					this, collectReturnValueConstraints(), findParameterMetaData(), checkParameterConstraints()
			);
		}

		/**
		 * Collects all return value constraints from this builder's method
		 * hierarchy.
		 *
		 * @return A list with all return value constraints.
		 */
		private List<MethodMetaConstraint<?>> collectReturnValueConstraints() {

			List<MethodMetaConstraint<?>> theValue = newArrayList();

			for ( MethodMetaData oneMethodMetaData : metaDataByDefiningType.values() ) {
				for ( MethodMetaConstraint<?> oneConstraint : oneMethodMetaData ) {
					theValue.add( oneConstraint );
				}
			}

			return theValue;
		}

		/**
		 * Finds the one method from the underlying hierarchy with parameter
		 * constraints. If no method in the hierarchy is parameter constrained,
		 * the parameter meta data from this builder's base method is returned.
		 *
		 * @return The parameter meta data for this builder's method.
		 */
		private List<ParameterMetaData> findParameterMetaData() {

			for ( MethodMetaData oneMethod : metaDataByDefiningType.values() ) {

				if ( oneMethod.hasParameterConstraints() ) {
					return oneMethod.getAllParameterMetaData();
				}
			}

			return metaDataByDefiningType.get( method.getDeclaringClass() ).getAllParameterMetaData();
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

			Collection<MethodMetaData> allMethods = metaDataByDefiningType.values();
			Set<MethodMetaData> methodsWithParameterConstraints = getMethodsWithParameterConstraints( allMethods );

			if ( methodsWithParameterConstraints.isEmpty() ) {
				return null;
			}

			if ( methodsWithParameterConstraints.size() > 1 ) {
				return new ConstraintDeclarationException(
						"Only the root method of an overridden method in an inheritance hierarchy may be annotated with parameter constraints, " +
								"but there are parameter constraints defined at all of the following overridden methods: " +
								methodsWithParameterConstraints
				);
			}

			MethodMetaData constrainedMethod = methodsWithParameterConstraints.iterator().next();

			for ( MethodMetaData oneMethod : allMethods ) {

				if ( !constrainedMethod.getMethod()
						.getDeclaringClass()
						.isAssignableFrom( oneMethod.getMethod().getDeclaringClass() ) ) {
					return new ConstraintDeclarationException(
							"Only the root method of an overridden method in an inheritance hierarchy may be annotated with parameter constraints. " +
									"The following method itself has no parameter constraints but it is not defined on a sub-type of " +
									constrainedMethod.getMethod().getDeclaringClass() + ": " + oneMethod
					);
				}
			}

			return null;
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

	public Method getMethod() {
		return method;
	}

	/**
	 * Returns meta data for the specified parameter of the represented method.
	 *
	 * @return Meta data for the specified parameter. Will never be
	 *         <code>null</code>.
	 */
	public ParameterMetaData getParameterMetaData(int parameterIndex) {
		return parameterMetaData.get( parameterIndex );
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
		return parameterMetaData;
	}

	/**
	 * Whether a cascaded validation of the return value of the represented
	 * method shall be performed or not. This is the case if either the method
	 * itself is annotated with {@link Valid} or any of the method's up in the
	 * inheritance hierarchy which it overrides.
	 *
	 * @return <code>True</code>, if a cascaded return value validation shall be
	 *         performed, <code>false</code> otherwise.
	 */
	public boolean isCascading() {
		return isCascading;
	}

	/**
	 * Whether the represented method itself or any of the method's up in the
	 * inheritance hierarchy which it overrides/implements is constrained.
	 *
	 * @return <code>True</code>, if this method is constrained by any means,
	 *         <code>false</code> otherwise.
	 */
	public boolean isConstrained() {
		return isConstrained;
	}

	/**
	 * Returns a single method meta data from this aggregation.
	 *
	 * @param method The method to retrieve the meta data for. Must either be the
	 * method represented by this meta data object or one method from
	 * a super-type, which the method represented by this meta data
	 * object overrides/implements.
	 *
	 * @return The meta data for the given method or null if this aggregation
	 *         doesn't contain any meta data for that method.
	 */
	public MethodMetaData getSingleMetaDataFor(Method method) {
		return metaDataByDefiningType.get( method.getDeclaringClass() );
	}

	public Iterable<MethodMetaData> getAllMethodMetaData() {
		return metaDataByDefiningType.values();
	}

	/**
	 * An iterator with the return value constraints of the represented method.
	 */
	public Iterator<MethodMetaConstraint<?>> iterator() {
		return returnValueConstraints.iterator();
	}

	@Override
	public String toString() {
		return "AggregatedMethodMetaData [method=" + method
				+ ", isCascading=" + isCascading() + ", isConstrained="
				+ isConstrained() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( method == null ) ? 0 : method.hashCode() );
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		AggregatedMethodMetaData other = (AggregatedMethodMetaData) obj;
		if ( method == null ) {
			if ( other.method != null ) {
				return false;
			}
		}
		else if ( !method.equals( other.method ) ) {
			return false;
		}
		return true;
	}

}
