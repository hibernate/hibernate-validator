/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.messageinterpolation.el;

import jakarta.el.ArrayELResolver;
import jakarta.el.CompositeELResolver;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.ListELResolver;
import jakarta.el.MapELResolver;
import jakarta.el.ResourceBundleELResolver;
import jakarta.el.StandardELContext;

/**
 * @author Guillaume Smet
 */
public class VariablesELContext extends StandardELContext {

	private static final ELResolver DEFAULT_RESOLVER;

	static {
		CompositeELResolver resolver = new CompositeELResolver();
		resolver.add( new RootResolver() );
		resolver.add( new ArrayELResolver( true ) );
		resolver.add( new ListELResolver( true ) );
		resolver.add( new MapELResolver( true ) );
		resolver.add( new ResourceBundleELResolver() );
		// this one is required so that expressions containing method calls are returned as is
		// if not there, the expression is replaced by an empty string
		resolver.add( new NoOpElResolver() );
		DEFAULT_RESOLVER = resolver;
	}

	public VariablesELContext(ExpressionFactory expressionFactory) {
		super( expressionFactory );

		// In jakarta.el.ELContext, the ExpressionFactory is extracted from the context map. If it is not found, it
		// defaults to ELUtil.getExpressionFactory() which, if we provided the ExpressionFactory to the
		// ResourceBundleMessageInterpolator, might not be the same. Thus, we inject the ExpressionFactory in the
		// context.
		putContext( ExpressionFactory.class, expressionFactory );
	}

	@Override
	public void addELResolver(ELResolver cELResolver) {
		throw new UnsupportedOperationException( getClass().getSimpleName() + " does not support addELResolver." );
	}

	@Override
	public ELResolver getELResolver() {
		return DEFAULT_RESOLVER;
	}

}
