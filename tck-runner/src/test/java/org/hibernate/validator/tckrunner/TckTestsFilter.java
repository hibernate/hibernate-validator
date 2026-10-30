/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.tckrunner;

import java.util.Optional;

import org.hibernate.beanvalidation.tck.util.IntegrationTest;
import org.hibernate.beanvalidation.tck.util.JavaFXTest;

import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.PostDiscoveryFilter;

/**
 * Replaces the TestNG method selectors that were declared in the TCK suite XML file:
 * <ul>
 * <li>excludes tests from classes annotated with {@code @IntegrationTest}
 * when the system property {@code excludeIntegrationTests} is set to {@code true};</li>
 * <li>excludes tests from classes annotated with {@code @JavaFXTest}
 * unless the system property {@code includeJavaFXTests} is set to {@code true}.</li>
 * </ul>
 * <p>
 * Registered through {@code ServiceLoader} so that it applies to every test
 * discovery in this module, regardless of how the tests were selected.
 */
public class TckTestsFilter implements PostDiscoveryFilter {

	private final boolean excludeIntegrationTests = Boolean.getBoolean( "excludeIntegrationTests" );
	private final boolean includeJavaFXTests = Boolean.getBoolean( "includeJavaFXTests" );

	@Override
	public FilterResult apply(TestDescriptor descriptor) {
		Optional<Class<?>> testClass = findTestClass( descriptor );
		if ( testClass.isEmpty() ) {
			return FilterResult.included( "Not a test class" );
		}
		Class<?> clazz = testClass.get();
		if ( excludeIntegrationTests && clazz.isAnnotationPresent( IntegrationTest.class ) ) {
			return FilterResult.excluded( "@IntegrationTest test" );
		}
		if ( !includeJavaFXTests && clazz.isAnnotationPresent( JavaFXTest.class ) ) {
			return FilterResult.excluded( "@JavaFXTest test" );
		}
		return FilterResult.included( "Included test" );
	}

	/**
	 * Returns the test class of the given descriptor.
	 * <p>
	 * Class descriptors expose the test class directly through their {@link ClassSource}.
	 * Method descriptors only carry a {@code MethodSource} without the Java class
	 * (the TestNG engine creates them from strings), so in that case we walk up
	 * to the enclosing class descriptor to get the class without any reflection.
	 */
	private static Optional<Class<?>> findTestClass(TestDescriptor descriptor) {
		TestDescriptor current = descriptor;
		while ( current != null ) {
			Optional<TestSource> source = current.getSource();
			if ( source.isPresent() && source.get() instanceof ClassSource classSource ) {
				return Optional.ofNullable( classSource.getJavaClass() );
			}
			current = current.getParent().orElse( null );
		}
		return Optional.empty();
	}
}
