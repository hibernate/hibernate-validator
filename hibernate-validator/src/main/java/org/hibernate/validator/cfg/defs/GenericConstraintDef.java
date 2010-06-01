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

// $Id$

package org.hibernate.validator.cfg.defs;

import java.lang.annotation.ElementType;
import javax.validation.Payload;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.ConstraintMapping;

/**
 * A {@code ConstraintDef} class which can be used to configure any constraint type. For this purpose the class defines
 * a {@code constraintType}  method to specify the constraint type and a generic {@code param(String key,Object value)}
 * to add arbitrary constraint parameters.
 *
 * @author Hardy Ferentschik
 */
public class GenericConstraintDef extends ConstraintDef {

	public GenericConstraintDef(Class<?> beanType, String property, ElementType elementType, ConstraintMapping mapping) {
		super( beanType, null, property, elementType, mapping );
	}

	public GenericConstraintDef message(String message) {
		super.message( message );
		return this;
	}

	public GenericConstraintDef groups(Class<?>... groups) {
		super.groups( groups );
		return this;
	}

	public GenericConstraintDef payload(Class<? extends Payload>... payload) {
		super.payload( payload );
		return this;
	}

	public GenericConstraintDef param(String key, Object value) {
		addParameter( key, value );
		return this;
	}

	public GenericConstraintDef constraintType(Class<?> constraintType) {
		this.constraintType = constraintType;
		return this;
	}
}