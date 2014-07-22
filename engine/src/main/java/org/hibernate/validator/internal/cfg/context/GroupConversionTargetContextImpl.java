/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.cfg.context;

import org.hibernate.validator.cfg.context.GroupConversionTargetContext;

/**
 * Context allowing to set the target of a group conversion.
 *
 * @author Gunnar Morling
 */
class GroupConversionTargetContextImpl<C> implements GroupConversionTargetContext<C> {

	private final C cascadableContext;
	private final Class<?> from;
	private final CascadableConstraintMappingContextImplBase<?> target;

	GroupConversionTargetContextImpl(Class<?> from, C cascadableContext, CascadableConstraintMappingContextImplBase<?> target) {
		this.from = from;
		this.cascadableContext = cascadableContext;
		this.target = target;
	}

	@Override
	public C to(Class<?> to) {
		target.addGroupConversion( from, to );
		return cascadableContext;
	}
}
