/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import java.util.List;
import java.util.Map;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

/**
 * Builder for group conversions.
 *
 * @author Hardy Ferentschik
 */
class GroupConversionBuilder {

	private GroupConversionBuilder() {
	}

	static Map<Class<?>, Class<?>> buildGroupConversionMap(List<GroupConversionType> groupConversionTypes,
																  String defaultPackage, ClassLoader userClassLoader) {
		Map<Class<?>, Class<?>> groupConversionMap = newHashMap();
		for ( GroupConversionType groupConversionType : groupConversionTypes ) {
			Class<?> fromClass = ClassLoadingHelper.loadClass( groupConversionType.getFrom(), defaultPackage, userClassLoader );
			Class<?> toClass = ClassLoadingHelper.loadClass( groupConversionType.getTo(), defaultPackage, userClassLoader );
			groupConversionMap.put( fromClass, toClass );
		}

		return groupConversionMap;
	}
}
