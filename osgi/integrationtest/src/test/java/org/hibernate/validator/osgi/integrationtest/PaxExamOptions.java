/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.osgi.integrationtest;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.DefaultCompositeOption;

public enum PaxExamOptions {
	JAVA_9(
		CoreOptions.vmOptions(
			"--add-opens",
			"java.base/java.security=ALL-UNNAMED",
			"--add-opens",
			"java.base/java.net=ALL-UNNAMED",
			"--add-opens",
			"java.base/java.lang=ALL-UNNAMED",
			"--add-opens",
			"java.base/java.util=ALL-UNNAMED",
			"--add-exports=java.base/sun.net.www.protocol.http=ALL-UNNAMED",
			"--add-exports=java.base/sun.net.www.protocol.https=ALL-UNNAMED",
			"--add-exports=java.xml.bind/com.sun.xml.internal.bind.v2.runtime=ALL-UNNAMED",
			"--add-exports=jdk.xml.dom/org.w3c.dom.html=ALL-UNNAMED",
			"--add-exports=jdk.naming.rmi/com.sun.jndi.url.rmi=ALL-UNNAMED",
			"--add-exports=java.xml.ws/com.sun.xml.internal.messaging.saaj.soap.impl=ALL-UNNAMED",
			"--add-modules",
			"java.xml.ws.annotation,java.corba,java.transaction,java.xml.bind,java.xml.ws,jdk.xml.bind" )
	);

	private final Option options;

	PaxExamOptions(Option... options) {
		this.options = new DefaultCompositeOption( options );
	}

	public Option options() {
		return options;
	}
}
