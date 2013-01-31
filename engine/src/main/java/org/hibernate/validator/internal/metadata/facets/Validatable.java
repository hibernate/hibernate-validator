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
package org.hibernate.validator.internal.metadata.facets;

/**
 * Provides a unified view on validatable elements of all kinds, be it Java
 * beans, the arguments passed to a method or the value returned from a method.
 * Allows a unified handling of these elements in the validation routine.
 *
 * @author Gunnar Morling
 */
public interface Validatable {

	/**
	 * Returns the cascaded elements of this validatable, e.g. the properties of
	 * a bean or the parameters of a method annotated with {@code @Valid}.
	 *
	 * @return The cascaded elements of this validatable.
	 */
	Iterable<Cascadable> getCascadables();
}
