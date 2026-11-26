package org.hibernate.validator.ap.testmodel;

import java.math.BigInteger;

import org.hibernate.validator.constraints.Even;

public class ModelWithEvenConstraints {

	@Even
	public Integer integer;

	@Even
	public Long longValue;

	@Even
	public Byte byteValue;

	@Even
	public Short shortValue;

	@Even
	public BigInteger bigInteger;

	@Even
	public String string;

	@Even
	public CharSequence charSequence;
}
