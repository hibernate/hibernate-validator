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
package org.hibernate.validator.util.privilegedactions;

import java.lang.reflect.Constructor;
import java.security.PrivilegedAction;

/**
 * @author Emmanuel Bernard
 */
public final class GetConstructor<T> implements PrivilegedAction<Constructor<T>> {
	private final Class<T> clazz;
	private final Class<?>[] params;

	public static <T> GetConstructor<T> action(Class<T> clazz, Class<?>... params) {
		return new GetConstructor<T>( clazz, params );
	}

	private GetConstructor(Class<T> clazz, Class<?>... params) {
		this.clazz = clazz;
		this.params = params;
	}

	public Constructor<T> run() {
		try {
			return clazz.getConstructor(params);
		}
		catch ( NoSuchMethodException e ) {
			return null;
		}
	}
}
