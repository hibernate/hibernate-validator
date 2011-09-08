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
package org.hibernate.validator.metadata.raw;

import java.util.Set;

import org.hibernate.validator.metadata.core.MetaConstraint;
import org.hibernate.validator.metadata.location.MethodConstraintLocation;

/**
 * Contains constraint-related meta-data for one method parameter.
 *
 * @author Gunnar Morling
 */
public class ConstrainedParameter extends AbstractConstrainedElement {

	private final String name;

	public ConstrainedParameter(ConfigurationSource source, MethodConstraintLocation location, String name, Set<MetaConstraint<?>> constraints, boolean isCascading) {

		super( source, ConstrainedElementKind.PARAMETER, location, constraints, isCascading );

		this.name = name;
	}

	public MethodConstraintLocation getLocation() {
		return (MethodConstraintLocation) super.getLocation();
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
