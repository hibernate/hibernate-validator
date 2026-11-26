package org.hibernate.validator.ap.testmodel;

import java.math.BigInteger;

import org.hibernate.validator.constraints.Odd;

public class ModelWithOddConstraints {

	@Odd
	public Integer integer;

	@Odd
	public Long longValue;

	@Odd
	public Byte byteValue;

	@Odd
	public Short shortValue;

	@Odd
	public BigInteger bigInteger;

	@Odd
	public String string;

	@Odd
	public CharSequence charSequence;
}
