package org.hibernate.validator.spec.s3.s4;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.GroupSequence;
import javax.validation.groups.Default;

/**
 * @author Emmanuel Bernard
 */
@ZipCodeCoherenceChecker(groups = Address.HighLevelCoherence.class)
public class Address {
	@NotNull @Size(max = 50)
	private String street1;

	@ZipCode
	private String zipcode;

	@NotNull @Size(max = 30)
	private String city;

	/**
	 * check conherence on the overall object
	 * Needs basic checking to be green first
	 */
	public interface HighLevelCoherence {}

	/**
	 * check both basic constraints and high level ones.
	 * high level constraints are not cheked if basic constraints fail
	 */
	@GroupSequence(sequence = {Default.class, HighLevelCoherence.class})
	public interface Complete {}
}
