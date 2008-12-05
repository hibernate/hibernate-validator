package org.hibernate.validator.spec.s3.s4;

import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

/**
 * User representation
 *
 * @author Emmanuel Bernard
 */
public class User {
	@NotNull
	private String firstname;

	@NotNull(groups = Default.class)
	private String lastname;

	@NotNull(groups = {Billable.class, BuyInOneClick.class})
	private CreditCard defaultCreditCard;
}
