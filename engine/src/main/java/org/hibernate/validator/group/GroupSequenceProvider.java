/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.group;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

/**
 * The {@code GroupSequenceProvider} annotation defines the {@code DefaultGroupSequenceProvider}
 * class responsible to return the list of classes defining the default group sequence for the annotated type.
 * <p>
 * Note:
 * <ul>
 * <li>It is not allowed to use {@code GroupSequenceProvider} and {@link jakarta.validation.GroupSequence} together on
 * the same type.</li>
 * <li>{@code GroupSequenceProvider} is a Hibernate Validator specific annotation and not portable.</li>
 * </ul>
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Hardy Ferentschik
 * @see jakarta.validation.GroupSequence
 */
@Retention(RUNTIME)
@Target({ TYPE })
public @interface GroupSequenceProvider {

	/**
	 * @return The default group sequence provider implementation.
	 */
	Class<? extends DefaultGroupSequenceProvider<?>> value();
}
