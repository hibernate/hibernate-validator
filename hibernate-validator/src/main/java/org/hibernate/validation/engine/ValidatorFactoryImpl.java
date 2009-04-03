// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;
import javax.validation.spi.ConfigurationState;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.xml.sax.SAXException;

import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.ReflectionHelper;
import org.hibernate.validation.xml.BeanType;
import org.hibernate.validation.xml.ClassType;
import org.hibernate.validation.xml.ConstraintMappingsType;
import org.hibernate.validation.xml.FieldType;
import org.hibernate.validation.xml.GetterType;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @todo Is this the right place to parse the mapping files?
 */
public class ValidatorFactoryImpl implements ValidatorFactory {

	private static final Logger log = LoggerFactory.make();
	private static final String VALIDATION_MAPPING_XSD = "META-INF/validation-mapping-1.0.xsd";

	private final MessageInterpolator messageInterpolator;
	private final TraversableResolver traversableResolver;
	private final ConstraintValidatorFactory constraintValidatorFactory;
	private final ConstraintHelper constraintHelper = new ConstraintHelper();
	private static final String PACKAGE_SEPERATOR = ".";

	public ValidatorFactoryImpl(ConfigurationState configurationState) {
		this.messageInterpolator = configurationState.getMessageInterpolator();
		this.constraintValidatorFactory = configurationState.getConstraintValidatorFactory();
		this.traversableResolver = configurationState.getTraversableResolver();

		parseMappingFiles( configurationState.getMappingStreams() );

	}

	/**
	 * {@inheritDoc}
	 */
	public Validator getValidator() {
		return usingContext().getValidator();
	}

	/**
	 * {@inheritDoc}
	 */
	public MessageInterpolator getMessageInterpolator() {
		return messageInterpolator;
	}

	/**
	 * {@inheritDoc}
	 */
	public ValidatorContext usingContext() {
		return new ValidatorContextImpl(
				constraintValidatorFactory, messageInterpolator, traversableResolver,
				constraintHelper
		);
	}

	public Schema getMappingSchema() {
		URL schemaUrl = this.getClass().getClassLoader().getResource( VALIDATION_MAPPING_XSD );
		SchemaFactory sf = SchemaFactory.newInstance( javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI );
		Schema schema = null;
		try {
			schema = sf.newSchema( schemaUrl );
		}
		catch ( SAXException e ) {
			log.warn( "Unable to create schema for {}: {}", VALIDATION_MAPPING_XSD, e.getMessage() );
		}
		return schema;
	}

	private void parseMappingFiles(Set<InputStream> mappingStreams) {
		Set<Class<?>> processedClasses = new HashSet<Class<?>>();
		for ( InputStream in : mappingStreams ) {
			try {
				ConstraintMappingsType mappings = getValidationConfig( in );
				String defaultPackage = mappings.getDefaultPackage();
				for ( BeanType bean : mappings.getBean() ) {
					Class<?> beanClass = getBeanClass( bean.getClazz(), defaultPackage );
					if ( processedClasses.contains( beanClass ) ) {
						throw new ValidationException( beanClass.getName() + " has already be configured in xml." );
					}
					boolean ignoreAnnotations = bean.isIgnoreAnnotations();
					@SuppressWarnings("unchecked")
					BeanMetaDataImpl<?> metaData = new BeanMetaDataImpl( beanClass, constraintHelper );
					parseClassLevelOverrides( metaData, bean.getClassType() );
					parseFieldLevelOverrides( metaData, bean.getField() );
					parsePropertyLevelOverrides( metaData, bean.getGetter() );
					processedClasses.add( beanClass );
				}
			}
			finally {
				try {
					in.close();
				}
				catch ( IOException e ) {
					log.warn( "Error closing input stream: {}", e.getMessage() );
				}
			}
		}
	}

	private void parseFieldLevelOverrides(BeanMetaDataImpl<?> metaData, List<FieldType> field) {
		//To change body of created methods use File | Settings | File Templates.
	}

	private void parsePropertyLevelOverrides(BeanMetaDataImpl<?> metaData, List<GetterType> getter) {
		//To change body of created methods use File | Settings | File Templates.
	}

	private void parseClassLevelOverrides(BeanMetaDataImpl<?> metaData, ClassType classType) {
		//To change body of created methods use File | Settings | File Templates.
	}

	private Class getBeanClass(String clazz, String defaultPackage) {
		String fullyQualifiedClass;
		if ( isQualifiedClass( clazz ) ) {
			fullyQualifiedClass = clazz;
		}
		else {
			fullyQualifiedClass = defaultPackage + PACKAGE_SEPERATOR + clazz;
		}
		try {
			return ReflectionHelper.classForName( fullyQualifiedClass, this.getClass() );
		}
		catch ( Exception e ) {
			throw new ValidationException( "Unable to instantiate class " + fullyQualifiedClass );
		}
	}

	private boolean isQualifiedClass(String clazz) {
		return clazz.contains( PACKAGE_SEPERATOR );
	}

	private ConstraintMappingsType getValidationConfig(InputStream in) {
		ConstraintMappingsType constraintMappings;
		Schema schema = getMappingSchema();
		try {
			JAXBContext jc = JAXBContext.newInstance( ConstraintMappingsType.class );
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			unmarshaller.setSchema( schema );
			StreamSource stream = new StreamSource( in );
			JAXBElement<ConstraintMappingsType> root = unmarshaller.unmarshal( stream, ConstraintMappingsType.class );
			constraintMappings = root.getValue();
		}
		catch ( JAXBException e ) {
			String msg = "Error parsing mapping file.";
			log.error( msg );
			throw new ValidationException( msg, e );
		}
		return constraintMappings;
	}
}
