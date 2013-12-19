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
package org.hibernate.validator.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 * A method-level constraint, that evaluates a script expression against the
 * annotated method or constructor. This constraint can be used to implement
 * validation routines that depend on several parameters of the annotated
 * executable.
 * </p>
 * <p>
 * Script expressions can be written in any scripting or expression language,
 * for which a <a href="http://jcp.org/en/jsr/detail?id=223">JSR 223</a>
 * ("Scripting for the Java<sup>TM</sup> Platform") compatible engine can be
 * found on the classpath. To refer to a parameter within the scripting
 * expression, use its name as obtained by the active
 * {@link javax.validation.ParameterNameProvider}. By default, {@code arg0}, {@code arg1} etc.
 * will be used as parameter names.
 * </p>
 * <p>
 * The following listing shows an example using the JavaScript engine which
 * comes with the JDK:
 * </p>
 * <pre>
 * {@code @ParametersScriptAssert(script = "arg0.before(arg1)", lang = "javascript")
 * public void createEvent(Date start, Date end) { ... }
 * }
 * </pre>
 * <p>
 * Can be specified on any method or constructor.
 * </p>
 *
 * @author Gunnar Morling
 */
@Target({ CONSTRUCTOR, METHOD })
@Retention(RUNTIME)
@Constraint(validatedBy = { })
@Documented
public @interface ParameterScriptAssert {

	String message() default "{org.hibernate.validator.constraints.ParametersScriptAssert.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return The name of the script language used by this constraint as
	 *         expected by the JSR 223 {@link javax.script.ScriptEngineManager}. A
	 *         {@link javax.validation.ConstraintDeclarationException} will be thrown upon script
	 *         evaluation, if no engine for the given language could be found.
	 */
	String lang();

	/**
	 * @return The script to be executed. The script must return
	 *         <code>Boolean.TRUE</code>, if the executable parameters could
	 *         successfully be validated, otherwise <code>Boolean.FALSE</code>.
	 *         Returning null or any type other than Boolean will cause a
	 *         {@link javax.validation.ConstraintDeclarationException} upon validation. Any
	 *         exception occurring during script evaluation will be wrapped into
	 *         a ConstraintDeclarationException, too. Within the script, the
	 *         validated parameters can be accessed using their names as retrieved from the
	 *         active {@link javax.validation.ParameterNameProvider}.
	 */
	String script();

	/**
	 * Defines several {@link ParameterScriptAssert} annotations on the same executable.
	 */
	@Target({ CONSTRUCTOR, METHOD })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		ParameterScriptAssert[] value();
	}
}
