/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.raw.propertyholder;

import java.util.List;
import java.util.Set;

import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.util.CollectionHelper;

/**
 * @author Marko Bekhta
 */
public class PropertyHolderConfiguration {

	private final PropertyHolderConfigurationSource source;

	private final String mappingName;

	private final Set<ConstrainedElement> constrainedElements;

	private final List<Class<?>> defaultGroupSequence;

	public PropertyHolderConfiguration(
			PropertyHolderConfigurationSource source,
			String mappingName,
			Set<? extends ConstrainedElement> constrainedElements,
			List<Class<?>> defaultGroupSequence) {

		this.source = source;
		this.mappingName = mappingName;
		this.constrainedElements = CollectionHelper.<ConstrainedElement>newHashSet( constrainedElements );
		this.defaultGroupSequence = defaultGroupSequence;
	}

	public PropertyHolderConfigurationSource getSource() {
		return source;
	}

	public String getMappingName() {
		return mappingName;
	}

	public Set<ConstrainedElement> getConstrainedElements() {
		return constrainedElements;
	}

	public List<Class<?>> getDefaultGroupSequence() {
		return defaultGroupSequence;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder( "PropertyHolderConfiguration{" );
		sb.append( "source=" ).append( source );
		sb.append( ", mappingName='" ).append( mappingName ).append( '\'' );
		sb.append( ", constrainedElements=" ).append( constrainedElements );
		sb.append( ", defaultGroupSequence=" ).append( defaultGroupSequence );
		sb.append( '}' );
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		PropertyHolderConfiguration that = (PropertyHolderConfiguration) o;

		if ( source != that.source ) {
			return false;
		}
		if ( !mappingName.equals( that.mappingName ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = source.hashCode();
		result = 31 * result + mappingName.hashCode();
		return result;
	}
}
