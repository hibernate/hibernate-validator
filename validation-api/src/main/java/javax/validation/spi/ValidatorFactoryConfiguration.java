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
package javax.validation.spi;

import java.io.InputStream;
import javax.validation.ConstraintFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;

/**
 * Contract between a <code>ValidatorFactoryBuilder</code> and a
 * </code>ValidatorProvider</code> to create a <code>ValidatorFactory</code>.
 * The configuration artifacts provided to the
 * <code>ValidatorFactoryBuilder</code> are passed along.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public interface ValidatorFactoryConfiguration {
	/**
	 * Message interpolator as defined by the client programmatically
	 * or null if undefined.
	 *
	 * @return message provider instance or null if not defined
	 */
	MessageInterpolator getMessageInterpolator();

	/**
	 * Returns the configuration stream defined by the client programmatically
	 * or null if undefined.
	 *
	 * @return the configuration input stream or null
	 */
	InputStream getConfigurationStream();

	/**
	 * Defines the constraint implementation factory as defined by
	 * the client programmatically or null if undefined
	 *
	 * @return factory instance or null if not defined
	 */
	ConstraintFactory getConstraintFactory();

	/**
	 * Traversable resolver as defined by the client programmatically
	 * or null if undefined.
	 *
	 * @return traversable provider instance or null if not defined
	 */
	TraversableResolver getTraversableResolver();
}
