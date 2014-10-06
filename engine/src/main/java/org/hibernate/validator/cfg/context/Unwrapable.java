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
package org.hibernate.validator.cfg.context;

/**
 * Facet of a constraint mapping creational context which allows to mark the underlying element as to be unwrapped prior
 * to validation.
 *
 * @author Gunnar Morling
 * @author Hardy Feretnschik
 *
 * @hv.experimental This API is considered experimental and may change in future revisions
 */
public interface Unwrapable<U extends Unwrapable<U>> {

	/**
	 * Configures explicitly the unwrapping mode of the current element (property, parameter etc.).
	 *
	 * @param unwrap Explicitly set whether to unwrap or not.
	 * @return The current creational context following the method chaining pattern.
	 * @see org.hibernate.validator.valuehandling.UnwrapValidatedValue
	 */
	U unwrapValidatedValue(boolean unwrap);
}
