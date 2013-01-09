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

import java.lang.annotation.ElementType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.metadata.ParameterDescriptor;

import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ParameterDescriptorImpl;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;

/**
 * An aggregated view of the constraint related meta data for a single method
 * parameter.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public class ParameterMetaData extends AbstractConstraintMetaData implements Cascadable {

	private final GroupConverter groupConverter;
	private final int index;

	/**
	 * @param index the parameter index
	 * @param name the parameter name
	 * @param type the parameter type
	 * @param constraints the constraints defined for this parameter
	 * @param isCascading should cascading constraints be evaluated. Returns {@code true} is the constrained element
	 * is marked for cascaded validation ({@code @Valid}), {@code false} otherwise.
	 */
	private ParameterMetaData(int index, String name, Class<?> type, Set<MetaConstraint<?>> constraints, boolean isCascading, Map<Class<?>, Class<?>> groupConversions) {
		super(
				name,
				type,
				constraints,
				ConstraintMetaDataKind.PARAMETER,
				isCascading,
				!constraints.isEmpty() || isCascading
		);

		this.groupConverter = new GroupConverter( groupConversions );
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public Class<?> convertGroup(Class<?> originalGroup) {
		return groupConverter.convertGroup( originalGroup );
	}

	@Override
	public ElementType getElementType() {
		return ElementType.PARAMETER;
	}

	@Override
	public Object getValue(Object parent) {
		return ( (Object[]) parent )[index];
	}

	@Override
	public ParameterDescriptor asDescriptor(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		return new ParameterDescriptorImpl(
				getType(),
				index,
				getName(),
				asDescriptors( getConstraints() ),
				isCascading(),
				defaultGroupSequenceRedefined,
				defaultGroupSequence
		);
	}

	public static class Builder extends MetaDataBuilder {
		private final Class<?> rootClass;
		private final Class<?> parameterType;
		private final int parameterIndex;
		private String name;

		public Builder(Class<?> rootClass, ConstrainedParameter constrainedParameter, ConstraintHelper constraintHelper) {
			super( constraintHelper );

			this.rootClass = rootClass;
			this.parameterType = constrainedParameter.getLocation().getParameterType();
			this.parameterIndex = constrainedParameter.getLocation().getParameterIndex();

			add( constrainedParameter );
		}

		@Override
		public boolean accepts(ConstrainedElement constrainedElement) {
			if ( constrainedElement.getKind() != ConstrainedElementKind.PARAMETER ) {
				return false;
			}

			return ( (ConstrainedParameter) constrainedElement ).getLocation().getParameterIndex() == parameterIndex;
		}

		@Override
		public void add(ConstrainedElement constrainedElement) {
			super.add( constrainedElement );
			ConstrainedParameter constrainedParameter = (ConstrainedParameter) constrainedElement;

			if ( name == null ) {
				name = constrainedParameter.getParameterName();
			}
		}

		@Override
		public ParameterMetaData build() {
			return new ParameterMetaData(
					parameterIndex,
					name,
					parameterType,
					adaptOriginsAndImplicitGroups( rootClass, getConstraints() ),
					isCascading(),
					getGroupConversions()
			);
		}
	}
}
