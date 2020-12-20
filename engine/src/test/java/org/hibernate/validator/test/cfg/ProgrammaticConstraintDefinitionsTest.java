/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cfg;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidatingProxy;
import static org.testng.Assert.fail;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.LuhnCheckDef;
import org.hibernate.validator.cfg.defs.ParameterScriptAssertDef;
import org.hibernate.validator.cfg.defs.br.CNPJDef;
import org.hibernate.validator.cfg.defs.br.CPFDef;
import org.hibernate.validator.cfg.defs.br.TituloEleitoralDef;
import org.hibernate.validator.cfg.defs.pl.NIPDef;
import org.hibernate.validator.cfg.defs.pl.PESELDef;
import org.hibernate.validator.cfg.defs.pl.REGONDef;
import org.hibernate.validator.cfg.defs.ru.INNDef;
import org.hibernate.validator.constraints.LuhnCheck;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;
import org.hibernate.validator.constraints.br.TituloEleitoral;
import org.hibernate.validator.constraints.pl.NIP;
import org.hibernate.validator.constraints.pl.PESEL;
import org.hibernate.validator.constraints.pl.REGON;
import org.hibernate.validator.constraints.ru.INN;
import org.hibernate.validator.testutil.PrefixableParameterNameProvider;
import org.testng.annotations.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * @author Marko Bekhta
 */
public class ProgrammaticConstraintDefinitionsTest {

	@Test
	public void countrySpecificProgrammaticDefinition() {
		doProgrammaticTest( TituloEleitoral.class, new TituloEleitoralDef(), "038763000914", "48255-77", "invalid Brazilian Voter ID card number" );
		doProgrammaticTest( CPF.class, new CPFDef(), "134.241.313-00", "48255-77", "invalid Brazilian individual taxpayer registry number (CPF)" );
		doProgrammaticTest( CNPJ.class, new CNPJDef(), "91.509.901/0001-69", "91.509.901/0001-60",
				"invalid Brazilian corporate taxpayer registry number (CNPJ)"
		);

		doProgrammaticTest( REGON.class, new REGONDef(), "49905531368510", "49905531368512", "invalid Polish Taxpayer Identification Number (REGON)" );
		doProgrammaticTest( REGON.class, new REGONDef(), "858336997", "691657185", "invalid Polish Taxpayer Identification Number (REGON)" );
		doProgrammaticTest( PESEL.class, new PESELDef(), "12252918020", "44051401358", "invalid Polish National Identification Number (PESEL)" );
		doProgrammaticTest( NIP.class, new NIPDef(), "1786052059", "2596048505", "invalid VAT Identification Number (NIP)" );

		doProgrammaticTest( INN.class, new INNDef().type( INN.Type.INDIVIDUAL ), "127530851622", "127530851623", "invalid Russian taxpayer identification number (INN)" );
		doProgrammaticTest( INN.class, new INNDef().type( INN.Type.JURIDICAL ), "8606995694", "8606995695", "invalid Russian taxpayer identification number (INN)" );
	}

	@Test
	public void luhnCheckDefProgrammaticDefinition() {
		doProgrammaticTest( LuhnCheck.class, new LuhnCheckDef().startIndex( 0 )
						.endIndex( Integer.MAX_VALUE )
						.checkDigitIndex( -1 )
						.ignoreNonDigitCharacters( false ),
				"A79927398713", 1
		);
		doProgrammaticTest( LuhnCheck.class, new LuhnCheckDef().startIndex( 0 )
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

	private void doProgrammaticTest(Class<? extends Annotation> constraint, ConstraintDef<?, ?> def, String content, int numOfViolations) {
		HibernateValidatorConfiguration config = Validation.byProvider( HibernateValidator.class ).configure();
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Bar.class )
				.ignoreAllAnnotations()
				.field( "source" )
				.constraint( def );

		Validator validator = config.addMapping( mapping )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<Bar>> violations = validator.validate( new Bar( content ) );
		if ( numOfViolations > 0 ) {
			assertThat( violations ).containsOnlyViolations(
					violationOf( constraint )
			);
		}
		else {
			assertNoViolations( violations );
		}
	}

	private void doProgrammaticTest(Class<? extends Annotation> constraint, ConstraintDef<?, ?> def, String validNum, String invalidNum, String message) {
		HibernateValidatorConfiguration config = Validation.byProvider( HibernateValidator.class ).configure();
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( OtherPerson.class )
				.ignoreAllAnnotations()
				.field( "number" )
				.constraint( def );

		Validator validator = config.addMapping( mapping )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<OtherPerson>> violations = validator.validate( new OtherPerson( invalidNum ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( constraint ).withMessage( message )
		);
		assertNoViolations( validator.validate( new OtherPerson( validNum ) ) );
	}

	@SuppressWarnings("unused")
	private static class OtherPerson {

		private final String number;

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
