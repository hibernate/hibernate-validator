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
import javax.validation.MessageResolver;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorBuilder;
import javax.validation.spi.ValidatorFactoryConfiguration;

import org.hibernate.validation.engine.MetaDataProviderImpl;
import org.hibernate.validation.engine.ValidatorFactoryImplementor;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class ValidatorFactoryImpl implements ValidatorFactoryImplementor {

	private final MessageResolver messageResolver;
	private final TraversableResolver traversableResolver;
	private final ConstraintFactory constraintFactory;

	//TODO is there a way to replace ? by so kind of <T> to express the correlation?
	private Map<Class<?>, MetaDataProviderImpl<?>> metadataProviders
			= new ConcurrentHashMap<Class<?>, MetaDataProviderImpl<?>>(10);


	public ValidatorFactoryImpl(ValidatorFactoryConfiguration configuration) {
		this.messageResolver = configuration.getMessageResolver();
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

	public MessageResolver getMessageResolver() {
		return messageResolver;
	}

	public ValidatorBuilder defineValidatorState() {
		return new ValidatorBuilderImpl(this, messageResolver, traversableResolver);
	}

	public <T> MetaDataProviderImpl<T> getMetadataProvider(Class<T> beanClass) {
		//FIXME make sure a optimized mock is provided when no constraints are present.
		if (beanClass == null) throw new IllegalArgumentException( "Class cannot be null" );
		@SuppressWarnings( "unchecked")
		MetaDataProviderImpl<T> metadata = ( MetaDataProviderImpl<T> ) metadataProviders.get(beanClass);
		if (metadata == null) {
			metadata = new MetaDataProviderImpl<T>(beanClass, constraintFactory);
			metadataProviders.put( beanClass, metadata );
		}
		return metadata;
	}
}
