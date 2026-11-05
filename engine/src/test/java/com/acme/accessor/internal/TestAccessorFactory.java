package com.acme.accessor.internal;

import org.hibernate.validator.test.internal.engine.AccessorFactoryConfigurationTest.TrackingAccessorFactory;

/**
 * Uses an application namespace so class loading goes through the configured external class loader.
 */
public class TestAccessorFactory extends TrackingAccessorFactory {
}
