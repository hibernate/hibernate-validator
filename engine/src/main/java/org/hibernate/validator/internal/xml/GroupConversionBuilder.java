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

	private final ClassLoadingHelper classLoadingHelper;

	GroupConversionBuilder(ClassLoadingHelper classLoadingHelper) {
		this.classLoadingHelper = classLoadingHelper;
	}

	Map<Class<?>, Class<?>> buildGroupConversionMap(List<GroupConversionType> groupConversionTypes,
																  String defaultPackage) {
		Map<Class<?>, Class<?>> groupConversionMap = newHashMap();
		for ( GroupConversionType groupConversionType : groupConversionTypes ) {
			Class<?> fromClass = classLoadingHelper.loadClass( groupConversionType.getFrom(), defaultPackage );
			Class<?> toClass = classLoadingHelper.loadClass( groupConversionType.getTo(), defaultPackage );
			groupConversionMap.put( fromClass, toClass );
		}

		return groupConversionMap;
	}
}
