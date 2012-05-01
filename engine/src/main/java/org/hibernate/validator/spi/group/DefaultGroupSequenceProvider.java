/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.spi.group;

import java.util.List;

/**
 * This class defines the dynamic group sequence provider contract.
 * <p>
 * In order to redefine dynamically the default group sequence for a type T, the {@link org.hibernate.validator.group.GroupSequenceProvider} annotation
 * must be placed on T, specifying as its value a concrete implementation of {@code DefaultGroupSequenceProvider}, which
 * must be parametrized with the type T.
 * </p>
 * <p>
 * If during the validation process the {@code Default} group is validated for T, the actual validated instance
 * is passed to the {@code DefaultGroupSequenceProvider} to determine the default group sequence.
 * </p>
 * <p>
 * Note:
 * <ul>
 * <li>Implementations must provide a public default constructor.</li>
 * <li>Implementations must be thread-safe.</li>
 * </ul>
 *
 * @param <T> The type for which an implementation is defined.
 *
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 * @author Hardy Ferentschik
 */
public interface DefaultGroupSequenceProvider<T> {

	/**
	 * This method returns the default group sequence for the given instance.
	 * <p>
	 * The object parameter allows to dynamically compose the default group sequence in function of the validated value state.
	 * </p>
	 *
	 * @param object the instance being validated. This value can be {@code null} in case this method was called as part of
	 * {@linkplain javax.validation.Validator#validateValue(Class, String, Object, Class[]) Validator#validateValue}.
	 *
	 * @return a list of classes specifying the default group sequence. The same constraints to the redefined group list
	 *         apply as for lists defined via {@code GroupSequence}. In particular the list has to contain the type T.
	 */
	List<Class<?>> getValidationGroups(T object);
}
