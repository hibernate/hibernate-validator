/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.bean;

final class CastingBeanReference<T> implements BeanReference<T> {

	private final BeanReference<?> casted;
	private final Class<T> expectedType;

	CastingBeanReference(BeanReference<?> casted, Class<T> expectedType) {
		this.casted = casted;
		this.expectedType = expectedType;
	}

	@Override
	@SuppressWarnings("unchecked") // Checked using reflection
	public BeanHolder<T> resolve(BeanResolver beanResolver) {
		BeanHolder<?> beanHolder = casted.resolve( beanResolver );
		try {
			// Just let the type throw an exception if something is wrong
			expectedType.cast( beanHolder.get() );
			// The instance can safely be cast to the expected type, so we can safely do this
			return (BeanHolder<T>) beanHolder;
		}
		catch (Exception e) {
			try {
				beanHolder.close();
			}
			catch (RuntimeException closeEx) {
				e.addSuppressed( closeEx );
			}
			throw e;
		}
	}

	@Override
	@SuppressWarnings("unchecked") // Checked using reflection
	public <U> BeanReference<? extends U> asSubTypeOf(Class<U> expectedType2) {
		if ( expectedType2.isAssignableFrom( expectedType ) ) {
			return (BeanReference<? extends U>) this;
		}
		else {
			return casted.asSubTypeOf( expectedType2 );
		}
	}
}
