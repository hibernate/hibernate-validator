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

import java.util.List;

/**
 * The default dynamic group sequence provider contract. A provider must implement
 * this contract and provide a public default constructor.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public interface DefaultGroupSequenceProvider<T> {
	/**
	 * This method is responsible to provide the composing classes of the default group sequence.
	 * <p>
	 * The object parameter allow the provider implementation to dynamically compose the default
	 * group sequence in function of the validated value state.
	 * </p>
	 *
	 * @param object the value being validated or {@code null} if this provider is used within the
	 * {@linkplain javax.validation.Validator#validateValue(Class, String, Object, Class[]) Validator#validateValue} method
	 *
	 * @return the class list specifying the default group sequence, if {@code null} is returned
	 * it's assumed to be the Default group.
	 */
	List<Class<?>> getValidationGroups(T object);
}
