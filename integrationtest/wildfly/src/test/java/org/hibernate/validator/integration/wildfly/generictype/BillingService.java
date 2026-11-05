package org.hibernate.validator.integration.wildfly.generictype;

public interface BillingService<T> {

	int getBillingAmount(T order);
}
