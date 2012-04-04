/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
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
