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

import java.util.List;
import javax.validation.spi.ValidationProvider;

/**
 * Determine the list of Bean Validation providers available in the runtime environment
 * <p/>
 * Bean Validation providers are identified by the presence of META-INF/services/javax.validation.spi.ValidationProvider
 * files following the Service Provider pattern described
 * <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jar/jar.html#Service%20Provider">here</a>
 * <p/>
 * Each META-INF/services/javax.validation.spi.ValidationProvider file contains the list of
 * ValidationProvider implementations each of them representing a provider.
 * <p/>
 * Implementations must be thread-safe.
 *
 * @author Emmanuel Bernard
 */
public interface ValidationProviderResolver {
	/**
	 * Returns a list of ValidationProviders available in the runtime environment.
	 *
	 * @return list of validation providers.
	 */
	List<ValidationProvider> getValidationProviders();
}
