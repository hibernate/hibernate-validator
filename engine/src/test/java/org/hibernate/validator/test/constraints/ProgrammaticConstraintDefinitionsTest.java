/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidatingProxy;
import static org.testng.Assert.fail;

import java.lang.annotation.ElementType;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.LuhnCheckDef;
import org.hibernate.validator.cfg.defs.ParameterScriptAssertDef;
import org.hibernate.validator.cfg.defs.SafeHtmlDef;
import org.hibernate.validator.cfg.defs.br.CNPJDef;
import org.hibernate.validator.cfg.defs.br.CPFDef;
import org.hibernate.validator.cfg.defs.br.TituloEleitoralDef;
import org.hibernate.validator.cfg.defs.pl.NIPDef;
import org.hibernate.validator.cfg.defs.pl.PESELDef;
import org.hibernate.validator.cfg.defs.pl.REGONDef;
import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.testutil.PrefixableParameterNameProvider;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class ProgrammaticConstraintDefinitionsTest {

	@Test
	public void countrySpecificProgrammaticDefinition() {
		doProgrammaticTest( new TituloEleitoralDef(), "038763000914", "48255-77", "invalid Brazilian Voter ID card number" );
		doProgrammaticTest( new CPFDef(), "134.241.313-00", "48255-77", "invalid Brazilian individual taxpayer registry number (CPF)" );
		doProgrammaticTest( new CNPJDef(), "91.509.901/0001-69", "91.509.901/0001-60", "invalid Brazilian corporate taxpayer registry number (CNPJ)" );

		doProgrammaticTest( new REGONDef(), "49905531368510", "49905531368512", "Invalid Polish Taxpayer Identification Number (REGON)" );
		doProgrammaticTest( new REGONDef(), "858336997", "691657185", "Invalid Polish Taxpayer Identification Number (REGON)" );
		doProgrammaticTest( new PESELDef(), "12252918020", "44051401358", "Invalid Polish National Identification Number (PESEL)" );
		doProgrammaticTest( new NIPDef(), "1786052059", "2596048505", "Invalid VAT Identification Number (NIP)" );

	}

	@Test
	public void safeHtmlProgrammaticDefinition() {
		doProgrammaticTest( new SafeHtmlDef().whitelistType( SafeHtml.WhiteListType.BASIC ), "<td>1234qwertd>", 1 );
		doProgrammaticTest( new SafeHtmlDef().whitelistType( SafeHtml.WhiteListType.NONE ), "test", 0 );
		doProgrammaticTest( new SafeHtmlDef().whitelistType( SafeHtml.WhiteListType.RELAXED ), "<td>1234qwer</td>", 0 );
		doProgrammaticTest( new SafeHtmlDef().whitelistType( SafeHtml.WhiteListType.NONE ).additionalTags( "td" ), "<td>1234qwer</td>", 0 );
	}

	@Test
	public void luhnCheckDefProgrammaticDefinition() {
		doProgrammaticTest( new LuhnCheckDef().startIndex( 0 )
									.endIndex( Integer.MAX_VALUE )
									.checkDigitIndex( -1 )
									.ignoreNonDigitCharacters( false ),
							"A79927398713", 1
		);
		doProgrammaticTest( new LuhnCheckDef().startIndex( 0 )
									.endIndex( Integer.MAX_VALUE )
									.checkDigitIndex( -1 )
									.ignoreNonDigitCharacters( true ),
							"A79927398713", 0
		);
	}

	@Test
	public void parameterScriptAssertDefProgrammaticDefinition() throws NoSuchMethodException {
		doExecutableProgrammaticTest( new ParameterScriptAssertDef().script( "param0.size() > 3" ).lang( "groovy" ), "asd", true );
		doExecutableProgrammaticTest( new ParameterScriptAssertDef().script( "param0.size() > 3" ).lang( "groovy" ), "asdqwe", false );
	}

	private void doExecutableProgrammaticTest(ParameterScriptAssertDef parameterScriptAssertDef, String content, boolean error)
			throws NoSuchMethodException {
		HibernateValidatorConfiguration config = Validation.byProvider( HibernateValidator.class ).configure();
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Foo.class )
				.ignoreAllAnnotations()
				.method( "setSource", String.class )
				.crossParameter()
				.constraint( parameterScriptAssertDef );

		Validator validator = config.addMapping( mapping )
				.parameterNameProvider( new PrefixableParameterNameProvider( "param" ) )
				.buildValidatorFactory()
				.getValidator();

		Foo bar = getValidatingProxy(
				new Bar( "" ),
				validator
		);

		try {
			bar.setSource( content );
			if ( error ) {
				fail( "Should throw an exception" );
			}
		}
		catch (ConstraintViolationException e) {
			if ( !error ) {
				fail( "Should not throw an exception" );
			}
		}
	}

	private void doProgrammaticTest(ConstraintDef<?, ?> def, String content, int numOfViolations) {
		HibernateValidatorConfiguration config = Validation.byProvider( HibernateValidator.class ).configure();
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Bar.class )
				.ignoreAllAnnotations()
				.property( "source", ElementType.FIELD )
				.constraint( def );

		Validator validator = config.addMapping( mapping )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<Bar>> violations = validator.validate( new Bar( content ) );
		assertNumberOfViolations( violations, numOfViolations );
	}

	private void doProgrammaticTest(ConstraintDef<?, ?> def, String validNum, String invalidNum, String message) {
		HibernateValidatorConfiguration config = Validation.byProvider( HibernateValidator.class ).configure();
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( OtherPerson.class )
				.ignoreAllAnnotations()
				.property( "number", ElementType.FIELD )
				.constraint( def );

		Validator validator = config.addMapping( mapping )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<OtherPerson>> violations = validator.validate( new OtherPerson( invalidNum ) );
		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, message );
		assertNumberOfViolations( validator.validate( new OtherPerson( validNum ) ), 0 );
	}

	@SuppressWarnings("unused")
	private static class OtherPerson {

		private String number;

		public OtherPerson(String number) {
			this.number = number;
		}

		public String getNumber() {
			return number;
		}

	}

	public interface Foo {
		String getSource();

		void setSource(String source);
	}

	private static class Bar implements Foo {

		private String source;

		public Bar(String source) {
			this.source = source;
		}

		@Override
		public String getSource() {
			return source;
		}

		@Override
		public void setSource(String source) {
			this.source = source;
		}
	}
}
