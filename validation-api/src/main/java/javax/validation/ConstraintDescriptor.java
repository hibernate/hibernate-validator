// $Id$
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
import java.util.List;

/**
 * Describes a single constraint and its composing constraints.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public interface ConstraintDescriptor {
	/**
	 * Returns the annotation describing the constraint declaration.
	 * If a composing constraint, parameter values are reflecting
	 * the overridden parameters from the main constraint
	 *
	 * @return The annotation for this constraint.
	 */
	Annotation getAnnotation();

	/**
	 * @return The groups the constraint is applied on.
	 */
	Set<Class<?>> getGroups();

	/**
	 * list of the constraint validation implementation classes
	 * The lsit is immutable
	 *
	 * @return list of the constraint validation implementation classes
	 */
	List<Class<? extends ConstraintValidator<?,?>>>
	    getConstraintValidatorClasses();

	/**
	 * Returns a map containing the annotation parameter names as keys and the
	 * annotation parameter values as value.
	 * If this constraint is used as part of a composed constraint, parameter
	 * values are reflecting the overridden parameters from the main constraint.
	 *
	 * @return Returns a map containing the annotation paramter names as keys
	 *         and the annotation parameter values as value.
	 */
	Map<String, Object> getParameters();

	/**
	 * Return a set of composing <code>ConstraintDescriptor</code>s where each
	 * descriptor describes a composing constraint. <code>ConstraintDescriptor</code>
	 * instances of composing constraints reflect overridden parameter values in
	 * {@link #getParameters()}  and {@link #getAnnotation()}.
	 *
	 * @return a set of <code>ConstraintDescriptor<code> objects or an empty set
	 *         in case there are no composing constraints.
	 */
	Set<ConstraintDescriptor> getComposingConstraints();

	/**
	 * @return true if the constraint is annotated with @ReportAsViolationFromCompositeConstraint
	 */
	boolean isReportAsViolationFromCompositeConstraint();
}