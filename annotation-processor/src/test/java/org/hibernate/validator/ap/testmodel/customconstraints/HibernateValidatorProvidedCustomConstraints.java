/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.customconstraints;

import java.util.Date;

import org.hibernate.validator.constraints.CreditCardNumber;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.LuhnCheck;
import org.hibernate.validator.constraints.Mod10Check;
import org.hibernate.validator.constraints.Mod11Check;
import org.hibernate.validator.constraints.ModCheck;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;
import org.hibernate.validator.constraints.br.TituloEleitoral;

@ScriptAssert(script = "some script", lang = "javascript")
@SuppressWarnings("deprecation")
public class HibernateValidatorProvidedCustomConstraints {

	/**
	 * Allowed.
	 */
	@CreditCardNumber
	@Email
	@Length
	@LuhnCheck
	@ModCheck(modType = ModCheck.ModType.MOD10, multiplier = 2)
	@Mod10Check
	@Mod11Check
	@NotBlank
	@NotEmpty
	@Range
	@SafeHtml
	@URL
	@CNPJ
	@CPF
	@TituloEleitoral
	public String string;

	/**
	 * Not allowed.
	 */
	@CreditCardNumber
	@Email
	@Length
	@LuhnCheck
	@ModCheck(modType = ModCheck.ModType.MOD10, multiplier = 2)
	@Mod10Check
	@Mod11Check
	@NotBlank
	@NotEmpty
	@Range
	@SafeHtml
	@URL
	@CNPJ
	@CPF
	@TituloEleitoral
	public Date date;
}
