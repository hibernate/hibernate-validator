/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.util.privilegedactions;

import static java.lang.Thread.currentThread;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import javax.validation.ValidationException;

import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class LoadClassTest {
    @Test
    public void testException1() {
        final LoadClass action = LoadClass.action("org.hibernate.validator.Dummy", null, false);
        try {
            action.run();
            fail("Should have thrown javax.validation.ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e.getCause(), "HV-1026: cause not set");
        }
    }

    @Test
    public void testException2() {
        final LoadClass action = LoadClass.action("org.hibernate.validator.Dummy", null, true);
        final ClassLoader current = currentThread().getContextClassLoader();
        try {
            currentThread().setContextClassLoader(null);
            try {
                action.run();
                fail("Should have thrown javax.validation.ValidationException");
            } catch (ValidationException e) {
                assertNotNull(e.getCause(), "HV-1026: cause not set");
            }
        } finally {
            currentThread().setContextClassLoader(current);
        }
    }
}
