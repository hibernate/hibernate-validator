package org.hibernate.validator.spec.s3.s4;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Represents an order in the system
 *
 * @author Emmanuel Bernard
 */
public class Order implements Auditable {
	private String creationDate;
	private String lastUpdate;
	private String lastModifier;
	private String lastReader;

	private String orderNumber;

	public String getCreationDate() {
		return this.creationDate;
	}

	public String getLastUpdate() {
		return this.lastUpdate;
	}

	public String getLastModifier() {
		return this.lastModifier;
	}

	public String getLastReader() {
		return this.lastReader;
	}

	@NotNull @Size(min=10, max=10)
	public String getOrderNumber() {
		return this.orderNumber;
	}
}
