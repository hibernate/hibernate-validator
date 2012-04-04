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
package org.hibernate.validator.resourceloading;

import java.util.Locale;
import java.util.ResourceBundle;

import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

/**
 * Abstract base for all {@link ResourceBundleLocator} implementations, that
 * wish to delegate to some other locator.
 *
 * @author Gunnar Morling
 */
@SuppressWarnings("deprecation")
public abstract class DelegatingResourceBundleLocator
		implements org.hibernate.validator.resourceloading.ResourceBundleLocator, org.hibernate.validator.spi.resourceloading.ResourceBundleLocator {

	private final ResourceBundleLocator delegate;

	/**
	 * Creates a new {@link DelegatingResourceBundleLocator}.
	 *
	 * @param delegate The delegate.
	 *
	 * @deprecated Will be removed in a future release. Use
	 *             {@link #DelegatingResourceBundleLocator(org.hibernate.validator.spi.resourceloading.ResourceBundleLocator)}
	 *             instead.
	 */
	@Deprecated
	public DelegatingResourceBundleLocator(org.hibernate.validator.resourceloading.ResourceBundleLocator delegate) {
		this.delegate = new Adapter( delegate );
	}

	/**
	 * Creates a new {@link DelegatingResourceBundleLocator}.
	 *
	 * @param delegate The delegate.
	 */
	public DelegatingResourceBundleLocator(org.hibernate.validator.spi.resourceloading.ResourceBundleLocator delegate) {
		this.delegate = delegate;
	}

	public ResourceBundle getResourceBundle(Locale locale) {
		return delegate == null ? null : delegate.getResourceBundle( locale );
	}

	private static class Adapter implements org.hibernate.validator.spi.resourceloading.ResourceBundleLocator {

		private final org.hibernate.validator.resourceloading.ResourceBundleLocator adaptee;

		public Adapter(org.hibernate.validator.resourceloading.ResourceBundleLocator adaptee) {
			this.adaptee = adaptee;
		}

		public ResourceBundle getResourceBundle(Locale locale) {
			return adaptee.getResourceBundle( locale );
		}
	}
}
