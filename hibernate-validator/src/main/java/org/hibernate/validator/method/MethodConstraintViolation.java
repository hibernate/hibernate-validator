/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.method;

import java.lang.reflect.Method;
import javax.validation.ConstraintViolation;


/**
 * Describes the violation of a method-level constraint by providing access
 * to the method/parameter hosting the violated constraint etc.
 *
 * @author Gunnar Morling
 * @see MethodValidator
 */
public interface MethodConstraintViolation<T> extends ConstraintViolation<T> {

	/**
	 * The kind of a {@link MethodConstraintViolation}.
	 *
	 * @author Gunnar Morling
	 */
	public static enum Kind {

		/**
		 * Identifies constraint violations occurred during the validation of a
		 * method's parameter.
		 */
		PARAMETER,

		/**
		 * Identifies constraint violations occurred during the validation of a
		 * method's return value.
		 */
		RETURN_VALUE
	}

	/**
	 * Returns the method during which's validation this constraint violation
	 * occurred.
	 *
	 * @return The method during which's validation this constraint violation
	 *         occurred.
	 */
	Method getMethod();

	/**
	 * Returns the index of the parameter holding the constraint which caused
	 * this constraint violation.
	 *
	 * @return The index of the parameter holding the constraint which caused
	 *         this constraint violation or null if this constraint violation is
	 *         not of {@link Kind#PARAMETER}.
	 */
	Integer getParameterIndex();

	/**
	 * <p>
	 * Returns the name of the parameter holding the constraint which caused
	 * this constraint violation.
	 * </p>
	 * <p>
	 * Currently a synthetic name following the form <code>"arg" + index</code>
	 * will be returned, e.g. <code>"arg0"</code>. In future versions it might
	 * be possible to specify real parameter names, e.g. using an
	 * annotation-based approach around <code>javax.inject.Named</code>.
	 * </p>
	 *
	 * @return The name of the parameter holding the constraint which caused
	 *         this constraint violation or null if this constraint violation is
	 *         not of {@link Kind#PARAMETER}.
	 */
	String getParameterName();

	/**
	 * Returns the kind of this method constraint violation.
	 *
	 * @return The kind of this method constraint violation.
	 */
	Kind getKind();
}
