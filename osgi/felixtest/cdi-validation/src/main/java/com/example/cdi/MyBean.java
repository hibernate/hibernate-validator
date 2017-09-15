/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package com.example.cdi;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class MyBean {
    
    public void doDefault(@ValidNumber String number) {
    }

    public void doAll9(@ValidNumber("all9") String number) {
    }
}
