/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.tckrunner.securitymanager.arquillian;

import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.test.impl.client.protocol.local.LocalProtocol;
import org.jboss.arquillian.container.test.impl.client.protocol.local.LocalProtocolConfiguration;
import org.jboss.arquillian.container.test.spi.ContainerMethodExecutor;
import org.jboss.arquillian.container.test.spi.command.CommandCallback;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;

/**
 * Extension of {@link LocalProtocol} which uses a special executor,
 * {@link LocalSecurityManagerTestingContainerMethodExecutor}.
 *
 * @author Gunnar Morling
 */
public class LocalSecurityManagerTestingProtocol extends LocalProtocol {

	public static final String NAME = "LocalSecurityManagerTesting";

	@Inject
	private Instance<Injector> injector;

	@Override
	public ProtocolDescription getDescription() {
		return new ProtocolDescription( NAME );
	}

	@Override
	public ContainerMethodExecutor getExecutor(LocalProtocolConfiguration protocolConfiguration, ProtocolMetaData metaData, CommandCallback callback) {
		return injector.get().inject( new LocalSecurityManagerTestingContainerMethodExecutor() );
	}
}
