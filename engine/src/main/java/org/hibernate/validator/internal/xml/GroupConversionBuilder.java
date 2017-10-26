/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

import javax.validation.groups.Default;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.xml.binding.GroupConversionType;

/**
 * Builder for group conversions.
 *
 * @author Hardy Ferentschik
 */
class GroupConversionBuilder {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final ClassLoadingHelper classLoadingHelper;

	GroupConversionBuilder(ClassLoadingHelper classLoadingHelper) {
		this.classLoadingHelper = classLoadingHelper;
	}

	Map<Class<?>, Class<?>> buildGroupConversionMap(List<GroupConversionType> groupConversionTypes,
																  String defaultPackage) {
		Map<Class<?>, Class<?>> groupConversionMap = newHashMap();
		for ( GroupConversionType groupConversionType : groupConversionTypes ) {
			Class<?> fromClass = groupConversionType.getFrom() == null ?
					Default.class :
					classLoadingHelper.loadClass( groupConversionType.getFrom(), defaultPackage );
			Class<?> toClass = classLoadingHelper.loadClass( groupConversionType.getTo(), defaultPackage );

			if ( groupConversionMap.containsKey( fromClass ) ) {
				throw LOG.getMultipleGroupConversionsForSameSourceException(
						fromClass,
						CollectionHelper.<Class<?>>asSet( groupConversionMap.get( fromClass ), toClass ) );
			}

			groupConversionMap.put( fromClass, toClass );
		}

		return groupConversionMap;
	}
}
