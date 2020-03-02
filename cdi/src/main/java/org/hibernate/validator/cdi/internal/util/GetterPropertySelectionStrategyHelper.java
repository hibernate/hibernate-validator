/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cdi.internal.util;

import java.lang.reflect.Method;
import java.util.Optional;

import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.internal.properties.DefaultGetterPropertySelectionStrategy;
import org.hibernate.validator.spi.properties.ConstrainableExecutable;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;

/**
 * A wrapper around {@link GetterPropertySelectionStrategy}.
 *
 * @author Marko Bekhta
 */
public class GetterPropertySelectionStrategyHelper {

	private final GetterPropertySelectionStrategy getterPropertySelectionStrategy;

	private GetterPropertySelectionStrategyHelper(GetterPropertySelectionStrategy getterPropertySelectionStrategy) {
		this.getterPropertySelectionStrategy = getterPropertySelectionStrategy;
	}

	public Optional<String> getProperty(Method method) {
		return getterPropertySelectionStrategy.getProperty( new ConstrainableMethod( method ) );
	}

	public static GetterPropertySelectionStrategyHelper forValidationFactory(ValidatorFactory factory) {
		GetterPropertySelectionStrategy getterPropertySelectionStrategy;
		if ( factory instanceof HibernateValidatorFactory ) {
			getterPropertySelectionStrategy = factory.unwrap( HibernateValidatorFactory.class ).getGetterPropertySelectionStrategy();
		}
		else {
			getterPropertySelectionStrategy = new DefaultGetterPropertySelectionStrategy();
		}
		return new GetterPropertySelectionStrategyHelper( getterPropertySelectionStrategy );
	}

	private static class ConstrainableMethod implements ConstrainableExecutable {

		private final Method method;

		private ConstrainableMethod(Method method) {
			this.method = method;
		}

		@Override
		public Class<?> getReturnType() {
			return method.getReturnType();
		}

		@Override
		public String getName() {
			return method.getName();
		}

		@Override
		public Class<?>[] getParameterTypes() {
			return method.getParameterTypes();
		}
	}
}
