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
import org.hibernate.validator.constraints.Mod11Check;

/**
 * @author Hardy Ferentschik
 */
public class Mod11CheckDef extends ConstraintDef<Mod11CheckDef, Mod11Check> {

	public Mod11CheckDef() {
		super( Mod11Check.class );
	}

	public Mod11CheckDef threshold(int threshold) {
		addParameter( "threshold", threshold );
		return this;
	}

	public Mod11CheckDef startIndex(int startIndex) {
		addParameter( "startIndex", startIndex );
		return this;
	}

	public Mod11CheckDef endIndex(int endIndex) {
		addParameter( "endIndex", endIndex );
		return this;
	}

	public Mod11CheckDef checkDigitIndex(int checkDigitIndex) {
		addParameter( "checkDigitIndex", checkDigitIndex );
		return this;
	}

	public Mod11CheckDef ignoreNonDigitCharacters(boolean ignoreNonDigitCharacters) {
		addParameter( "ignoreNonDigitCharacters", ignoreNonDigitCharacters );
		return this;
	}

	public Mod11CheckDef treatCheck10As(char treatCheck10As) {
		addParameter( "treatCheck10As", treatCheck10As );
		return this;
	}

	public Mod11CheckDef treatCheck11As(char treatCheck11As) {
		addParameter( "treatCheck11As", treatCheck11As );
		return this;
	}

	public Mod11CheckDef processingDirection(Mod11Check.ProcessingDirection processingDirection) {
		addParameter( "processingDirection", processingDirection );
		return this;
	}
}
