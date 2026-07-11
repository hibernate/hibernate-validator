/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.tckrunner.securitymanager;

import org.hibernate.validator.internal.IgnoreForbiddenApisErrors;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * Main class for running the test suite programmatically, for debugging purposes.
 * <p>
 * Specify the following arguments when running this test:
 * <pre>
 * -Dvalidation.provider=org.hibernate.validator.HibernateValidator
 * -DexcludeIntegrationTests=true
 * -Darquillian.protocol=Local
 * </pre>
 *
 * @author Gunnar Morling
 */
public class TckRunner {

	@IgnoreForbiddenApisErrors(reason = "Console application that uses System.out and print stack traces.")
	public static void main(String[] args) {
		LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
				.selectors( DiscoverySelectors.selectPackage( "org.hibernate.beanvalidation.tck.tests" ) )
				.build();

		SummaryGeneratingListener listener = new SummaryGeneratingListener();
		Launcher launcher = LauncherFactory.create();
		launcher.registerTestExecutionListeners( listener );
		launcher.execute( request );

		TestExecutionSummary summary = listener.getSummary();


		System.out.println( "Tests run: " + summary.getTestsStartedCount()
				+ ", Failures: " + summary.getTestsFailedCount()
				+ ", Skipped: " + summary.getTestsSkippedCount() );

		for ( TestExecutionSummary.Failure failure : summary.getFailures() ) {
			System.out.println( "Failed: " + failure.getTestIdentifier().getDisplayName() );
			failure.getException().printStackTrace();
		}
	}
}
