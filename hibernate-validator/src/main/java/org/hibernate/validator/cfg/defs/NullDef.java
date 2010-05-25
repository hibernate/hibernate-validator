// $Id$
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
package org.hibernate.validator.cfg.defs;

import java.lang.annotation.ElementType;
import javax.validation.Payload;
import javax.validation.constraints.Null;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.ConstraintMapping;

/**
 * @author Hardy Ferentschik
 */
public class NullDef extends ConstraintDef<Null> {
	public NullDef(Class<?> beanType, String property, ElementType elementType, ConstraintMapping mapping) {
		super( beanType, Null.class, property, elementType, mapping );
	}

	public NullDef message(String message) {
		addParameter( "message", message );
		return this;
	}

	public NullDef groups(Class<?>... groups) {
		addParameter( "groups", groups );
		return this;
	}

	public NullDef payload(Class<? extends Payload>... payload) {
		addParameter( "payload", payload );
		return this;
	}
}