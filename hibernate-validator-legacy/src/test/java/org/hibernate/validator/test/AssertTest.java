package org.hibernate.validator.test;

import junit.framework.TestCase;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.hibernate.validator.AssertTrueValidator;
import org.hibernate.validator.AssertFalseValidator;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Test cases for <code>AssertFalse</code> and <code>AssertTrue</code>.
 *
 * @author Hardy Ferentschik
 * @see HV-53
 */
public class AssertTest extends TestCase {
    ClassValidator<BooleanHolder> classValidator;

    public void setUp() throws Exception {
        classValidator = new ClassValidator<BooleanHolder>(
                BooleanHolder.class, ResourceBundle.getBundle("messages", Locale.ENGLISH)
        );
    }

    public void testSuccessfulValidation() throws Exception {
        BooleanHolder bholder = new BooleanHolder();
        bholder.setMustBeTrue(true);
        bholder.setMustBeFalse(false);
        InvalidValue[] invalidValues = classValidator.getInvalidValues(bholder);
        assertTrue("should be empty", invalidValues.length == 0);
    }

    public void testUnSuccessfulValidation() throws Exception {
        BooleanHolder bholder = new BooleanHolder();
        bholder.setMustBeTrue(false);
        bholder.setMustBeFalse(true);
        InvalidValue[] invalidValues = classValidator.getInvalidValues(bholder);
        assertTrue("should be empty", invalidValues.length == 2);
    }

    public void testNullValidation() throws Exception {
        BooleanHolder bholder = new BooleanHolder();
        bholder.setMustBeTrue(null);
        bholder.setMustBeFalse(null);
        InvalidValue[] invalidValues = classValidator.getInvalidValues(bholder);
        assertTrue("should be empty", invalidValues.length == 0);
    }

    public void testNonBooleanValidation() throws Exception {
        AssertTrueValidator trueValidator = new AssertTrueValidator();
        assertFalse(trueValidator.isValid("foo"));

        AssertFalseValidator falseValidator = new AssertFalseValidator();
        assertFalse(falseValidator.isValid("foo"));
    }
}
