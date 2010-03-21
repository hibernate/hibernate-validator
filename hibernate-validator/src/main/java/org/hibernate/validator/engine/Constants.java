/*
 * $Id:$
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.engine;

/**
 * Constants used throughout the Hibernate Validator project.
 *
 * @author Gunnar Morling
 */
public final class Constants {

	private Constants() {
	}

	/**
	 * The name of the default message bundle.
	 */
	public static final String DEFAULT_VALIDATION_MESSAGES = "org.hibernate.validator.ValidationMessages";

	/**
	 * The name of the user-provided message bundle as defined in the specification.
	 */
	public static final String USER_VALIDATION_MESSAGES = "ValidationMessages";
}
