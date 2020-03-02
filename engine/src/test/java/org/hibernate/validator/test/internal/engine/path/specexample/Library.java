/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.path.specexample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * @author Guillaume Smet
 */
public class Library {

	private String name;

	private String location;

	private List<Book> books = new ArrayList<>();

	private Map<Author, Book> mostPopularBookPerAuthor = new HashMap<>();

	public Library(@NotNull String name, @NotNull String location) {
		this.name = name;
		this.location = location;
	}

	public void addBook(@NotNull @Valid Book book) {
		books.add( book );
	}

	public void addAllBooks(@NotNull List<@Valid Book> books) {
		books.addAll( books );
	}

	public String getName() {
		return name;
	}

	@NotNull
	public String getLocation() {
		return location;
	}

	public void putMostPopularBookPerAuthor(Author author, Book book) {
		mostPopularBookPerAuthor.put( author, book );
	}

	public Map<Author, @Valid Book> getMostPopularBookPerAuthor() {
		return mostPopularBookPerAuthor;
	}
}
