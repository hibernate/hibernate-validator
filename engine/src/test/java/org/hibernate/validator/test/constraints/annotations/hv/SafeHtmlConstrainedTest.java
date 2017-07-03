/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class SafeHtmlConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testSafeHtmlNumber() {
		Foo foo = new Foo( "<div>content</div>" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testSafeHtmlInvalid() {
		Foo foo = new Foo( "<script>alert('Doh')</script>" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( SafeHtml.class )
		);
	}

	private static class Foo {

		@SafeHtml
		private final String html;

		public Foo(String html) {
			this.html = html;
		}
	}
}
