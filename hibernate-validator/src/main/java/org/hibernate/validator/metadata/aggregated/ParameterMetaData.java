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
package org.hibernate.validator.metadata.aggregated;

import java.util.List;
import java.util.Set;

import org.hibernate.validator.metadata.core.ConstraintHelper;
import org.hibernate.validator.metadata.core.MetaConstraint;
import org.hibernate.validator.metadata.descriptor.ParameterDescriptorImpl;
import org.hibernate.validator.metadata.raw.ConstrainedElement;
import org.hibernate.validator.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.method.metadata.ParameterDescriptor;

import static org.hibernate.validator.util.CollectionHelper.newHashSet;

/**
 * @author Gunnar Morling
 */
public class ParameterMetaData extends AbstractConstraintMetaData {

	private final Class<?> type;

	private final int index;

	private final String name;

	/**
	 * @param constraints
	 * @param isCascading
	 * @param constrainedMetaDataKind
	 */
	public ParameterMetaData(Set<MetaConstraint<?>> constraints, boolean isCascading, String name, Class<?> type, int index, boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {

		super(
				constraints,
				ConstraintMetaDataKind.PARAMETER,
				isCascading,
				!constraints.isEmpty() || isCascading,
				defaultGroupSequenceRedefined,
				defaultGroupSequence
		);

		this.type = type;
		this.index = index;
		this.name = name;
	}

	public Class<?> getType() {
		return type;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public ParameterDescriptor asDescriptor() {
		return new ParameterDescriptorImpl(
				type,
				index,
				isCascading(),
				asDescriptors( getConstraints() ),
				isDefaultGroupSequenceRedefined(),
				getDefaultGroupSequence()
		);
	}

	public static class Builder extends MetaDataBuilder {

		private final Class<?> rootClass;

		private final Class<?> parameterType;

		private final int parameterIndex;

		private final Set<MetaConstraint<?>> constraints = newHashSet();

		private String name;

		private boolean isCascading = false;

		public Builder(Class<?> rootClass, ConstrainedParameter constrainedParameter, ConstraintHelper constraintHelper) {

			super( constraintHelper );

			this.rootClass = rootClass;
			this.parameterType = constrainedParameter.getLocation().getParameterType();
			this.parameterIndex = constrainedParameter.getLocation().getParameterIndex();

			add( constrainedParameter );
		}

		@Override
		public boolean accepts(ConstrainedElement constrainedElement) {

			if ( constrainedElement.getConstrainedElementKind() != ConstrainedElementKind.PARAMETER ) {
				return false;
			}

			return ( (ConstrainedParameter) constrainedElement ).getLocation().getParameterIndex() == parameterIndex;
		}

		@Override
		public void add(ConstrainedElement constrainedElement) {

			ConstrainedParameter constrainedParameter = (ConstrainedParameter) constrainedElement;

			constraints.addAll( constrainedParameter.getConstraints() );

			if ( name == null ) {
				name = constrainedParameter.getParameterName();
			}

			isCascading = isCascading || constrainedParameter.isCascading();
		}

		@Override
		public ParameterMetaData build(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {

			return new ParameterMetaData(
					adaptOriginsAndImplicitGroups( rootClass, constraints ),
					isCascading,
					name,
					parameterType,
					parameterIndex,
					defaultGroupSequenceRedefined,
					defaultGroupSequence
			);
		}

	}

}
