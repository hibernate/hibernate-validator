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
package org.hibernate.validator.cfg;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.context.TypeConstraintMappingContext;
import org.hibernate.validator.internal.cfg.context.ConstraintMappingContext;
import org.hibernate.validator.internal.cfg.context.TypeConstraintMappingContextImpl;
import org.hibernate.validator.internal.util.Contracts;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * Top level class for constraints configured via the programmatic API. This
 * class is not intended to be inherited by clients. It will be converted into
 * an interface in a future release.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public class ConstraintMapping {

	/**
	 * Not intended for public use.
	 *
	 * @deprecated Will be removed in a future release.
	 */
	@Deprecated
	protected ConstraintMappingContext context;

	/**
	 * @deprecated This class will be converted into an interface in a future
	 *             release. Use
	 *             {@link HibernateValidatorConfiguration#createConstraintMapping()}
	 *             instead to create new constraint mappings.
	 */
	@Deprecated
	public ConstraintMapping() {
		context = new ConstraintMappingContext();
	}

	/**
	 * @deprecated This class will be converted into an interface in a future
	 *             release. Use
	 *             {@link HibernateValidatorConfiguration#createConstraintMapping()}
	 *             instead to create new constraint mappings.
	 */
	@Deprecated
	protected ConstraintMapping(ConstraintMapping original) {
		this.context = original.context;
	}

	/**
	 * Starts defining constraints on the specified bean class.
	 *
	 * @param <C> The type to be configured.
	 * @param beanClass The bean class on which to define constraints. All constraints defined after calling this method
	 * are added to the bean of the type {@code beanClass} until the next call of {@code type}.
	 *
	 * @return Instance allowing for defining constraints on the specified class.
	 */
	public final <C> TypeConstraintMappingContext<C> type(Class<C> beanClass) {
		Contracts.assertNotNull( beanClass, MESSAGES.beanTypeMustNotBeNull() );
		return new TypeConstraintMappingContextImpl<C>( beanClass, context );
	}
}
