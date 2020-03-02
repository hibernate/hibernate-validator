/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraints;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import org.hibernate.validator.constraints.ParameterScriptAssert.List;

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
 * {@link jakarta.validation.ParameterNameProvider}. The default provider will
 * return the actual parameter names, if the -parameters compiler option
 * has been enabled, and {@code arg0}, {@code arg1} etc. otherwise.
 * </p>
 * <p>
 * The following listing shows an example using the JavaScript engine which
 * comes with the JDK:
 * </p>
 * <pre>
 * {@code @ParameterScriptAssert(script = "start.before(end)", lang = "javascript")
 * public void createEvent(Date start, Date end) { ... }
 * }
 * </pre>
 * <p>
 * Can be specified on any method or constructor.
 * </p>
 *
 * @author Gunnar Morling
 */
@Documented
@Constraint(validatedBy = { })
@Target({ CONSTRUCTOR, METHOD })
@Retention(RUNTIME)
@Repeatable(List.class)
public @interface ParameterScriptAssert {

	String message() default "{org.hibernate.validator.constraints.ParametersScriptAssert.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return The name of the script language used by this constraint as
	 *         expected by the JSR 223 {@link javax.script.ScriptEngineManager}. A
	 *         {@link jakarta.validation.ConstraintDeclarationException} will be thrown upon script
	 *         evaluation, if no engine for the given language could be found.
	 */
	String lang();

	/**
	 * @return The script to be executed. The script must return
	 *         {@code Boolean.TRUE}, if the executable parameters could
	 *         successfully be validated, otherwise {@code Boolean.FALSE}.
	 *         Returning null or any type other than Boolean will cause a
	 *         {@link jakarta.validation.ConstraintDeclarationException} upon validation. Any
	 *         exception occurring during script evaluation will be wrapped into
	 *         a ConstraintDeclarationException, too. Within the script, the
	 *         validated parameters can be accessed using their names as retrieved from the
	 *         active {@link jakarta.validation.ParameterNameProvider}.
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
