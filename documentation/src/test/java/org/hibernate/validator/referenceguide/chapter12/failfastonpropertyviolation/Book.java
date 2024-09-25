/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
// tag::include[]
package org.hibernate.validator.referenceguide.chapter12.failfastonpropertyviolation;

//end::include[]
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.constraints.ISBN;

//tag::include[]
@NonSelfPublishing
public class Book {

	@ISBN
	private String isbn;

	@NotBlank
	private String title;

	@NotNull
	private Person author;

	@NotNull
	private Person publisher;

	//constructors, getters and setters...
	//end::include[]

	public Book(String isbn, String title, Person author, Person publisher) {
		this.isbn = isbn;
		this.title = title;
		this.author = author;
		this.publisher = publisher;
	}

	public String getIsbn() {
		return this.isbn;
	}

	public String getTitle() {
		return this.title;
	}

	public Person getAuthor() {
		return this.author;
	}

	public Person getPublisher() {
		return this.publisher;
	}

	//tag::include[]
}
//end::include[]
