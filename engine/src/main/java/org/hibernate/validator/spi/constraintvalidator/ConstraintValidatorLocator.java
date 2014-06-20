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
package org.hibernate.validator.spi.constraintvalidator;

import java.util.List;

/**
 * A {@code ConstraintValidatorLocator} allows for the discovery of custom constraint validator instances.
 *
 * This is a Hibernate Validator specific feature and can be configured via
 * {@link org.hibernate.validator.HibernateValidatorConfiguration}.
 *
 * <p>
 * The default implementation uses Java's {@code ServiceLoader} approach to locate custom constraint validator
 * implementations.
 * </p>
 *
 * @author Hardy Ferentschik
 * @hv.experimental This API is considered experimental and may change in future revisions
 */
public interface ConstraintValidatorLocator {

	/**
	 * Returns a list of {@code ConstraintValidatorContribution} instances, each of which provides a list of constraint
	 * validator classes for a given constraint.
	 *
	 * @return returns a list of {@code ConstraintValidatorContribution} instances. If no custom constraints are found,
	 * the empty list is returned
	 */
	List<ConstraintValidatorContribution<?>> getConstraintValidatorContributions();

}
