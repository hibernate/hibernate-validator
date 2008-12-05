package org.hibernate.validator.spec.s3.s4;

import javax.validation.constraints.NotNull;

/**
 * Auditable object contract
 *
 * @author Emmanuel Bernard
 */
public interface Auditable {
	@NotNull
	String getCreationDate();

	@NotNull
	String getLastUpdate();

	@NotNull
	String getLastModifier();

	@NotNull
	String getLastReader();
}
