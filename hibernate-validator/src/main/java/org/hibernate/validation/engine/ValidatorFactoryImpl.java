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

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Set;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;
import javax.validation.ValidationException;
import javax.validation.spi.ConfigurationState;

import org.hibernate.validation.engine.xml.AnnotationIgnores;
import org.hibernate.validation.engine.xml.XmlMappingParser;

/**
 * Factory returning initialized <code>Validator</code> instances.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class ValidatorFactoryImpl implements ValidatorFactory {

	private final MessageInterpolator messageInterpolator;
	private final TraversableResolver traversableResolver;
	private final ConstraintValidatorFactory constraintValidatorFactory;
	private final ConstraintHelper constraintHelper;
	private final BeanMetaDataCache beanMetaDataCache;

	public ValidatorFactoryImpl(ConfigurationState configurationState) {
		this.messageInterpolator = configurationState.getMessageInterpolator();
		this.constraintValidatorFactory = configurationState.getConstraintValidatorFactory();
		this.traversableResolver = configurationState.getTraversableResolver();
		this.constraintHelper = new ConstraintHelper();
		this.beanMetaDataCache = new BeanMetaDataCache();

		initBeanMetaData( configurationState.getMappingStreams() );
	}

	public Validator getValidator() {
		return usingContext().getValidator();
	}

	public MessageInterpolator getMessageInterpolator() {
		return messageInterpolator;
	}

	public <T> T unwrap(Class<T> type) {
		throw new ValidationException( "Type " + type + " not supported");
	}

	public ValidatorContext usingContext() {
		return new ValidatorContextImpl(
				constraintValidatorFactory,
				messageInterpolator,
				traversableResolver,
				constraintHelper,
				beanMetaDataCache
		);
	}

	private <T> void initBeanMetaData(Set<InputStream> mappingStreams) {

		XmlMappingParser mappingParser = new XmlMappingParser( constraintHelper );
		mappingParser.parse( mappingStreams );

		AnnotationIgnores annotationIgnores = mappingParser.getAnnotationIgnores();
		for ( Class<?> beanClass : mappingParser.getProcessedClasses() ) {
			BeanMetaDataImpl<T> metaData = new BeanMetaDataImpl<T>(
					( Class<T> ) beanClass, constraintHelper, annotationIgnores
			);

			for ( MetaConstraint<T, ? extends Annotation> constraint : mappingParser.getConstraintsForClass( ( Class<T> ) beanClass ) ) {
				metaData.addMetaConstraint( constraint );
			}

			for ( Member m : mappingParser.getCascadedMembersForClass( beanClass ) ) {
				metaData.addCascadedMember( m );
			}

			if ( !mappingParser.getDefaultSequenceForClass( beanClass ).isEmpty() ) {
				metaData.setDefaultGroupSequence( mappingParser.getDefaultSequenceForClass( beanClass ) );
			}
			beanMetaDataCache.addBeanMetaData( ( Class<T> ) beanClass, ( BeanMetaDataImpl<T> ) metaData );
		}
	}
}
