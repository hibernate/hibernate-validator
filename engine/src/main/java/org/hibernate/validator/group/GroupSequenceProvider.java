/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.group;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The {@code GroupSequenceProvider} annotation defines the {@code DefaultGroupSequenceProvider}
 * class responsible to return the list of classes defining the default group sequence for the annotated type.
 * <p>
 * Note:
 * <ul>
 * <li>It is not allowed to use {@code GroupSequenceProvider} and {@link javax.validation.GroupSequence} together on
 * the same type.</li>
 * <li>{@code GroupSequenceProvider} is a Hibernate Validator specific annotation and not portable.</li>
 * </ul>
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Hardy Ferentschik
 * @see javax.validation.GroupSequence
 */
@Retention(RUNTIME)
@Target({ TYPE })
public @interface GroupSequenceProvider {

	/**
	 * @return The default group sequence provider implementation.
	 */
	Class<? extends DefaultGroupSequenceProvider<?>> value();
}
