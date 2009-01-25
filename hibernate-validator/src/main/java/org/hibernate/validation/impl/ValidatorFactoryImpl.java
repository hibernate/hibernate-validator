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
package org.hibernate.validation.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.ConstraintFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorBuilder;
import javax.validation.spi.ValidatorFactoryConfiguration;

import org.hibernate.validation.engine.BeanMetaDataImpl;
import org.hibernate.validation.engine.ValidatorFactoryImplementor;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class ValidatorFactoryImpl implements ValidatorFactoryImplementor {
	
	private final MessageInterpolator messageInterpolator;
	private final TraversableResolver traversableResolver;
	private final ConstraintFactory constraintFactory;

	//TODO is there a way to replace ? by so kind of <T> to express the correlation?
	private Map<Class<?>, BeanMetaDataImpl<?>> metadataProviders
			= new ConcurrentHashMap<Class<?>, BeanMetaDataImpl<?>>(10);


	public ValidatorFactoryImpl(ValidatorFactoryConfiguration configuration) {
		this.messageInterpolator = configuration.getMessageInterpolator();
		this.constraintFactory = configuration.getConstraintFactory();
		this.traversableResolver = configuration.getTraversableResolver();
		//do init metadata from XML form
	}

	/**
	 * {@inheritDoc}
	 */
	public Validator getValidator() {
		return defineValidatorState().getValidator();
	}

	public MessageInterpolator getMessageInterpolator() {
		return messageInterpolator;
	}

	public ValidatorBuilder defineValidatorState() {
		return new ValidatorBuilderImpl(this, messageInterpolator, traversableResolver);
	}

	public <T> BeanMetaDataImpl<T> getBeanMetaData(Class<T> beanClass) {
		//FIXME make sure a optimized mock is provided when no constraints are present.
		if (beanClass == null) throw new IllegalArgumentException( "Class cannot be null" );
		@SuppressWarnings( "unchecked")
		BeanMetaDataImpl<T> metadata = ( BeanMetaDataImpl<T> ) metadataProviders.get(beanClass);
		if (metadata == null) {
			metadata = new BeanMetaDataImpl<T>(beanClass, messageInterpolator, constraintFactory);
			metadataProviders.put( beanClass, metadata );
		}
		return metadata;
	}
}
