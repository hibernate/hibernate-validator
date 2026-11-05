package org.hibernate.validator.test.cdi.internal.beanmetadataclassnormalizer;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

public class CustomProxyBeanMetadataClassNormalizerCdiExtension implements Extension {

	public void addEjbProxyNormalizer(@Observes AfterBeanDiscovery afterBeanDiscovery) {
		afterBeanDiscovery.addBean( new CustomProxyBeanMetaDataClassNormalizer() );
	}
}
