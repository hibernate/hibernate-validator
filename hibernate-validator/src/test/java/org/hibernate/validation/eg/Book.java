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
package org.hibernate.validation.eg;

import javax.validation.GroupSequence;
import javax.validation.Valid;
import javax.validation.groups.Default;
import javax.validation.constraints.NotNull;

import org.hibernate.validation.constraints.Length;
import org.hibernate.validation.constraints.NotEmpty;
import org.hibernate.validation.eg.groups.First;
import org.hibernate.validation.eg.groups.Second;
import org.hibernate.validation.eg.groups.Last;

/**
 * @author Hardy Ferentschik
 */
@GroupSequence(name = Default.class, value = { First.class, Second.class, Last.class })
public class Book {
	@NotNull(groups = First.class)
	@NotEmpty(groups = First.class)
	private String title;

	@Length(max = 30, groups = Second.class)
	private String subtitle;

	@Valid
	@NotNull(groups = First.class)
	private Author author;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}
}
