// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package javax.validation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;

/**
 * Mark a parameter as overriding the parameter of a composing constraint.
 * Both parameter must share the same type.
 *
 * @author Emmanuel Bernard
 */
@Retention(RUNTIME)
public @interface OverridesParameter {
	/**
	 * @return Constraint type the parameter is overriding
	 */
	Class<? extends Annotation> constraint();

	/**
	 * @return name of constraint parameter overridden.
	 * Defaults to the name of the parameter hosting the annotation.
	 */
	String parameter();

	/**
	 * @return The index of the targeted constraint declaration when using
	 * multiple constraints of the same type.
	 * The index represents the index of the constraint in the value() array.
	 *
	 * By default, no index is defined and the single constraint declaration
	 * is targeted
	 */
	int index() default -1;
}
