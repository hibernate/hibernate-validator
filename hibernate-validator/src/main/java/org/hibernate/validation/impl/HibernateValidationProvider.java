// $Id: HibernateValidationProvider.java 105 2008-09-29 12:37:32Z hardy.ferentschik $
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
import javax.validation.ValidatorBuilder;
import javax.validation.ValidatorFactory;
import javax.validation.spi.ValidationProvider;
import javax.validation.spi.ValidatorBuilderImplementor;
import javax.validation.spi.BootstrapState;

import org.hibernate.validation.HibernateValidatorBuilder;

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
	public boolean isSuitable(Class<? extends ValidatorBuilder<?>> builderClass) {
		return builderClass == HibernateValidatorBuilder.class;
	}

	public <T extends ValidatorBuilder<T>> T createSpecializedValidatorBuilder(BootstrapState state, Class<T> builderClass) {
		if ( ! isSuitable( builderClass ) ) {
			throw new ValidationException("Illegal call to createSpecializedValidatorBuilder() for a non suitable provider");
		}
		//cast protected  by isSuitable call
		return builderClass.cast( new ValidatorBuilderImpl( this ) );
	}

	/**
	 * {@inheritDoc}
	 */
	public ValidatorBuilder<?> createGenericValidatorBuilder(BootstrapState state) {
		return new ValidatorBuilderImpl( state );
	}

	/**
	 * {@inheritDoc}
	 */
	public ValidatorFactory buildValidatorFactory(ValidatorBuilderImplementor validatorBuilder) {
		return new ValidatorFactoryImpl( validatorBuilder );
	}
}
