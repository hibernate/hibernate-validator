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
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.Map;
import javax.validation.metadata.ElementDescriptor;
import javax.validation.metadata.ReturnValueDescriptor;

import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.facets.Validatable;

/**
 * Represents the constraint related meta data of the return value of a method
 * or constructor.
 *
 * @author Gunnar Morling
 */
public class ReturnValueMetaData implements Validatable, Cascadable {

	public static final String RETURN_VALUE_NODE_NAME = null;

	private final GroupConverter groupConverter;

	private final ReturnValueDescriptor descriptor;

	public ReturnValueMetaData(Map<Class<?>, Class<?>> groupConversions, ReturnValueDescriptor descriptor) {
		this.groupConverter = new GroupConverter( groupConversions );
		this.descriptor = descriptor;
	}

	@Override
	public Iterable<Cascadable> getCascadables() {
		return Arrays.<Cascadable>asList( this );
	}

	@Override
	public Class<?> convertGroup(Class<?> originalGroup) {
		return groupConverter.convertGroup( originalGroup );
	}

	@Override
	public ElementType getElementType() {
		return ElementType.METHOD;
	}

	@Override
	public Object getValue(Object parent) {
		return parent;
	}

	@Override
	public String getName() {
		return RETURN_VALUE_NODE_NAME;
	}

	@Override
	public ElementDescriptor getDescriptor() {
		return descriptor;
	}
}
