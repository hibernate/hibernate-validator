package org.hibernate.validator.test.internal.engine.methodlevel.generic;

/**
 * @author Hardy Ferentschik
 */
public abstract class AbstractSimpleService<C> implements SimpleService<C> {
	@Override
	public void configure(C config) {
		if ( config == null ) {
			throw new IllegalStateException( "Config cannot be null!" );
		}
	}
}
