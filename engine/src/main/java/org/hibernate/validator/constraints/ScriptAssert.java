/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 * A class-level constraint, that evaluates a script expression against the
 * annotated element. This constraint can be used to implement validation
 * routines, that depend on multiple attributes of the annotated element.
 * </p>
 * <p>
 * Script expressions can be written in any scripting or expression language,
 * for which a <a href="http://jcp.org/en/jsr/detail?id=223">JSR 223</a>
 * ("Scripting for the Java<sup>TM</sup> Platform") compatible engine can be
 * found on the classpath. The following listing shows an example using the
 * JavaScript engine, which comes with the JDK:
 * </p>
 * <pre>
 * {@code @ScriptAssert(lang = "javascript", script = "_this.startDate.before(_this.endDate)")
 * public class CalendarEvent {
 *
 * 	private Date startDate;
 *
 * 	private Date endDate;
 *
 * 	//...
 *
 * }
 * }
 * </pre>
 * <p>
 * Using a real expression language in conjunction with a shorter object alias
 * allows for very compact expressions:
 * </p>
 *
 * <pre>
 * {@code @ScriptAssert(lang = "jexl", script = "_.startDate > _.endDate", alias = "_")
 * public class CalendarEvent {
 *
 * 	private Date startDate;
 *
 * 	private Date endDate;
 *
 * 	//...
 *
 * }
 * }
 * </pre>
 * <p>
 * Accepts any type.
 * </p>
 *
 * @author Gunnar Morling
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { })
@Documented
public @interface ScriptAssert {

	String message() default "{org.hibernate.validator.constraints.ScriptAssert.message}";

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
	 *         <code>Boolean.TRUE</code>, if the annotated element could
	 *         successfully be validated, otherwise <code>Boolean.FALSE</code>.
	 *         Returning null or any type other than Boolean will cause a
	 *         {@link javax.validation.ConstraintDeclarationException} upon validation. Any
	 *         exception occurring during script evaluation will be wrapped into
	 *         a ConstraintDeclarationException, too. Within the script, the
	 *         validated object can be accessed from the {@link javax.script.ScriptContext
	 *         script context} using the name specified in the
	 *         <code>alias</code> attribute.
	 */
	String script();

	/**
	 * @return The name, under which the annotated element shall be registered
	 *         within the script context. Defaults to "_this".
	 */
	String alias() default "_this";

	/**
	 * Defines several {@code @ScriptAssert} annotations on the same element.
	 */
	@Target({ TYPE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		ScriptAssert[] value();
	}
}
