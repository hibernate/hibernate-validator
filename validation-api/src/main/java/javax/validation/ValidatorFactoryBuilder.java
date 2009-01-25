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
package javax.validation;

import java.io.InputStream;

/**
 * Receives configuration information, selects the appropriate
 * Bean Validation provider and build the appropriate
 * ValidatorFactory.
 * <p/>
 * Usage:
 * <pre>
 * ValidatorFactoryBuilder<?> builder = //provided by one of the Validation bootstrap methods
 * ValidatorFactory = builder
 *         .messageInterpolator( new CustomMessageInterpolator() )
 *         .build();
 * </pre>
 * <p/>
 * The ValidationProviderResolver is specified at ValidatorFactoryBuilder time
 * (see {@link javax.validation.spi.ValidationProvider}).
 * If none is explicitely requested, the default ValidationProviderResolver is used.
 * <p/>
 * The provider is selected in the following way:
 * - if a specific ValidatorFactoryBuilder subclass is requested programmatically using
 * Validation.builderType(), find the first provider matching it
 * - if a specific ValidatorFactoryBuilder subclass is defined in META-INF/validation.xml,
 * find the first provider matching it
 * - otherwise, use the first provider returned by the ValidationProviderResolver
 * <p/>
 * Implementations are not meant to be thread-safe
 *
 * @author Emmanuel Bernard
 */
public interface ValidatorFactoryBuilder<T extends ValidatorFactoryBuilder> {
	/**
	 * Defines the message interpolator used. Has priority over the configuration
	 * based message interpolator.
	 *
	 * @param interpolator message interpolator implementation.
	 *
	 * @return <code>this</code> following the chaining method pattern.
	 */
	T messageInterpolator(MessageInterpolator interpolator);

	/**
	 * Defines the traversable resolver used. Has priority over the configuration
	 * based traversable resolver.
	 *
	 * @param resolver traversable resolver implementation.
	 *
	 * @return <code>this</code> following the chaining method pattern.
	 */
	T traversableResolver(TraversableResolver resolver);

	/**
	 * Defines the constraint factory. Has priority over the configuration
	 * based constraint factory.
	 *
	 * @param constraintFactory constraint factory inmplementation.
	 *
	 * @return <code>this</code> following the chaining method pattern.
	 */
	T constraintFactory(ConstraintFactory constraintFactory);

	/**
	 * Configure the ValidatorFactory based on <code>stream</code>
	 * If not specified, META-INF/validation.xml is used
	 * <p/>
	 * The stream should be closed by the client API after the
	 * ValidatorFactory has been returned
	 *
	 * @param stream configuration stream.
	 *
	 * @return <code>this</code> following the chaining method pattern.
	 */
	T configure(InputStream stream);

	/**
	 * Return an implementation of the MessageInterpolator interface following the
	 * default MessageInterpolator defined in the specification:
	 *  - use the ValidationMessages resource bundle to load keys
	 *  - use Locale.getDefault()
	 *
	 * @return default MessageInterpolator implementation compliant with the specification
	 */
	MessageInterpolator getDefaultMessageInterpolator();

	/**
	 * Build a ValidatorFactory implementation.
	 *
	 * @return ValidatorFactory
	 */
	ValidatorFactory build();
}
