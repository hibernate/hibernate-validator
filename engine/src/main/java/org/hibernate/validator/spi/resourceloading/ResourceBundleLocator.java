/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.spi.resourceloading;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * <p>
 * Used by {@link org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator} to load resource bundles
 * containing message texts to be displayed in case of validation errors.
 * </p>
 * <p>
 * The default implementation provides access to the bundle "ValidationMessages"
 * as described in the BV specification. By providing additional implementations
 * of this interface, alternative ways of bundle loading can be realized, e.g.
 * by loading bundles based on XML files or from a database.
 * </p>
 * <p>
 * A {@code ResourceBundleLocator} implementation must be thread-safe.
 * </p>
 *
 * @author Gunnar Morling
 */
public interface ResourceBundleLocator {

	/**
	 * Returns a resource bundle for the given locale.
	 *
	 * @param locale A locale, for which a resource bundle shall be retrieved. Must
	 * not be null.
	 *
	 * @return A resource bundle for the given locale. May be null, if no such
	 *         bundle exists.
	 */
	ResourceBundle getResourceBundle(Locale locale);
}
