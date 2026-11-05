package org.hibernate.validator.integration.wildfly.generictype;

import jakarta.validation.constraints.Min;

public class RetailBillingService implements BillingService<String> {

	@Override
	@Min(0)
	public int getBillingAmount(String order) {
		return -1;
	}
}
