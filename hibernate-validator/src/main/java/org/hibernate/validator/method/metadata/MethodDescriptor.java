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
package org.hibernate.validator.method.metadata;

import java.util.List;
import javax.validation.Valid;
import javax.validation.metadata.ElementDescriptor;

/**
 * Describes a method and the constraints associated with it.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public interface MethodDescriptor extends ElementDescriptor {

	/**
	 * Returns the name of the method represented by this descriptor.
	 *
	 * @return The name of the method represented by this descriptor.
	 */
	String getMethodName();

	/**
	 * <p>
	 * Returns a list with descriptors for this method's parameters.
	 * </p>
	 * <p>
	 * The size of this list corresponds with the number of this method's
	 * parameters.
	 * </p>
	 *
	 * @return A list with descriptors for this method's parameters. An empty
	 *         list will be returned if this method has no parameters.
	 */
	List<ParameterDescriptor> getParameterDescriptors();

	/**
	 * Whether a cascaded validation for this method's return value shall be
	 * performed or not. This is the case if this method is marked for a
	 * cascaded validation (e.g. by annotating it with the {@link Valid}
	 * annotation) either locally or in the inheritance hierarchy.
	 *
	 * @return <code>True</code>, if this method's return value shall be
	 *         validated recursively, <code>false</code> otherwise.
	 */
	boolean isCascaded();
}
