package org.hibernate.validator.test.constraints.impl;

import junit.framework.Assert;
import org.hibernate.validator.constraints.impl.FutureValidatorForAbstractInstant;
import org.hibernate.validator.constraints.impl.PastValidatorForAbstractInstant;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.MutableDateTime;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Kevin Pollet
 */
public class PastValidatorForAbstractInstantTest {

     private static PastValidatorForAbstractInstant validator;

     @BeforeClass
     public static void init() {
        validator = new PastValidatorForAbstractInstant();         
     }

     @Test
     public void testIsValidForInstant() {
         Instant future = new Instant().plus(31557600000l);
         Instant past = new Instant().minus(31557600000l);

         Assert.assertTrue(validator.isValid(null, null));
         Assert.assertTrue(validator.isValid(past, null));
         Assert.assertFalse(validator.isValid(new DateTime(), null));
         Assert.assertFalse(validator.isValid(future, null));

     }

     @Test
     public void testIsValidForDateTime() {
         DateTime future = new DateTime().plusYears(1);
         DateTime past = new DateTime().minusYears(1);

         Assert.assertTrue(validator.isValid(null, null));
         Assert.assertTrue(validator.isValid(past, null));
         Assert.assertFalse(validator.isValid(new DateTime(), null));
         Assert.assertFalse(validator.isValid(future, null));

     }

     @Test
     public void testIsValidForDateMidnight() {
         DateMidnight future = new DateMidnight().plusYears(1);
         DateMidnight past = new DateMidnight().minusYears(1);

         Assert.assertTrue(validator.isValid(null, null));
         Assert.assertTrue(validator.isValid(past, null));
         Assert.assertTrue(validator.isValid(new DateMidnight(), null));
         Assert.assertFalse(validator.isValid(future, null));

     }

     @Test
     public void testIsValidForMutableDateTime() {
         MutableDateTime future = new MutableDateTime();
         future.addYears(1);

         MutableDateTime past = new MutableDateTime();
         past.addYears(-1);

         Assert.assertTrue(validator.isValid(null, null));
         Assert.assertTrue(validator.isValid(past, null));
         Assert.assertFalse(validator.isValid(new MutableDateTime(), null));
         Assert.assertFalse(validator.isValid(future, null));

     }
    
}
