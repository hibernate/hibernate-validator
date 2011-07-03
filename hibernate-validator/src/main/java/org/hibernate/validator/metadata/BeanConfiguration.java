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

import java.lang.reflect.Member;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.group.DefaultGroupSequenceProvider;

/**
 * @author Gunnar Morling
 */
public class BeanConfiguration<T> {

	private final Class<T> beanClass;

	private final Set<BeanMetaConstraint<?>> constraints;

	private final Set<Member> cascadedMembers;

	private final Set<MethodMetaData> methodMetaData;

	private List<Class<?>> defaultGroupSequence;

	private Class<? extends DefaultGroupSequenceProvider<?>> defaultGroupSequenceProvider;

	/**
	 * @param beanClass
	 * @param constraints
	 * @param cascadedMembers
	 * @param defaultGroupSequence
	 */
	public BeanConfiguration(Class<T> beanClass,
							 Set<BeanMetaConstraint<?>> constraints,
							 Set<Member> cascadedMembers,
							 Set<MethodMetaData> methodMetaData,
							 List<Class<?>> defaultGroupSequence,
							 Class<? extends DefaultGroupSequenceProvider<?>> defaultGroupSequenceProvider) {

		this.beanClass = beanClass;
		this.constraints = constraints;
		this.cascadedMembers = cascadedMembers;
		this.methodMetaData = methodMetaData;
		this.defaultGroupSequence = defaultGroupSequence;
		this.defaultGroupSequenceProvider = defaultGroupSequenceProvider;
	}

	public Class<T> getBeanClass() {
		return beanClass;
	}

	public Set<BeanMetaConstraint<?>> getConstraints() {
		return constraints;
	}

	public Set<Member> getCascadedMembers() {
		return cascadedMembers;
	}

	public Set<MethodMetaData> getMethodMetaData() {
		return methodMetaData;
	}

	public List<Class<?>> getDefaultGroupSequence() {
		return defaultGroupSequence;
	}

	public Class<? extends DefaultGroupSequenceProvider<?>> getDefaultGroupSequenceProvider() {
		return defaultGroupSequenceProvider;
	}

	public void merge(BeanConfiguration<T> other) {

		constraints.addAll( other.getConstraints() );
		cascadedMembers.addAll( other.getCascadedMembers() );
		methodMetaData.addAll( other.getMethodMetaData() );

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
		return "BeanConfiguration [beanClass=" + beanClass + ", constraints="
				+ constraints + ", cascadedMembers=" + cascadedMembers
				+ ", methodMetaData=" + methodMetaData
				+ ", defaultGroupSequence=" + defaultGroupSequence
				+ ", defaultGroupSequenceProvider="
				+ defaultGroupSequenceProvider + "]";
	}

}
