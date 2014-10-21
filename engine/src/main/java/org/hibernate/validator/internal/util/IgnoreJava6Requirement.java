/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Hint to AnimalSniffer to ignore violations.
 * <p>
 * Should be applied with great care in general, i.e. you need to make sure that the violating element is only meant to
 * be run on the Java version it is based on.
 *
 * @author Gunnar Morling
 *
 */
@Retention(value=RetentionPolicy.CLASS)
@Target(value={ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE})
public @interface IgnoreJava6Requirement {}
