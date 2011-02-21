/*
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

import java.lang.reflect.Method;

/**
 * A {@link javax.validation.Path.Node} implementation representing a single method parameter.
 *
 * @author Gunnar Morling
 */
public class MethodParameterNodeImpl extends NodeImpl {

	private static final long serialVersionUID = -1964614171714243780L;

	private final static String NAME_TEMPLATE = "%s#%s(%s)";

	/**
	 * Creates a new {@link MethodParameterNodeImpl}.
	 *
	 * @param method The method hosting the parameter to represent.
	 * @param parameterName The name of the parameter to represent.
	 * @param parent The parent node, representing the bean hosting the given
	 * method.
	 */
	MethodParameterNodeImpl(Method method, String parameterName, NodeImpl parent) {
		super(
				String.format(
						NAME_TEMPLATE, method.getDeclaringClass().getSimpleName(), method.getName(), parameterName
				),
				parent, false, null, null
		);
	}
}
