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
package org.hibernate.validator.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.OverridesAttribute;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Pattern;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validate that the string is a valid URL.
 *
 * @author Hardy Ferentschik
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@ReportAsSingleViolation
@Pattern(regexp = "")
/**
 * {@code URL} is a field/method level constraint which can be applied on a string to assert that the string
 * represents a valid URL. Per default the constraint verifies that the annotated value conforms to
 * <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC2396</a>. Via the parameters {@code protocol}, {@code host}
 * and {@code port} one can assert the corresponding parts of the parsed URL.
 * <p>
 * Due to the fact that RFC2396 is a very generic specification it often is not restrictive enough for a given usecase.
 * In this case one can also specify the optional {@code regexp} and {@code flags} parameters. This way an additional
 * Java regular expression can be specified which the string (URL) has to match.
 * </p>
 */
public @interface URL {
	String message() default "{org.hibernate.validator.constraints.URL.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return the protocol (scheme) the annotated string must match, eg ftp or http.
	 *         Per default any protocol is allowed
	 */
	String protocol() default "";

	/**
	 * @return the host the annotated string must match, eg localhost. Per default any host is allowed
	 */
	String host() default "";

	/**
	 * @return the port the annotated string must match, eg 80. Per default any port is allowed
	 */
	int port() default -1;

	/**
	 * @return an additional regular expression the annotated URL must match. The default is any string ('.*')
	 */
	@OverridesAttribute(constraint = Pattern.class, name = "regexp") String regexp() default ".*";

	/**
	 * @return used in combination with {@link #regexp()} in order to specify a regular expression option
	 */
	@OverridesAttribute(constraint = Pattern.class, name = "flags") Pattern.Flag[] flags() default { };

	/**
	 * Defines several {@code @URL} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		URL[] value();
	}
}
