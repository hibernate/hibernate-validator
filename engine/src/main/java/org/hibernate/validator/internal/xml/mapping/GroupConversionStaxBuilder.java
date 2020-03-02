/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.validation.groups.Default;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.xml.AbstractStaxBuilder;

/**
 * Builder for group conversions.
 *
 * @author Marko Bekhta
 */
class GroupConversionStaxBuilder extends AbstractStaxBuilder {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final String GROUP_CONVERSION_TYPE_QNAME_LOCAL_PART = "convert-group";
	private static final QName FROM_QNAME = new QName( "from" );
	private static final QName TO_QNAME = new QName( "to" );

	private static final String DEFAULT_GROUP_NAME = Default.class.getName();

	private final ClassLoadingHelper classLoadingHelper;
	private final DefaultPackageStaxBuilder defaultPackageStaxBuilder;

	private final Map<String, List<String>> groupConversionRules;

	GroupConversionStaxBuilder(ClassLoadingHelper classLoadingHelper, DefaultPackageStaxBuilder defaultPackageStaxBuilder) {
		this.classLoadingHelper = classLoadingHelper;
		this.defaultPackageStaxBuilder = defaultPackageStaxBuilder;
		this.groupConversionRules = new HashMap<>();
	}

	@Override
	protected String getAcceptableQName() {
		return GROUP_CONVERSION_TYPE_QNAME_LOCAL_PART;
	}

	@Override
	protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) {
		StartElement startElement = xmlEvent.asStartElement();
		String from = readAttribute( startElement, FROM_QNAME ).orElse( DEFAULT_GROUP_NAME );
		String to = readAttribute( startElement, TO_QNAME ).get();
		groupConversionRules.merge(
				from,
				Collections.singletonList( to ),
				(v1, v2) -> Stream.concat( v1.stream(), v2.stream() ).collect( Collectors.toList() )
		);
	}

	Map<Class<?>, Class<?>> build() {
		String defaultPackage = defaultPackageStaxBuilder.build().orElse( "" );

		Map<Class<?>, List<Class<?>>> resultingMapping = groupConversionRules.entrySet().stream()
				.collect(
						// Using groupingBy collector to prevent possible loss of information
						// as a string value in `from` could possibly contain both qualified and non qualified
						// version of a same class from the default package.
						Collectors.groupingBy(
								entry -> classLoadingHelper.loadClass( entry.getKey(), defaultPackage ),
								Collectors.collectingAndThen(
										Collectors.toList(),
										entries -> entries.stream()
												.flatMap( entry -> entry.getValue().stream() )
												.map( className -> classLoadingHelper.loadClass( className, defaultPackage ) )
												.collect( Collectors.toList() )
								)

						)
				);
		// in case of any duplicates in conversion rules we need to throw an exception:
		for ( Map.Entry<Class<?>, List<Class<?>>> entry : resultingMapping.entrySet() ) {
			if ( entry.getValue().size() > 1 ) {
				throw LOG.getMultipleGroupConversionsForSameSourceException(
						entry.getKey(),
						entry.getValue()
				);
			}
		}

		return resultingMapping.entrySet().stream()
				.collect( Collectors.toMap(
						Map.Entry::getKey,
						entry -> entry.getValue().get( 0 )
				) );
	}
}
