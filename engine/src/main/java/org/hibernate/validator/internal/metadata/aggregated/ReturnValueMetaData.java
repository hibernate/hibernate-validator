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

/**
 * Represents the constraint related meta data of the return value of a method
 * or constructor.
 *
 * @author Gunnar Morling
 */
public class ReturnValueMetaData implements Validatable {

	//TODO HV-571: The spec currently says "In the return value case, the name of the node is null".
	//But maybe a reserved name like this is actually better. Need to discuss with EG.
	public static final String RETURN_VALUE_NODE_NAME = "$retval";


	private final Map<Class<?>, Class<?>> groupConversions;

	public ReturnValueMetaData(Map<Class<?>, Class<?>> groupConversions) {
		this.groupConversions = groupConversions;
	}

	@Override
	public Iterable<Cascadable> getCascadables() {
		return Arrays.<Cascadable>asList( new ReturnValueCascadable( groupConversions ) );
	}

	private class ReturnValueCascadable implements Cascadable {

		private final GroupConverter groupConverter;

		public ReturnValueCascadable(Map<Class<?>, Class<?>> groupConversions) {
			this.groupConverter = new GroupConverter( groupConversions );
		}

		@Override
		public String getName() {
			return RETURN_VALUE_NODE_NAME;
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
	}
}
