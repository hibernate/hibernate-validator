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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ValidationException;

/**
 * Default <code>ConstraintValidatorFactory</code> using a no-arg constructor.
 *
 * @author Emmanuel Bernard
 */
public class ConstraintValidatorFactoryImpl implements ConstraintValidatorFactory {
	
	/**
	 * {@inheritDoc}
	 */
	public <T extends ConstraintValidator> T getInstance(Class<T> key) {
		try {
			return key.newInstance();
		}
		catch ( InstantiationException e ) {
			throw new ValidationException( "Unable to instanciate " + key, e );
		}
		catch ( IllegalAccessException e ) {
			throw new ValidationException( "Unable to instanciate " + key, e );
		}
	}
}
