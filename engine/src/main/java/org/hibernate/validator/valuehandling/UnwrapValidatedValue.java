/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.valuehandling;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * When specified on properties (as represented by fields or property getters), parameters or executables, the value of
 * the annotated element will be unwrapped from a container type prior to validation. This is useful when working with
 * types such as {@code JAXBElement} or {@code Optional} where constraints should not apply to the
 * container but to the wrapped element:
 *
 * <pre>
 * &#064;Size(max = 10)
 * &#064;UnwrapValidatedValue
 * private JAXBElement&lt;String&gt; name;
 * </pre>
 *
 * For each type to be unwrapped, a corresponding
 * {@link org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper} implementation must be registered.
 *
 * @author Gunnar Morling
 * @hv.experimental This API is considered experimental and may change in future revisions
 */
@Documented
@Target({ METHOD, FIELD, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
public @interface UnwrapValidatedValue {
	/**
	 * @return Returns {@code true} or {@code false} depending on whether unwrapping should occur. This flag can be used
	 * to explicitly configure unwrapping in the case where there are constraint validator instances which can validate
	 * the wrapper as well as the wrapped value.
	 */
	boolean value() default true;
}
