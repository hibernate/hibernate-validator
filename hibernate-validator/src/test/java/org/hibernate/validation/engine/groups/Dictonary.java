// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine.groups;

import javax.validation.constraints.NotNull;

import org.hibernate.validation.constraints.NotEmpty;
import org.hibernate.validation.engine.groups.Book;

/**
 * @author Hardy Ferentschik
 */
public class Dictonary extends Book {
	@NotNull(groups = Translate.class)
	@NotEmpty(groups = Translate.class)
	private String translatesTo;

	@NotNull(groups = Translate.class)
	@NotEmpty(groups = Translate.class)
	private String translatesFrom;

	public String getTranslatesTo() {
		return translatesTo;
	}

	public void setTranslatesTo(String translatesTo) {
		this.translatesTo = translatesTo;
	}

	public String getTranslatesFrom() {
		return translatesFrom;
	}

	public void setTranslatesFrom(String translatesFrom) {
		this.translatesFrom = translatesFrom;
	}

	/**
	 * Translator related constraints
	 */
	public interface Translate {
	}
}
