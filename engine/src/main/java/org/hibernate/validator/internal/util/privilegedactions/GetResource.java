/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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


package org.hibernate.validator.internal.util.privilegedactions;

import java.net.URL;
import java.security.PrivilegedAction;

/**
 * Loads the given resource.
 *
 * @author Gunnar Morling
 */
public final class GetResource implements PrivilegedAction<URL> {

	private final String resourceName;
	private final ClassLoader classLoader;

	public static GetResource action(ClassLoader classLoader, String resourceName) {
		return new GetResource( classLoader, resourceName );
	}

	private GetResource(ClassLoader classLoader, String resourceName) {
		this.classLoader = classLoader;
		this.resourceName = resourceName;
	}

	@Override
	public URL run() {
		return classLoader.getResource( resourceName );
	}
}
