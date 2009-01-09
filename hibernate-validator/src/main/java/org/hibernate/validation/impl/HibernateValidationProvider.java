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

import javax.validation.ValidationException;
import javax.validation.ValidatorFactoryBuilder;
import javax.validation.ValidatorFactory;
import javax.validation.spi.ValidationProvider;
import javax.validation.spi.ValidatorFactoryConfiguration;
import javax.validation.spi.BootstrapState;

import org.hibernate.validation.HibernateValidatorFactoryBuilder;
import org.hibernate.validation.Version;

/**
 * Default implementation of <code>ValidationProvider</code> within Hibernate validator.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class HibernateValidationProvider implements ValidationProvider {

	/**
	 * {@inheritDoc}
	 */
	public boolean isSuitable(Class<? extends ValidatorFactoryBuilder<?>> builderClass) {
		return builderClass == HibernateValidatorFactoryBuilder.class;
	}

	public <T extends ValidatorFactoryBuilder<T>> T createSpecializedValidatorFactoryBuilder(BootstrapState state, Class<T> builderClass) {
		if ( !isSuitable( builderClass ) ) {
			throw new ValidationException(
					"Illegal call to createSpecializedValidatorFactoryBuilder() for a non suitable provider"
			);
		}
		//cast protected  by isSuitable call
		return builderClass.cast( new ValidatorFactoryBuilderImpl( this ) );
	}

	/**
	 * {@inheritDoc}
	 */
	public ValidatorFactoryBuilder<?> createGenericValidatorFactoryBuilder(BootstrapState state) {
		return new ValidatorFactoryBuilderImpl( state );
	}

	/**
	 * {@inheritDoc}
	 */
	public ValidatorFactory buildValidatorFactory(ValidatorFactoryConfiguration configuration) {
		return new ValidatorFactoryImpl( configuration );
	}
}
