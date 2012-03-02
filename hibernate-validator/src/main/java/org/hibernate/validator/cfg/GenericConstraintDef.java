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

package org.hibernate.validator.cfg;

import java.lang.annotation.Annotation;

/**
 * A {@link ConstraintDef} class which can be used to configure any constraint
 * type. For this purpose the class defines a generic method
 * {@link GenericConstraintDef#param(String, Object)} which allows to add
 * arbitrary constraint parameters.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class GenericConstraintDef<A extends Annotation> extends ConstraintDef<GenericConstraintDef<A>, A> {

	public GenericConstraintDef(Class<A> constraintType) {
		super( constraintType );
	}

	public GenericConstraintDef<A> param(String key, Object value) {
		addParameter( key, value );
		return this;
	}
}
