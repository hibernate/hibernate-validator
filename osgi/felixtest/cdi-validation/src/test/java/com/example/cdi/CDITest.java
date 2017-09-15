/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package com.example.cdi;

import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.rules.ExpectedException.none;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CDITest {

    @Inject
    private MyBean bean;

    @Rule
    public ExpectedException thrown = none();

    @Deployment
    public static Archive<?> deployment() {
        WebArchive war = create(WebArchive.class)
            .addClasses(
                MyBean.class, 
                ValidNumber.class, 
                ValidNumberValidator.class);

        System.out.println(war.toString(true));
        
        return war;
    }

    @Test
    public void testDefault() {
        bean.doDefault("123456");
    }
    
    @Test
    public void testAll9Valid() {
        bean.doAll9("99999");
    }

    @Test
    public void testAll9Invalid() {
        thrown.expectMessage("invalid number");
        bean.doAll9("999949");
    }

}
