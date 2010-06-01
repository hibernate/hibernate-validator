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
import javax.validation.constraints.Digits;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.ConstraintMapping;


/**
 * @author Hardy Ferentschik
 */
public class DigitsDef extends ConstraintDef<Digits> {

	public DigitsDef(Class<?> beanType, String property, ElementType elementType, ConstraintMapping mapping) {
		super( beanType, Digits.class, property, elementType, mapping );
	}

	public DigitsDef message(String message) {
		super.message( message );
		return this;
	}

	public DigitsDef groups(Class<?>... groups) {
		super.groups( groups );
		return this;
	}

	public DigitsDef payload(Class<? extends Payload>... payload) {
		super.payload( payload );
		return this;
	}

	public DigitsDef integer(int integer) {
		addParameter( "integer", integer );
		return this;
	}

	public DigitsDef fraction(int fraction) {
		addParameter( "fraction", fraction );
		return this;
	}
}