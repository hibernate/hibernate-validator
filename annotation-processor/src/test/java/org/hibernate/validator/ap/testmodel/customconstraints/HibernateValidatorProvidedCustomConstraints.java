/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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

@ScriptAssert(script = "", lang = "javascript")
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
