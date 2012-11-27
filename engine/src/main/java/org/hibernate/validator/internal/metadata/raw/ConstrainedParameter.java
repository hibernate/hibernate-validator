/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.internal.metadata.raw;

import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ExecutableConstraintLocation;

/**
 * Contains constraint-related meta-data for one method parameter.
 *
 * @author Gunnar Morling
 */
public class ConstrainedParameter extends AbstractConstrainedElement {

	private final String name;

	/**
	 * Creates a new parameter meta data object.
	 *
	 * @param source The source of meta data.
	 * @param location The location of the represented method parameter.
	 * @param name The name of the represented parameter.
	 * @param constraints The constraints of the represented method parameter, if
	 * any.
	 * @param groupConversions The group conversions of the represented method parameter, if any.
	 * @param isCascading Whether a cascaded validation of the represented method
	 * parameter shall be performed or not.
	 */
	public ConstrainedParameter(ConfigurationSource source, ExecutableConstraintLocation location, String name, Set<MetaConstraint<?>> constraints, Map<Class<?>, Class<?>> groupConversions, boolean isCascading) {

		super(
				source,
				ConstrainedElementKind.PARAMETER,
				location,
				constraints,
				groupConversions,
				isCascading
		);

		this.name = name;
	}

	@Override
	public ExecutableConstraintLocation getLocation() {
		return (ExecutableConstraintLocation) super.getLocation();
	}

	public String getParameterName() {
		return name;
	}

	@Override
	public String toString() {

		//display short annotation type names
		StringBuilder sb = new StringBuilder();

		for ( MetaConstraint<?> oneConstraint : getConstraints() ) {
			sb.append( oneConstraint.getDescriptor().getAnnotation().annotationType().getSimpleName() );
			sb.append( ", " );
		}

		String constraintsAsString = sb.length() > 0 ? sb.substring( 0, sb.length() - 2 ) : sb.toString();

		return "ParameterMetaData [location=" + getLocation() + "], name=" + name + "], constraints=["
				+ constraintsAsString + "], isCascading=" + isCascading() + "]";
	}
}
