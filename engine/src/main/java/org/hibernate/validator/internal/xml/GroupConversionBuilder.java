/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.xml;

import java.util.List;
import java.util.Map;

import org.hibernate.validator.internal.util.ReflectionHelper;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

/**
 * Builder for group conversions.
 *
 * @author Hardy Ferentschik
 */
public class GroupConversionBuilder {

	private GroupConversionBuilder() {
	}

	public static Map<Class<?>, Class<?>> buildGroupConversionMap(List<GroupConversionType> groupConversionTypes,
																  String defaultPackage) {
		Map<Class<?>, Class<?>> groupConversionMap = newHashMap();
		for ( GroupConversionType groupConversionType : groupConversionTypes ) {
			Class<?> fromClass = ReflectionHelper.loadClass( groupConversionType.getFrom(), defaultPackage );
			Class<?> toClass = ReflectionHelper.loadClass( groupConversionType.getTo(), defaultPackage );
			groupConversionMap.put( fromClass, toClass );
		}

		return groupConversionMap;
	}
}


