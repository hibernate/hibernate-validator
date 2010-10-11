package org.hibernate.validator.test;

import junit.framework.TestCase;
import org.hibernate.validator.ClassValidator;

/**
 * @author Emmanuel Bernard
 */
public class EmailTest extends TestCase {
	private ClassValidator<User> userValidator;

	public void testEmail() throws Exception {
		userValidator = new ClassValidator<User>(User.class);
		isRightEmail("emmanuel@hibernate.org");
		isRightEmail("");
		isRightEmail(null);
		isRightEmail("emmanuel@hibernate");
		isRightEmail("emma-n_uel@hibernate");
		isRightEmail("emma+nuel@hibernate.org");
		isRightEmail("emma=nuel@hibernate.org");
		isRightEmail("emmanuel@[123.12.2.11]");
		isWrongEmail("emmanuel.hibernate.org");
		isWrongEmail("emma nuel@hibernate.org");
		isWrongEmail("emma(nuel@hibernate.org");
		isWrongEmail("emmanuel@");
		isWrongEmail("emma\nnuel@hibernate.org");
		isWrongEmail("emma@nuel@hibernate.org");
	}

	private void isRightEmail(String email) {
		assertEquals("Wrong email", 0, userValidator.getPotentialInvalidValues("email", email).length);
	}

	private void isWrongEmail(String email) {
		assertEquals("Right email", 1, userValidator.getPotentialInvalidValues("email", email).length);
	}
}
