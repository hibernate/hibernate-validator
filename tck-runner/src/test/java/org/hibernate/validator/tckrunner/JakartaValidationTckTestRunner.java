/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.tckrunner;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Jakarta Validation TCK tests Runner")
// Defines a "root" package, subpackages are included.
// Use Include/Exclude ClassNamePatterns annotations to limit the executed tests:
@SelectPackages({ "org.hibernate.beanvalidation.tck.tests" })
@IncludeClassNamePatterns({ ".*" })
public class JakartaValidationTckTestRunner {
}
