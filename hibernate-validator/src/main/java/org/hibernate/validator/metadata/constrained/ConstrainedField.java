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
package org.hibernate.validator.metadata.constrained;

import java.lang.reflect.Member;
import java.util.Set;

import org.hibernate.validator.metadata.MetaConstraint;
import org.hibernate.validator.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.util.ReflectionHelper;

/**
 * @author Gunnar Morling
 */
public class ConstrainedField extends AbstractConstrainedElement {

	/**
	 * @param location
	 * @param constraints
	 * @param isCascading
	 */
	public ConstrainedField(ConfigurationSource source, BeanConstraintLocation location, Set<MetaConstraint<?>> constraints,
							boolean isCascading) {

		super( source, location, constraints, isCascading );

		Member member = location.getMember();
		if ( member != null && isConstrained() ) {
			ReflectionHelper.setAccessibility( member );
		}
	}

	public ConstrainedElementKind getConstrainedElementKind() {
		return ConstrainedElementKind.FIELD;
	}

	public BeanConstraintLocation getLocation() {
		return (BeanConstraintLocation) super.getLocation();
	}

}
