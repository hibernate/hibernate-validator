/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.aspectj.validation.internal.util.logging;

import static org.jboss.logging.Logger.Level.INFO;

import org.hibernate.validator.aspectj.validation.internal.util.logging.formatter.ClassObjectFormatter;
import org.hibernate.validator.aspectj.validation.spi.ValidatorFactoryProducer;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.FormatWith;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * The Hibernate Validator AspectJ Validation logger interface for JBoss Logging.
 * <p>
 * New log messages must always use an incremented message id.
 *
 * @author Marko Bekhta
 */
@MessageLogger(projectCode = "HVAJV")
public interface Log extends BasicLogger {

	@LogMessage(level = INFO)
	@Message(id = 1, value = "No ValidatorFactoryProducers were found. Using a default ValidatorFactory.")
	void defaultValidatorUsage();

	@LogMessage(level = INFO)
	@Message(id = 2, value = "Multiple ValidatorFactoryProducers were found. Check your configuration. Using one of the found producers (%s).")
	void multipleValidatorFactoryProducers(Class<? extends ValidatorFactoryProducer> aClass);

	@Message(id = 3, value = "Unable to create ValidatorFactory using %1$s producer.")
	IllegalStateException errorCreatingValidatorFactoryFromProducer(@FormatWith(ClassObjectFormatter.class) Class<? extends ValidatorFactoryProducer> aClass, @Cause Exception cause);

}
