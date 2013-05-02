/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.xml.mixedconfiguration.annotation;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.test.internal.xml.mixedconfiguration.ICompetition;

public abstract class Competition implements ICompetition {

	@NotNull
	@Size(min = 1)
	private String name;

	public Competition() {
		super();
	}

	public Competition(String name) {
		setName( name );
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
