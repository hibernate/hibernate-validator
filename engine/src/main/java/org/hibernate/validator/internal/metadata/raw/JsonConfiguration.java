/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.raw;

import java.util.List;
import java.util.Set;

import javax.json.JsonObject;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

/**
 * Represents the complete constraint related configuration of one JSON type
 * originating from one {@link ConfigurationSource}. Contains meta-data on
 * constraints (property and class level) as well as meta data on default
 * group sequences.
 *
 * @author Gunnar Morling
 */
public class JsonConfiguration<T> {

	private final ConfigurationSource source;

	private final Class<T> beanClass;

	private final Set<ConstrainedElement> constrainedElements;

	private final List<Class<?>> defaultGroupSequence;

	// TODO: not good ! :)
	private final DefaultGroupSequenceProvider<JsonObject> defaultGroupSequenceProvider;

	/**
	 * Creates a new bean configuration.
	 *
	 * @param source The source of this configuration.
	 * @param beanClass The type represented by this configuration.
	 * @param constrainedElements The constraint elements representing this type's fields,
	 * methods etc.
	 * @param defaultGroupSequence The default group sequence for the given type as configured by
	 * the given configuration source.
	 * @param defaultGroupSequenceProvider The default group sequence provider for the given type as
	 * configured by the given configuration source.
	 */
	public JsonConfiguration(
			ConfigurationSource source,
			Class<T> beanClass,
			Set<? extends ConstrainedElement> constrainedElements,
			List<Class<?>> defaultGroupSequence,
			DefaultGroupSequenceProvider<JsonObject> defaultGroupSequenceProvider) {

		this.source = source;
		this.beanClass = beanClass;
		this.constrainedElements = CollectionHelper.<ConstrainedElement>newHashSet( constrainedElements );
		this.defaultGroupSequence = defaultGroupSequence;
		this.defaultGroupSequenceProvider = defaultGroupSequenceProvider;
	}

	public ConfigurationSource getSource() {
		return source;
	}

	public Class<T> getBeanClass() {
		return beanClass;
	}

	public Set<ConstrainedElement> getConstrainedElements() {
		return constrainedElements;
	}

	public List<Class<?>> getDefaultGroupSequence() {
		return defaultGroupSequence;
	}

	public DefaultGroupSequenceProvider<JsonObject> getDefaultGroupSequenceProvider() {
		return defaultGroupSequenceProvider;
	}

	@Override
	public String toString() {
		return "JsonConfiguration [beanClass=" + beanClass.getSimpleName()
				+ ", source=" + source
				+ ", constrainedElements=" + constrainedElements
				+ ", defaultGroupSequence=" + defaultGroupSequence
				+ ", defaultGroupSequenceProvider="
				+ defaultGroupSequenceProvider + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ( ( beanClass == null ) ? 0 : beanClass.hashCode() );
		result = prime * result + ( ( source == null ) ? 0 : source.hashCode() );
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		JsonConfiguration<?> other = (JsonConfiguration<?>) obj;
		if ( beanClass == null ) {
			if ( other.beanClass != null ) {
				return false;
			}
		}
		else if ( !beanClass.equals( other.beanClass ) ) {
			return false;
		}
		if ( source != other.source ) {
			return false;
		}
		return true;
	}

}
