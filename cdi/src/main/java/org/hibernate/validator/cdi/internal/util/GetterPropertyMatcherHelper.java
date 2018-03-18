/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cdi.internal.util;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.internal.properties.DefaultGetterPropertyMatcher;
import org.hibernate.validator.spi.properties.ConstrainableExecutable;
import org.hibernate.validator.spi.properties.GetterPropertyMatcher;

/**
 * A wrapper around {@link GetterPropertyMatcher}.
 *
 * @author Marko Bekhta
 */
public class GetterPropertyMatcherHelper {

	private final GetterPropertyMatcher getterPropertyMatcher;

	private GetterPropertyMatcherHelper(GetterPropertyMatcher getterPropertyMatcher) {
		this.getterPropertyMatcher = getterPropertyMatcher;
	}

	public boolean isProperty(Method method) {
		return getterPropertyMatcher.isProperty( new ConstrainableMethod( method ) );
	}

	public String getPropertyName(Method method) {
		return getterPropertyMatcher.getPropertyName( new ConstrainableMethod( method ) );
	}

	public static GetterPropertyMatcherHelper forValidationFactory(ValidatorFactory factory) {
		GetterPropertyMatcher getterPropertyMatcher;
		if ( factory instanceof HibernateValidatorFactory ) {
			getterPropertyMatcher = factory.unwrap( HibernateValidatorFactory.class ).getGetterPropertyMatcher();
		}
		else {
			getterPropertyMatcher = new DefaultGetterPropertyMatcher();
		}
		return new GetterPropertyMatcherHelper( getterPropertyMatcher );
	}

	private static class ConstrainableMethod implements ConstrainableExecutable {

		private final Method method;

		private ConstrainableMethod(Method method) {
			this.method = method;
		}

		@Override public Class<?> getReturnType() {
			return method.getReturnType();
		}

		@Override public String getName() {
			return method.getName();
		}

		@Override public Type[] getParameterTypes() {
			return method.getParameterTypes();
		}

		public Method getMethod() {
			return method;
		}
	}
}
