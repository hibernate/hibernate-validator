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
package org.hibernate.validator.metadata;

/**
 * Builds {@link ConstraintMetaData} instances for the
 * {@link ConstrainedElement} objects representing one method or property in a
 * type's inheritance hierarchy.
 *
 * @author Gunnar Morling
 */
public abstract class MetaDataBuilder {

	/**
	 * Whether this builder allows to add the given element or not. This is the
	 * case if the specified element relates to the same property or method with
	 * which this builder was instantiated.
	 *
	 * @param constrainedElement The element to check.
	 *
	 * @return <code>true</code> if the given element can be added to this
	 *         builder, <code>false</code> otherwise.
	 */
	public abstract boolean accepts(ConstrainedElement constrainedElement);

	/**
	 * Adds the given element to this builder. It must be checked with
	 * {@link #accepts(ConstrainedElement)} before, whether this is allowed or
	 * not.
	 *
	 * @param constrainedElement The element to add.
	 */
	public abstract void add(ConstrainedElement constrainedElement);

	/**
	 * Creates a new, read-only {@link ConstraintMetaData} object with all
	 * constraint information related to the method or property represented by
	 * this builder.
	 *
	 * @return A {@link ConstraintMetaData} object.
	 */
	public abstract ConstraintMetaData build();

}