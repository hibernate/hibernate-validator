/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.xml;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ParameterNameProvider;

import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ExecutableConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Builder for constraint parameters.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintParameterBuilder {

	private ConstraintParameterBuilder() {
	}

	public static List<ConstrainedParameter> buildParameterConstraints(List<ParameterType> parameterList,
																	   ExecutableElement executableElement,
																	   String defaultPackage,
																	   ConstraintHelper constraintHelper,
																	   ParameterNameProvider parameterNameProvider) {
		List<ConstrainedParameter> constrainedParameters = newArrayList();
		int i = 0;
		String[] parameterNames = executableElement.getParameterNames( parameterNameProvider );
		for ( ParameterType parameterType : parameterList ) {
			ExecutableConstraintLocation constraintLocation = new ExecutableConstraintLocation( executableElement, i );
			Set<MetaConstraint<?>> metaConstraints = newHashSet();
			for ( ConstraintType constraint : parameterType.getConstraint() ) {
				ConstraintDescriptorImpl<?> constraintDescriptor = ConstraintBuilder.buildConstraintDescriptor(
						constraint,
						executableElement.getElementType(),
						defaultPackage,
						constraintHelper
				);
				@SuppressWarnings("unchecked")
				MetaConstraint<?> metaConstraint = new MetaConstraint( constraintDescriptor, constraintLocation );
				metaConstraints.add( metaConstraint );
			}
			Map<Class<?>, Class<?>> groupConversions = GroupConversionBuilder.buildGroupConversionMap(
					parameterType.getConvertGroup(),
					defaultPackage
			);

			ConstrainedParameter constrainedParameter = new ConstrainedParameter(
					ConfigurationSource.XML,
					constraintLocation,
					parameterNames[i],
					metaConstraints,
					groupConversions,
					parameterType.getValid() != null
			);
			constrainedParameters.add( constrainedParameter );
			i++;
		}

		return constrainedParameters;
	}
}


