/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.path.specexample;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;

@AvailableInStore(groups = { Availability.class })
public class Book {

	@NonEmpty(groups = { FirstLevelCheck.class, Default.class })
	private String title;

	@Valid
	@NotNull
	private List<Author> authors;

	@Valid
	private Map<String, Review> reviewsPerSource;

	@Valid
	private Review pickedReview;

	private List<@NotBlank String> tags;

	private Map<Integer, List<@NotBlank String>> tagsByChapter;

	private List<@Valid Category> categories;

	private Map<Integer, List<@Valid Author>> authorsByChapter;

	// [...]

	public Book() {
	}

	public Book(String title, Author author) {
		this.title = title;
		this.authors = Arrays.asList( author );
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(List<Author> authors) {
		this.authors = authors;
	}

	public Map<String, Review> getReviewsPerSource() {
		return reviewsPerSource;
	}

	public void setReviewsPerSource(Map<String, Review> reviewsPerSource) {
		this.reviewsPerSource = reviewsPerSource;
	}

	public Review getPickedReview() {
		return pickedReview;
	}

	public void setPickedReview(Review pickedReview) {
		this.pickedReview = pickedReview;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public Map<Integer, List<String>> getTagsByChapter() {
		return tagsByChapter;
	}

	public void setTagsByChapter(Map<Integer, List<String>> tagsByChapter) {
		this.tagsByChapter = tagsByChapter;
	}

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

	public Map<Integer, List<Author>> getAuthorsByChapter() {
		return authorsByChapter;
	}

	public void setAuthorsByChapter(Map<Integer, List<Author>> authorsByChapter) {
		this.authorsByChapter = authorsByChapter;
	}
}
