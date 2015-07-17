/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.tckrunner.securitymanager.arquillian;

import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * An Arquillian extension which contributes a special protocol, {@link LocalSecurityManagerTestingProtocol}.
 * <p>
 * This protocol is essentially the same as
 * {@link org.jboss.arquillian.container.test.impl.client.protocol.local.LocalProtocol}, only that the execution of
 * tests is channeled through a specific protection domain (basically, "target/classes" of this module, or the
 * equivalent JAR). Specifying only minimal permissions to this protection domain allows for the identification of code
 * in the HV JAR which doesn't use privileged actions as required.
 *
 * @author Gunnar Morling
 */
public class LocalSecurityManagerTestingExtension implements LoadableExtension {

	@Override
	public void register(ExtensionBuilder builder) {
		builder.service( Protocol.class, LocalSecurityManagerTestingProtocol.class );
	}
}
