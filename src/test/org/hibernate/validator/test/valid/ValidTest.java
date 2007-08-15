//$Id: $
package org.hibernate.validator.test.valid;

import junit.framework.TestCase;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;

/**
 * @author Emmanuel Bernard
 */
public class ValidTest extends TestCase {
	public void testDeepValid() throws Exception {
		ClassValidator<Form> formValidator = new ClassValidator<Form>(Form.class);
		Address a = new Address();
		Member m = new Member();
		m.setAddress( a );
		Form f = new Form();
		f.setMember(m);
		InvalidValue[] values = formValidator.getInvalidValues( f );
		assertEquals( 1, values.length );
	}
}
