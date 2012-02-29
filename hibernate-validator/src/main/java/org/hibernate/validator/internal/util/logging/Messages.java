package org.hibernate.validator.internal.util.logging;

import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;

/**
 * @author Hardy Ferentschik
 */
@MessageBundle(projectCode = "HV")
public interface Messages {
	/**
	 * The messages.
	 */
	Messages MESSAGES = org.jboss.logging.Messages.getBundle(Messages.class);

	@Message(value = "must not be null.")
	String mustNotBeNull();

	@Message(value = "%s must not be null.")
	String mustNotBeNull(String parameterName);

	@Message(value = "The parameter \"%s\" must not be null.")
	String parameterMustNotBeNull(String parameterName);

	@Message(value = "The parameter \"%s\" must not be empty.")
	String parameterMustNotBeEmpty(String parameterName);

	@Message(value = "The bean type cannot be null.")
	String beanTypeCannotBeNull();

	@Message(value = "null is not allowed as property path.")
	String propertyPathCannotBeNull();

	@Message(value = "The property name must not be empty.")
	String propertyNameMustNotBeEmpty();

	@Message(value = "null passed as group name.")
	String groupMustNotBeNull();

	@Message(value = "The bean type must not be null when creating a constraint mapping.")
	String beanTypeMustNotBeNull();

	@Message(value = "The method name must not be null.")
	String methodNameMustNotBeNull();

	@Message(value = "The object to be validated must not be null.")
	String validatedObjectMustNotBeNull();

	@Message(value = "The method to be validated must not be null.")
	String validatedMethodMustNotBeNull();

	@Message(value = "The class cannot be null.")
	String classCannotBeNull();

	@Message(value = "Class is null.")
	String classIsNull();

	@Message(value = "No JSR 223 script engine found for language \"%s\".")
	String unableToFindScriptEngine(String languageName);
}


