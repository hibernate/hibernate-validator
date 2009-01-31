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
import java.util.Set;
import java.util.Map;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;

/**
 * Contract between a <code>Configuration</code> and a
 * </code>ValidatorProvider</code> to create a <code>ValidatorFactory</code>.
 * The configuration artifacts defined in the XML configuration and provided to the
 * <code>Configuration</code> are merged and passed along via ConfigurationState.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public interface ConfigurationState {

	/**
	 * returns true if Configuration.ignoreXMLConfiguration() has been called
	 * In this case, the ValiatorFactory must ignore META-INF/validation.xml
	 * @return true if META-INF/validation.xml should be ignored
	 */
	boolean isIgnoreXmlConfiguration();

	/**
	 * Message interpolator as defined in the following decresing priority:
	 *  - set via the Configuration programmatic API
	 *  - defined in META-INF/validation.xml provided that ignoredXmlConfiguration
	 * is false. In this case the instance is created via its no-arg constructor.
	 *  - null if undefined.
	 *
	 * @return message provider instance or null if not defined
	 */
	MessageInterpolator getMessageInterpolator();

	/**
	 * Returns a set of stream corresponding to:
	 *  - mapping XML streams passed programmatically in Configuration
	 *  - mapping XML stream located in the resources defined in
	 * META-INF/validation.xml (constraint-mapping element)
	 *
	 * Streams represented in the XML configuration and opened by the 
	 * configuration implementation must be closed by the configuration
	 * implementation after the ValidatorFactory creation (or if an exception
	 * occurs).
	 *
	 * @return set of input stream
	 */
	Set<InputStream> getMappingStreams();

	/**
	 * ConstraintValidatorFactory implementation as defined in the following
	 * decreasing priority:
	 *  - set via the Configuration programmatic API
	 *  - defined in META-INF/validation.xml provided that ignoredXmlConfiguration
	 * is false. In this case the instance is created via its no-arg constructor.
	 *  - null if undefined.
	 *
	 * @return factory instance or null if not defined
	 */
	ConstraintValidatorFactory getConstraintValidatorFactory();

	/**
	 * TraversableResolver as defined in the following decresing priority:
	 *  - set via the Configuration programmatic API
	 *  - defined in META-INF/validation.xml provided that ignoredXmlConfiguration
	 * is false. In this case the instance is created via its no-arg constructor.
	 *  - null if undefined.
	 *
	 * @return traversable provider instance or null if not defined
	 */
	TraversableResolver getTraversableResolver();

	/**
	 * return  non type-safe properties defined via:
	 *  - Configuration.addProperty(String, String)
	 *  - META-INF/validation.xml provided that ignoredXmlConfiguration
	 * is false.
	 *
	 * If a property is defined both programmatically and in XML,
	 * the value defined programmatically has priority 
	 *
	 * @return Map whose key is the property key and the value the property value
	 */
	Map<String, String> getProperties();
}
