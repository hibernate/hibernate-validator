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

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Contains constraint-related meta-data for one method parameter.
 *
 * @author Gunnar Morling
 */
public class ConstrainedParameter extends AbstractConstrainedElement {

	private final Type type;
	private final String name;
	private final int index;

	public ConstrainedParameter(ConfigurationSource source,
								ConstraintLocation location,
								Type type,
								int index,
								String name) {
		this(
				source,
				location,
				type,
				index,
				name,
				Collections.<MetaConstraint<?>>emptySet(),
				Collections.<Class<?>, Class<?>>emptyMap(),
				false,
				false
		);
	}

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
	 * @param requiresUnwrapping Whether the value of the parameter must be unwrapped prior to validation or not
	 */
	public ConstrainedParameter(ConfigurationSource source,
								ConstraintLocation location,
								Type type,
								int index,
								String name,
								Set<MetaConstraint<?>> constraints,
								Map<Class<?>, Class<?>> groupConversions,
								boolean isCascading,
								boolean requiresUnwrapping) {
		super(
				source,
				ConstrainedElementKind.PARAMETER,
				location,
				constraints,
				groupConversions,
				isCascading,
				requiresUnwrapping
		);

		this.type = type;
		this.name = name;
		this.index = index;
	}

	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

	/**
	 * Creates a new constrained parameter object by merging this and the given
	 * other parameter.
	 *
	 * @param other The parameter to merge.
	 *
	 * @return A merged parameter.
	 */
	public ConstrainedParameter merge(ConstrainedParameter other) {
		ConfigurationSource mergedSource = ConfigurationSource.max( source, other.source );

		String mergedName;

		if ( source.getPriority() > other.source.getPriority() ) {
			mergedName = name;
		}
		else {
			mergedName = other.name;
		}

		Set<MetaConstraint<?>> mergedConstraints = newHashSet( constraints );
		mergedConstraints.addAll( other.constraints );

		Map<Class<?>, Class<?>> mergedGroupConversions = newHashMap( groupConversions );
		mergedGroupConversions.putAll( other.groupConversions );

		return new ConstrainedParameter(
				mergedSource,
				getLocation(),
				type,
				index,
				mergedName,
				mergedConstraints,
				mergedGroupConversions,
				isCascading || other.isCascading,
				requiresUnwrapping || other.requiresUnwrapping
		);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + index;
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
		ConstrainedParameter other = (ConstrainedParameter) obj;
		if ( index != other.index ) {
			return false;
		}
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
