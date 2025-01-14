/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.customconstraints;

import java.time.Duration;
import java.util.Date;

import org.hibernate.validator.constraints.CreditCardNumber;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.LuhnCheck;
import org.hibernate.validator.constraints.Mod10Check;
import org.hibernate.validator.constraints.Mod11Check;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;
import org.hibernate.validator.constraints.br.TituloEleitoral;
import org.hibernate.validator.constraints.pl.NIP;
import org.hibernate.validator.constraints.pl.PESEL;
import org.hibernate.validator.constraints.pl.REGON;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;

@ScriptAssert(script = "some script", lang = "javascript")
public class HibernateValidatorProvidedCustomConstraints {

	/**
	 * Allowed.
	 */
	@CreditCardNumber
	@Length
	@LuhnCheck
	@Mod10Check
	@Mod11Check
	@Range
	@URL
	@CNPJ
	@CPF
	@TituloEleitoral
	@REGON
	@NIP
	@PESEL
	public String string;

	@DurationMax
	@DurationMin
	public Duration duration;

	/**
	 * Not allowed.
	 */
	@CreditCardNumber
	@Length
	@LuhnCheck
	@Mod10Check
	@Mod11Check
	@Range
	@URL
	@CNPJ
	@CPF
	@TituloEleitoral
	@REGON
	@NIP
	@PESEL
	@DurationMax
	@DurationMin
	public Date date;
}
