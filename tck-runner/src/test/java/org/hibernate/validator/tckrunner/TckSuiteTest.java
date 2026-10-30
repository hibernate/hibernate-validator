/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.tckrunner;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Runs the TestNG-based Jakarta Validation TCK on the JUnit Platform.
 * <p>
 * Replaces the TestNG suite XML file that Surefire no longer supports since 3.6.0.
 * Test filtering based on the {@code @IntegrationTest} and {@code @JavaFXTest}
 * annotations is handled by {@link TckTestsFilter}, which replaces the TestNG
 * method selectors that were declared in the suite XML file.
 */
@Suite
@SuiteDisplayName("Jakarta Validation TCK")
@SelectPackages("org.hibernate.beanvalidation.tck.tests")
@IncludeClassNamePatterns(".*")
public class TckSuiteTest {
}
