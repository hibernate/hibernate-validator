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
package org.hibernate.validation.constraints.composition;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.OverridesAttribute;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;


/**
 * @author Hardy Ferentschik
 */
@NotNull
@Size
// first pattern just duplicates the length of 5 characters, the second pattern is just to proof that parameters can be overridden.
@Pattern.List({ @Pattern(regexp = "....."), @Pattern(regexp = "bar") })
@Constraint(validatedBy = FrenchZipcodeConstraintValidator.class)
@Documented
@Target({ METHOD, FIELD, TYPE })
@Retention(RUNTIME)
public @interface FrenchZipcode {
	String message() default "Wrong zipcode";

	Class<?>[] groups() default { };

	@OverridesAttribute.List({
			@OverridesAttribute(constraint = Size.class, name = "min"),
			@OverridesAttribute(constraint = Size.class, name = "max")
	}) int size() default 5;

	@OverridesAttribute(constraint = Size.class,
			name = "message") String sizeMessage() default "A french zip code has a length of 5";


	@OverridesAttribute(constraint = Pattern.class, name = "regexp", constraintIndex = 2) String regex() default "\\d*";
}
