/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal.beanmetadataclassnormalizer;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

public class CustomProxyBeanMetadataClassNormalizerCdiExtension implements Extension {

	public void addEjbProxyNormalizer(@Observes AfterBeanDiscovery afterBeanDiscovery) {
		afterBeanDiscovery.addBean( new CustomProxyBeanMetaDataClassNormalizer() );
	}
}
