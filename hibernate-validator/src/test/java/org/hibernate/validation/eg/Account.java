package org.hibernate.validation.eg;

import javax.validation.Valid;

/**
 * Class with no constraints but with a cascade @Valid annotation
 */
public class Account {
	private String accountLogin;
	private Customer customer;

	public String getAccountLogin() {
		return accountLogin;
	}

	public void setAccountLogin(String accountLogin) {
		this.accountLogin = accountLogin;
	}

	@Valid
	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
}
