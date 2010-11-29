/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import javax.validation.Path;

/**
 * Container class for a validation error message and its corresponding path.
 *
 * @author Hardy Ferentschik
 */
public class MessageAndPath {
	private final String message;
	private final Path propertyPath;

	public MessageAndPath(String message, Path property) {
		this.message = message;
		this.propertyPath = property;
	}

	public final String getMessage() {
		return message;
	}

	public final Path getPath() {
		return propertyPath;
	}
}
