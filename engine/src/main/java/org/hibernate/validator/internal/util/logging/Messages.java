/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
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
	Messages MESSAGES = org.jboss.logging.Messages.getBundle( Messages.class );

	@Message(value = "must not be null.", format = Message.Format.NO_FORMAT)
	String mustNotBeNull();

	@Message(value = "%s must not be null.")
	String mustNotBeNull(String parameterName);

	@Message(value = "The parameter \"%s\" must not be null.")
	String parameterMustNotBeNull(String parameterName);

	@Message(value = "The parameter \"%s\" must not be empty.")
	String parameterMustNotBeEmpty(String parameterName);

	@Message(value = "The bean type cannot be null.", format = Message.Format.NO_FORMAT)
	String beanTypeCannotBeNull();

	@Message(value = "null is not allowed as property path.", format = Message.Format.NO_FORMAT)
	String propertyPathCannotBeNull();

	@Message(value = "The property name must not be empty.", format = Message.Format.NO_FORMAT)
	String propertyNameMustNotBeEmpty();

	@Message(value = "null passed as group name.", format = Message.Format.NO_FORMAT)
	String groupMustNotBeNull();

	@Message(value = "The bean type must not be null when creating a constraint mapping.",
			format = Message.Format.NO_FORMAT)
	String beanTypeMustNotBeNull();

	@Message(value = "The method name must not be null.", format = Message.Format.NO_FORMAT)
	String methodNameMustNotBeNull();

	@Message(value = "The object to be validated must not be null.", format = Message.Format.NO_FORMAT)
	String validatedObjectMustNotBeNull();

	@Message(value = "The method to be validated must not be null.", format = Message.Format.NO_FORMAT)
	String validatedMethodMustNotBeNull();

	@Message(value = "The class cannot be null.", format = Message.Format.NO_FORMAT)
	String classCannotBeNull();

	@Message(value = "Class is null.", format = Message.Format.NO_FORMAT)
	String classIsNull();

	@Message(value = "No JSR 223 script engine found for language \"%s\".")
	String unableToFindScriptEngine(String languageName);

	@Message(value = "The constructor to be validated must not be null.", format = Message.Format.NO_FORMAT)
	String validatedConstructorMustNotBeNull();

	@Message(value = "The method parameter array cannot not be null.", format = Message.Format.NO_FORMAT)
	String validatedParameterArrayMustNotBeNull();

	@Message(value = "The created instance must not be null.", format = Message.Format.NO_FORMAT)
	String validatedConstructorCreatedInstanceMustNotBeNull();

	@Message(value = "The input stream for #addMapping() cannot be null.")
	String inputStreamCannotBeNull();

	@Message(value = "Constraints on the parameters of constructors of non-static inner classes " +
			"are not supported if those parameters have a generic type due to JDK bug JDK-5087240.")
	String constraintOnConstructorOfNonStaticInnerClass();

	@Message(value = "Custom parameterized types with more than one type argument are not supported and " +
			"will not be checked for type use constraints.")
	String parameterizedTypesWithMoreThanOneTypeArgument();

	@Message(value = "Hibernate Validator cannot instantiate AggregateResourceBundle.CONTROL. " +
			"This can happen most notably in a Google App Engine environment. " +
			"A PlatformResourceBundleLocator without bundle aggregation was created. " +
			"This only effects you in case you are using multiple ConstraintDefinitionContributor jars. " +
			"ConstraintDefinitionContributors are a Hibernate Validator specific feature. All Bean Validation " +
			"features work as expected. See also https://hibernate.atlassian.net/browse/HV-1023.")
	String unableToUseResourceBundleAggregation();
}


