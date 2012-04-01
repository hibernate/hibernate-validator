/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.engine.groups;

import java.util.List;

import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

/**
 * Adapts a {@link org.hibernate.validator.group.DefaultGroupSequenceProvider} to a {@link DefaultGroupSequenceProvider}.
 *
 * @author Gunnar Morling
 */
@SuppressWarnings("deprecation")
public class DefaultGroupSequenceProviderAdapter<T> implements DefaultGroupSequenceProvider<T> {

	private final org.hibernate.validator.group.DefaultGroupSequenceProvider<T> adaptee;

	public static <T> DefaultGroupSequenceProviderAdapter<T> getInstance(org.hibernate.validator.group.DefaultGroupSequenceProvider<T> adaptee) {
		return new DefaultGroupSequenceProviderAdapter<T>( adaptee );
	}

	private DefaultGroupSequenceProviderAdapter(org.hibernate.validator.group.DefaultGroupSequenceProvider<T> adaptee) {
		this.adaptee = adaptee;
	}

	public List<Class<?>> getValidationGroups(T object) {
		return adaptee.getValidationGroups( object );
	}
}
