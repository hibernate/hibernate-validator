package org.hibernate.validator.test.internal.engine.methodlevel.generic;

import jakarta.validation.constraints.NotNull;

/**
 * @author Hardy Ferentschik
 */
public interface SimpleService<C> {
	void configure(@NotNull C config);

	void doIt(@NotNull C config);
}
