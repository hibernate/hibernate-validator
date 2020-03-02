/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.sequence;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.GroupSequence;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.Assert;
import org.testng.annotations.Test;

@TestForIssue(jiraKey = "HV-1692")
public class SequenceOnObjectsWithCycles {

	@Test
	public void groupSequenceOfGroupSequences() {
		Validator validator = ValidatorUtil.getValidator();

		YourAnnotatedBean yourEntity1 = new YourAnnotatedBean();
		AnotherBean anotherBean = new AnotherBean();
		anotherBean.setYourAnnotatedBean( yourEntity1 );
		yourEntity1.setBean( anotherBean );

		Set<ConstraintViolation<YourAnnotatedBean>> constraintViolations = validator.validate( yourEntity1 );
		Assert.assertEquals( 0, constraintViolations.size() );

	}

	@GroupSequence({ AnotherBean.class, Magic.class })
	public class AnotherBean {

		@Valid
		private YourAnnotatedBean yourAnnotatedBean;


		public void setYourAnnotatedBean(YourAnnotatedBean yourAnnotatedBean) {
			this.yourAnnotatedBean = yourAnnotatedBean;
		}
	}

	@GroupSequence({ YourAnnotatedBean.class, Magic.class })
	public class YourAnnotatedBean {

		@Valid
		private AnotherBean bean;

		public void setBean(AnotherBean bean) {
			this.bean = bean;
		}
	}

	public interface Magic {
	}
}
