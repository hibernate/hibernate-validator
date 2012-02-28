/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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

// $Id:$
package org.hibernate.validator.constraints;

/**
 * The Enum {@code CompositionType} which is used as argument to the annotation {@code ConstraintComposition}.
 */
public enum CompositionType {
	/**
	 * Used to indicate the disjunction of all constraints it is applied to.
	 */
	OR,

	/**
	 * Used to indicate the conjunction of all the constraints it is applied to.
	 */
	AND,

	/**
	 * ALL_FALSE is a generalisation of the usual NOT operator, which is applied to
	 * a list of conditions rather than just one element.
	 * When the annotation it is used on is composed of a single constraint annotation, then it is equivalent to NOT.
	 */
	ALL_FALSE
}


