// $Id: ElementDescriptorImpl.java 105 2008-09-29 12:37:32Z hardy.ferentschik $
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.impl;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.ConstraintDescriptor;
import javax.validation.ElementDescriptor;

/**
 * Describe a validated element (class, field or property).
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @todo Should returnType be renamed to type?
 * @todo Handle problem in descirbing cyclic dependecies for propertyPath
 */
public class ElementDescriptorImpl implements ElementDescriptor {
	private final Class returnType;
	private final boolean cascaded;
	private final List<ConstraintDescriptor> constraintDescriptors = new ArrayList<ConstraintDescriptor>();
	private final String propertyPath;


	public ElementDescriptorImpl(Class returnType, boolean cascaded, String propertyPath) {
		this.returnType = returnType;
		this.cascaded = cascaded;
		this.propertyPath = propertyPath;
	}

	public void addConstraintDescriptor(ConstraintDescriptorImpl constraintDescriptor) {
		constraintDescriptors.add( constraintDescriptor );
	}

	/**
	 * {@inheritDoc}
	 *
	 * @todo Generic type or regular type?
	 */
	public Class getType() {
		return returnType;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCascaded() {
		return cascaded;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ConstraintDescriptor> getConstraintDescriptors() {
		return Collections.unmodifiableList( constraintDescriptors );
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPropertyPath() {
		return propertyPath;
	}
}
