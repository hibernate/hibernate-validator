/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.cdi.service;

/**
 * @author Hardy Ferentschik
 */
public class PingServiceImpl implements PingService {
	@Override
	public String ping() {
		return "pong";
	}
}
