/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.cdi.scope;

import java.util.Set;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

@Stateless
public class FooBean implements FooRemote, FooLocal {

	@Resource
	private SessionContext ctx;

	@Override
	public int verifyValidatorOnPojo(int testValue) {
		PojoBean pojo = new PojoBean( testValue );

		Set<ConstraintViolation<PojoBean>> violations = lookupValidatorInJNDI().validate( pojo );
		return violations.size();
	}

	@Override
	public Validator getValidator() {
		return lookupValidatorInJNDI();
	}

	private Validator lookupValidatorInJNDI() {
		return (Validator) ctx.lookup( "java:comp/Validator" );
	}

}

