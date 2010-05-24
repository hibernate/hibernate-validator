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
package org.hibernate.validator.cfg;

import java.lang.annotation.ElementType;
import javax.validation.Payload;
import javax.validation.constraints.DecimalMin;

/**
 * @author Hardy Ferentschik
 */
public class DecimalMinDefinition extends ConstraintDefinition<DecimalMin> {

	public DecimalMinDefinition(Class<?> beanType, String property, ElementType elementType, ConstraintMapping mapping) {
		super( beanType, DecimalMin.class, property, elementType, mapping );
	}

	public DecimalMinDefinition message(String message) {
		addParameter( "message", message );
		return this;
	}

	public DecimalMinDefinition groups(Class<?>... groups) {
		addParameter( "groups", groups );
		return this;
	}

	public DecimalMinDefinition payload(Class<? extends Payload>... payload) {
		addParameter( "payload", payload );
		return this;
	}

	public DecimalMinDefinition value(String min) {
		addParameter( "value", min );
		return this;
	}
}