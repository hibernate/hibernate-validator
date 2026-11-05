package org.hibernate.validator.test.constraints.composition.validationtarget;

/**
 * @author Gunnar Morling
 */
public class InvoiceService {

	@ValidInvoiceAmount
	public long getInvoiceAmount(String orderId) {
		return 0;
	}
}
