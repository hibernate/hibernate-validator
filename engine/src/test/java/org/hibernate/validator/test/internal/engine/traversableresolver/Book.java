/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.traversableresolver;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author Hardy Ferentschik
 */
@BookBusinessRules
@Entity
public class Book {
	@Id
	public Long id;

	@ManyToOne
	public Author author;
}


