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
}


