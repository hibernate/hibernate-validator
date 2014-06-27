/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.lang.reflect.Member;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * @author Khalid Alqinyah
 */
public class ConstrainedTypeArgument extends AbstractConstrainedElement {
	public ConstrainedTypeArgument(ConfigurationSource source,
								   ConstraintLocation location,
								   Set<MetaConstraint<?>> constraints,
								   Map<Class<?>, Class<?>> groupConversions,
								   boolean isCascading,
								   boolean requiresUnwrapping) {
		super(
				source,
				ConstrainedElementKind.TYPE_USE,
				location,
				constraints,
				groupConversions,
				isCascading,
				requiresUnwrapping
		);

		Member member = location.getMember();
		if ( member != null && isConstrained() ) {
			ReflectionHelper.setAccessibility( member );
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ( ( getLocation().getMember() == null ) ? 0 : getLocation().getMember().hashCode() );
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
		ConstrainedTypeArgument other = (ConstrainedTypeArgument) obj;
		if ( getLocation().getMember() == null ) {
			if ( other.getLocation().getMember() != null ) {
				return false;
			}
		}
		else if ( !getLocation().getMember().equals( other.getLocation().getMember() ) ) {
			return false;
		}
		return true;
	}
}
