package org.hibernate.validator.test;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

import org.hibernate.validator.ValidatorClass;

/**
 * Dummy sample of a bean-level validation annotation
 *
 * @author Emmanuel Bernard
 */
@ValidatorClass(SerializabilityValidator.class)
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface Serializability {
	String message() default "Bean not Serializable";
}
