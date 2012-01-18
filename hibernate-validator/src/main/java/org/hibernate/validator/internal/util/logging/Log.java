/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.internal.util.logging;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.WARN;

/**
 * The logger interface for JBoss Logging.
 *
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2012 SERLI
 */
@MessageLogger(projectCode = "HV")
public interface Log extends BasicLogger {

	@LogMessage(level = INFO)
	@Message(id = 1, value = "Hibernate Validator %s")
	void version(String version);

	@LogMessage(level = INFO)
	@Message(id = 2, value = "Ignoring XML configuration.")
	void ignoringXmlConfiguration();

	@LogMessage(level = INFO)
	@Message(id = 3, value = "Using %s as constraint factory.")
	void usingConstraintFactory(String constraintFactoryClassName);

	@LogMessage(level = INFO)
	@Message(id = 4, value = "Using %s as message interpolator.")
	void usingMessageInterpolator(String messageInterpolatorClassName);

	@LogMessage(level = INFO)
	@Message(id = 5, value = "Using %s as traversable resolver.")
	void usingTraversableResolver(String traversableResolverClassName);

	@LogMessage(level = INFO)
	@Message(id = 6, value = "Using %s as validation provider.")
	void usingValidationProvider(String validationProviderClassName);

	@LogMessage(level = INFO)
	@Message(id = 7, value = "%s found. Parsing XML based configuration.")
	void parsingXMLFile(String fileName);

	@LogMessage(level = WARN)
	@Message(id = 8, value = "Unable to close input stream.")
	void unableToCloseInputStream();

	@LogMessage(level = WARN)
	@Message(id = 9, value = "Unable to load provider class %s.")
	void unableToLoadProviderClass(String providerName);

	@LogMessage(level = WARN)
	@Message(id = 10, value = "Unable to close input stream for %s.")
	void unableToCloseXMLFileInputStream(String fileName);

	@LogMessage(level = WARN)
	@Message(id = 11, value = "Unable to create schema for %1$s: %2$s")
	void unableToCreateSchema(String fileName, String message);
}
