// $Id: ConstraintDescriptor.java 114 2008-10-01 13:44:26Z hardy.ferentschik $
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
package javax.validation;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

/**
 * Describes a single constraint.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public interface ConstraintDescriptor {
	/**
	 * Returns the annotation describing the constraint declaration.
	 * If a composing constraint, parameter values are reflecting
	 * the overridden parameters form the main constraint
	 *
	 * @return The annotation for this constraint.
	 */
	Annotation getAnnotation();

	/**
	 * @return The groups the constraint is applied on.
	 */
	Set<String> getGroups();

	/**
	 * @return The actual constraint implementation.
	 */
	Constraint getConstraintImplementation();

	/**
	 * Returns a map containing the annotation paramter names as keys and the annotation parameter values
	 * as value.
	 * If a composing constraint, parameter values are reflecting
	 * the overridden parameters form the main constraint
	 *
	 * @return Returns a map containing the annotation paramter names as keys and the annotation parameter values
	 *         as value.
	 */
	Map<String, Object> getParameters();

	/**
	 * Return a set of ConstraintDescriptors. Each ConstraintDescriptor describes a composing
	 * constraint. ConstraintDescriptor instances of composing constraints reflect overridden
	 * parameter values in #getParameters() and #getAnnotation() 
	 *
	 * @return a set of ConstraintDescriptor object or an empty set
	 */
	Set<ConstraintDescriptor> getComposingConstraints();
}
