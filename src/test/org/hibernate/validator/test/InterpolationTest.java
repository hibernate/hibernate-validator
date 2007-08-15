//$Id: $
package org.hibernate.validator.test;

import java.util.MissingResourceException;

import junit.framework.TestCase;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;

/**
 * @author Emmanuel Bernard
 */
public class InterpolationTest extends TestCase {
    public void testMissingKey() {

        Building b = new Building();
        b.setAddress( "2323 Younge St");
		ClassValidator<Building> validator = new ClassValidator<Building>(Building.class);
		try {
            validator.getInvalidValues( b );
        }
        catch (MissingResourceException e) {
            fail("message should be interpolated lazily in DefaultMessageInterpolator");
        }

		b = new Building();
        b.setAddress("");
        boolean failure = false;
		InvalidValue[] invalidValues = validator.getInvalidValues( b );
		assertNotSame( "Should have a failure here", 0, invalidValues.length );
		assertEquals( "Missing key should be left unchanged",
                    "{notpresent.Key} and #{key.notPresent} and {key.notPresent2} 1",
                    invalidValues[0].getMessage() );
    }
}

