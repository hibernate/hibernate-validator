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
package org.hibernate.validator.internal.metadata.raw;

import java.util.Set;

import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.BeanConstraintLocation;

/**
 * Represents a field of a Java type and all its associated meta-data relevant
 * in the context of bean validation, for instance its constraints.
 *
 * @author Gunnar Morling
 */
public class ConstrainedField extends AbstractConstrainedElement {

	/**
	 * Creates a new field meta data object.
	 *
	 * @param source The source of meta data.
	 * @param location The location of the represented field.
	 * @param constraints The constraints of the represented field, if any.
	 * @param isCascading Whether a cascaded validation of the represented field shall
	 * be performed or not.
	 */
	public ConstrainedField(ConfigurationSource source,
							BeanConstraintLocation location,
							Set<MetaConstraint<?>> constraints,
							boolean isCascading) {

		super( source, ConstrainedElementKind.FIELD, location, constraints, isCascading );
	}

	public BeanConstraintLocation getLocation() {
		return (BeanConstraintLocation) super.getLocation();
	}
}
