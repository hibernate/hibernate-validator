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
package org.hibernate.validator.internal.engine.valuehandling;

import java.lang.reflect.Type;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;

import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

/**
 * Base class for {@link ValidatedValueUnwrapper}s based on ClassMate's type resolver.
 *
 * @author Gunnar Morling
 */
public abstract class TypeResolverBasedValueUnwrapper<T> extends ValidatedValueUnwrapper<T> {

	private final Class<?> clazz;
	private final TypeResolver typeResolver;

	TypeResolverBasedValueUnwrapper(TypeResolutionHelper typeResolutionHelper) {
		this.typeResolver = typeResolutionHelper.getTypeResolver();
		clazz = resolveSingleTypeParameter( typeResolver, this.getClass(), ValidatedValueUnwrapper.class );
	}

	@Override
	public Type getValidatedValueType(Type valueType) {
		return resolveSingleTypeParameter( typeResolver, valueType, clazz );
	}

	/**
	 * Resolves the single type parameter of the given target class, using the given sub-type.
	 */
	private static Class<?> resolveSingleTypeParameter(TypeResolver typeResolver, Type subType, Class<?> target) {
		ResolvedType resolvedType = typeResolver.resolve( subType );
		return resolvedType.typeParametersFor( target ).get( 0 ).getErasedType();
	}
}
