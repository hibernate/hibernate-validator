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
 * Configuration<?> configuration = //provided by one of the Validation bootstrap methods
 *     ValidatorFactory = configuration
 *         .messageInterpolator( new CustomMessageInterpolator() )
 *         .buildValidatorFactory();
 * </pre>
 * <p/>
 *
 * By default, the configuration information is retrieved from
 * META-INF/validation.xml
 * It is possible to override the configuration retrieved from the XML file
 * by using one or more of the Configuration methods.
 *
 * The ValidationProviderResolver is specified at Configuration time
 * (see {@link javax.validation.spi.ValidationProvider}).
 * If none is explicitely requested, the default ValidationProviderResolver is used.
 * <p/>
 * The provider is selected in the following way:
 * - if a specific Configuration subclass is requested programmatically using
 * Validation.byProvider(Class), find the first provider matching it
 * - if a specific Configuration subclass is defined in META-INF/validation.xml,
 * find the first provider matching it
 * - otherwise, use the first provider returned by the ValidationProviderResolver
 * <p/>
 * Implementations are not meant to be thread-safe
 *
 * @author Emmanuel Bernard
 */
public interface Configuration<T extends Configuration<T>> {

	/**
	 * Ignore data from the META-INF/validation.xml file if this
	 * method is called.
	 * This method is typically useful for containers that parse
	 * META-INF/validation.xml themselves and pass the information
	 * via the Configuration methods.
	 */
	T ignoreXmlConfiguration();
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
	 * Defines the constraint validator factory. Has priority over the configuration
	 * based constraint factory.
	 *
	 * @param constraintValidatorFactory constraint factory inmplementation.
	 *
	 * @return <code>this</code> following the chaining method pattern.
	 */
	T constraintValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory);

	/**
	 * Add a stream describing constaint mapping in the Bean Validation
	 * XML format.
	 * <p/>
	 * The stream should be closed by the client API after the
	 * ValidatorFactory has been built. The Bean Validation provider
	 * must not close the stream.
	 *
	 * @param stream XML mapping stream.
	 *
	 * @return <code>this</code> following the chaining method pattern.
	 */
	T addMapping(InputStream stream);

	/**
	 * Add a provider specific property. This property is equivalent to
	 * XML Configuration properties.
	 * If the underlying provider does not know how to handle the property,
	 * it must silently ignore it.
	 *
	 * Note: Using this non type-safe method is generally not recommended.
	 *
	 * It is more appropriate to use, if available, the type-safe equivalent provided
	 * by a specific provider in its Configuration subclass.
	 * <code>ValidatorFactory factory = Validation.byProvider(ACMEConfiguration.class)
	 *        .configure()
	 *           .providerSpecificProperty(ACMEState.FAST)
	 *           .buildValidatorFactory();
	 * </code>
	 * This method is typically used by containers parsing META-INF/validation.xml
	 * themselves and injecting the state to the Configuration object.
	 *
	 * If a property with a given name is defined both via this method and in the
	 * XML configuration, the value set programmatically has priority.
	 */
	T addProperty(String name, String value);

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
	ValidatorFactory buildValidatorFactory();
}
