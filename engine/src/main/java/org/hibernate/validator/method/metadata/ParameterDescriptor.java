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

import javax.validation.metadata.ElementDescriptor;

/**
 * Describes a constrained parameter and the constraints associated with it.
 *
 * @author Gunnar Morling
 * @deprecated Will by replaced by equivalent functionality defined by the Bean
 *             Validation 1.1 API as of Hibernate Validator 5.
 */
@Deprecated
public interface ParameterDescriptor extends ElementDescriptor {

	/**
	 * Whether cascaded validation for this parameter shall be
	 * performed or not. This is the case if this parameter is annotated with the
	 * {@link javax.validation.Valid} annotation either locally or in the inheritance hierarchy.
	 *
	 * @return <code>True</code>, if this parameter shall be
	 *         validated recursively, <code>false</code> otherwise.
	 */
	boolean isCascaded();

	/**
	 * Returns this parameter's index within the parameter array of the
	 * method holding it.
	 *
	 * @return This parameter's index.
	 */
	int getIndex();

}
