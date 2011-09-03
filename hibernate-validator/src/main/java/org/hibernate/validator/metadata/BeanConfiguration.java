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
package org.hibernate.validator.metadata;

import static org.hibernate.validator.util.CollectionHelper.newHashSet;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.metadata.constrained.ConstrainedElement;

/**
 * @author Gunnar Morling
 */
public class BeanConfiguration<T> {

	public static enum ConfigurationSource {
		ANNOTATION, XML, API
	}

	private final EnumSet<ConfigurationSource> configurationSources;

	private final Class<T> beanClass;

	private final Set<ConstrainedElement> constrainableElements;

	private List<Class<?>> defaultGroupSequence;

	private Class<? extends DefaultGroupSequenceProvider<?>> defaultGroupSequenceProvider;

	/**
	 * @param beanClass
	 * @param constraints
	 * @param cascadedMembers
	 * @param defaultGroupSequence
	 */
	public BeanConfiguration(ConfigurationSource configurationSource,
							 Class<T> beanClass,
							 Set<? extends ConstrainedElement> constrainableElements,
							 List<Class<?>> defaultGroupSequence,
							 Class<? extends DefaultGroupSequenceProvider<?>> defaultGroupSequenceProvider) {

		this.configurationSources = EnumSet.of( configurationSource );
		this.beanClass = beanClass;
		this.constrainableElements = newHashSet( constrainableElements );
		this.defaultGroupSequence = defaultGroupSequence;
		this.defaultGroupSequenceProvider = defaultGroupSequenceProvider;
	}

	public Class<T> getBeanClass() {
		return beanClass;
	}

	public Set<ConstrainedElement> getConstrainableElements() {
		return constrainableElements;
	}

	public List<Class<?>> getDefaultGroupSequence() {
		return defaultGroupSequence;
	}

	public Class<? extends DefaultGroupSequenceProvider<?>> getDefaultGroupSequenceProvider() {
		return defaultGroupSequenceProvider;
	}

	public void merge(BeanConfiguration<T> other) {

		configurationSources.addAll( other.configurationSources );
		constrainableElements.addAll( other.getConstrainableElements() );

		// TODO GM: Determine which default sequence should be taken
		if ( ( defaultGroupSequence == null || defaultGroupSequence.isEmpty() )
				&& other.getDefaultGroupSequence() != null ) {
			defaultGroupSequence = other.getDefaultGroupSequence();
		}

		// TODO GM: Determine which default sequence provider should be taken
		if ( defaultGroupSequenceProvider == null
				&& other.getDefaultGroupSequenceProvider() != null ) {
			defaultGroupSequenceProvider = other.getDefaultGroupSequenceProvider();
		}
	}

	@Override
	public String toString() {
		return "BeanConfiguration [configurationSources=" + configurationSources + ", beanClass=" + beanClass.getSimpleName()
				+ ", constrainableElements=" + constrainableElements
				+ ", defaultGroupSequence=" + defaultGroupSequence
				+ ", defaultGroupSequenceProvider="
				+ defaultGroupSequenceProvider + "]";
	}

}
