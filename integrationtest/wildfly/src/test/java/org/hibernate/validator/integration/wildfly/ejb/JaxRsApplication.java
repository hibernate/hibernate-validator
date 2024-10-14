/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.wildfly.ejb;

import java.util.Collections;
import java.util.Set;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("")
public class JaxRsApplication extends Application {
	@Override
	public Set<Class<?>> getClasses() {
		return Collections.emptySet();
	}
}
