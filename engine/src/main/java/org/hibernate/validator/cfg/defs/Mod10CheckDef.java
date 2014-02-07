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

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Mod10Check;

/**
 * @author Hardy Ferentschik
 */
public class Mod10CheckDef extends ConstraintDef<Mod10CheckDef, Mod10Check> {

	public Mod10CheckDef() {
		super( Mod10Check.class );
	}

	public Mod10CheckDef multiplier(int multiplier) {
		addParameter( "multiplier", multiplier );
		return this;
	}

	public Mod10CheckDef weight(int weight) {
		addParameter( "weight", weight );
		return this;
	}

	public Mod10CheckDef startIndex(int startIndex) {
		addParameter( "startIndex", startIndex );
		return this;
	}

	public Mod10CheckDef endIndex(int endIndex) {
		addParameter( "endIndex", endIndex );
		return this;
	}

	public Mod10CheckDef checkDigitIndex(int checkDigitIndex) {
		addParameter( "checkDigitIndex", checkDigitIndex );
		return this;
	}

	public Mod10CheckDef ignoreNonDigitCharacters(boolean ignoreNonDigitCharacters) {
		addParameter( "ignoreNonDigitCharacters", ignoreNonDigitCharacters );
		return this;
	}
}
