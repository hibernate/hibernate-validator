/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.stax.mapping;

import java.util.Optional;

/**
 * Builder for default package. Provides a default package name as defined in the xml
 * or {@link Optional#empty()} if none was provided.
 *
 * @author Marko Bekhta
 */
public class DefaultPackageStaxBuilder extends AbstractOneLineStringStaxBuilder {

	private static final String DEFAULT_PACKAGE_QNAME = "default-package";

	@Override
	protected String getAcceptableQname() {
		return DEFAULT_PACKAGE_QNAME;
	}

}
