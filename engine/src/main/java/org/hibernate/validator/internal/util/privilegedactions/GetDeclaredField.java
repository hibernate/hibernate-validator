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
package org.hibernate.validator.internal.util.privilegedactions;

import java.lang.reflect.Field;
import java.security.PrivilegedAction;

/**
 * Returns the declared field with the specified name or {@code null} if it does not exist.
 *
 * @author Emmanuel Bernard
 */
public final class GetDeclaredField implements PrivilegedAction<Field> {
	private final Class<?> clazz;
	private final String fieldName;

	public static GetDeclaredField action(Class<?> clazz, String fieldName) {
		return new GetDeclaredField( clazz, fieldName );
	}

	private GetDeclaredField(Class<?> clazz, String fieldName) {
		this.clazz = clazz;
		this.fieldName = fieldName;
	}

	@Override
	public Field run() {
		try {
			final Field field = clazz.getDeclaredField( fieldName );
			field.setAccessible( true );
			return field;
		}
		catch ( NoSuchFieldException e ) {
			return null;
		}
	}
}
