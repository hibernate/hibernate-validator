/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap;

import static org.hibernate.validator.ap.testutil.CompilerTestHelper.assertThatDiagnosticsMatch;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.EnumSet;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;

import org.hibernate.validator.ap.testmodel.FieldLevelValidationUsingBuiltInConstraints;
import org.hibernate.validator.ap.testmodel.MethodLevelValidationUsingBuiltInConstraints;
import org.hibernate.validator.ap.testmodel.ModelWithCodePointLengthConstraints;
import org.hibernate.validator.ap.testmodel.ModelWithDateConstraints;
import org.hibernate.validator.ap.testmodel.ModelWithISBNConstraints;
import org.hibernate.validator.ap.testmodel.ModelWithJava8DateTime;
import org.hibernate.validator.ap.testmodel.ModelWithJavaMoneyTypes;
import org.hibernate.validator.ap.testmodel.ModelWithJodaTypes;
import org.hibernate.validator.ap.testmodel.ModelWithUniqueElementsConstraints;
import org.hibernate.validator.ap.testmodel.ModelWithoutConstraints;
import org.hibernate.validator.ap.testmodel.MultipleConstraintsOfSameType;
import org.hibernate.validator.ap.testmodel.ValidationUsingAtValidAnnotation;
import org.hibernate.validator.ap.testmodel.boxing.ValidLong;
import org.hibernate.validator.ap.testmodel.boxing.ValidLongValidator;
import org.hibernate.validator.ap.testmodel.boxing.ValidationUsingBoxing;
import org.hibernate.validator.ap.testmodel.classlevelconstraints.ClassLevelValidation;
import org.hibernate.validator.ap.testmodel.classlevelconstraints.ValidCustomer;
import org.hibernate.validator.ap.testmodel.classlevelconstraints.ValidCustomerValidator;
import org.hibernate.validator.ap.testmodel.composedconstraint.FieldLevelValidationUsingComposedConstraint;
import org.hibernate.validator.ap.testmodel.composedconstraint.ValidOrderNumber;
import org.hibernate.validator.ap.testmodel.composedconstraint2.ComposedConstraint;
import org.hibernate.validator.ap.testmodel.composedconstraint2.ComposingConstraint1;
import org.hibernate.validator.ap.testmodel.composedconstraint2.ComposingConstraint1ValidatorForGregorianCalendar;
import org.hibernate.validator.ap.testmodel.composedconstraint2.ComposingConstraint1ValidatorForList;
import org.hibernate.validator.ap.testmodel.composedconstraint2.ComposingConstraint1ValidatorForString;
import org.hibernate.validator.ap.testmodel.composedconstraint2.ComposingConstraint2;
import org.hibernate.validator.ap.testmodel.composedconstraint2.ComposingConstraint2ValidatorForArrayList;
import org.hibernate.validator.ap.testmodel.composedconstraint2.ComposingConstraint2ValidatorForCalendar;
import org.hibernate.validator.ap.testmodel.composedconstraint2.ComposingConstraint2ValidatorForCollection;
import org.hibernate.validator.ap.testmodel.composedconstraint2.FieldLevelValidationUsingComplexComposedConstraint;
import org.hibernate.validator.ap.testmodel.crossparameters.GenericCrossParameterValidator;
import org.hibernate.validator.ap.testmodel.crossparameters.GenericCrossParameterValidatorObjectArray;
import org.hibernate.validator.ap.testmodel.crossparameters.GenericNormalValidator;
import org.hibernate.validator.ap.testmodel.crossparameters.MethodLevelValidationUsingCrossParameterConstraints;
import org.hibernate.validator.ap.testmodel.crossparameters.ValidCrossParameterAndNormalConstraint;
import org.hibernate.validator.ap.testmodel.crossparameters.ValidCrossParameterConstraint;
import org.hibernate.validator.ap.testmodel.customconstraints.BeanValidationConstraints;
import org.hibernate.validator.ap.testmodel.customconstraints.CaseMode;
import org.hibernate.validator.ap.testmodel.customconstraints.CheckCase;
import org.hibernate.validator.ap.testmodel.customconstraints.CheckCaseValidator;
import org.hibernate.validator.ap.testmodel.customconstraints.FieldLevelValidationUsingCustomConstraints;
import org.hibernate.validator.ap.testmodel.customconstraints.HibernateValidatorProvidedCustomConstraints;
import org.hibernate.validator.ap.testmodel.customconstraints.UnwrappingConstraints;
import org.hibernate.validator.ap.testmodel.groupsequenceprovider.BazDefaultGroupSequenceProvider;
import org.hibernate.validator.ap.testmodel.groupsequenceprovider.FooBarBazDefaultGroupSequenceProvider;
import org.hibernate.validator.ap.testmodel.groupsequenceprovider.FooBarDefaultGroupSequenceProvider;
import org.hibernate.validator.ap.testmodel.groupsequenceprovider.FooDefaultGroupSequenceProvider;
import org.hibernate.validator.ap.testmodel.groupsequenceprovider.GroupSequenceProviderDefinition;
import org.hibernate.validator.ap.testmodel.groupsequenceprovider.QuxDefaultGroupSequenceProvider;
import org.hibernate.validator.ap.testmodel.groupsequenceprovider.SampleDefaultGroupSequenceProvider;
import org.hibernate.validator.ap.testmodel.inheritedvalidator.AbstractCustomConstraintValidator;
import org.hibernate.validator.ap.testmodel.inheritedvalidator.CustomConstraint;
import org.hibernate.validator.ap.testmodel.inheritedvalidator.CustomConstraintValidator;
import org.hibernate.validator.ap.testmodel.inheritedvalidator.FieldLevelValidationUsingInheritedValidator;
import org.hibernate.validator.ap.testmodel.nouniquevalidatorresolution.NoUniqueValidatorResolution;
import org.hibernate.validator.ap.testmodel.nouniquevalidatorresolution.SerializableCollection;
import org.hibernate.validator.ap.testmodel.nouniquevalidatorresolution.Size;
import org.hibernate.validator.ap.testmodel.nouniquevalidatorresolution.SizeValidatorForCollection;
import org.hibernate.validator.ap.testmodel.nouniquevalidatorresolution.SizeValidatorForSerializable;
import org.hibernate.validator.ap.testmodel.nouniquevalidatorresolution.SizeValidatorForSet;
import org.hibernate.validator.ap.testmodel.overriding.MethodOverridingTests;
import org.hibernate.validator.ap.testutil.CompilerTestHelper.Library;
import org.hibernate.validator.ap.util.DiagnosticExpectation;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

/**
 * Miscellaneous tests for {@link ConstraintValidationProcessor}.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class ConstraintValidationProcessorTest extends ConstraintValidationProcessorTestBase {

	@Test
	public void fieldLevelValidationUsingBuiltInConstraints() {

		File sourceFile = compilerHelper.getSourceFile( FieldLevelValidationUsingBuiltInConstraints.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics, new DiagnosticExpectation( Kind.ERROR, 43 ), new DiagnosticExpectation( Kind.ERROR, 49 )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-840")
	public void overridingMethodParameterConstraintsTest() {
		File sourceFile = compilerHelper.getSourceFile( MethodOverridingTests.class );

		boolean compilationResult = compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 39 ),
				new DiagnosticExpectation( Kind.ERROR, 56 ),
				new DiagnosticExpectation( Kind.ERROR, 71 ),
				new DiagnosticExpectation( Kind.ERROR, 90 ),
				new DiagnosticExpectation( Kind.ERROR, 144 ),
				new DiagnosticExpectation( Kind.ERROR, 152 ),
				new DiagnosticExpectation( Kind.ERROR, 169 ),
				new DiagnosticExpectation( Kind.ERROR, 191 ),
				new DiagnosticExpectation( Kind.ERROR, 219 ),
				new DiagnosticExpectation( Kind.ERROR, 373 ),
				new DiagnosticExpectation( Kind.ERROR, 387 ),
				new DiagnosticExpectation( Kind.ERROR, 409 ),
				new DiagnosticExpectation( Kind.ERROR, 434 )
		);

		assertEquals( diagnostics.getDiagnostics().get( 0 ).getMessage( Locale.getDefault() ),
				"Method parameters do not respect the inheritance rules. " +
				"In subtypes, no parameter constraints may be declared on overridden or implemented methods, nor may parameters be marked for cascaded validation. " +
				"Types that contain the overridden methods are: [ org.hibernate.validator.ap.testmodel.overriding.MethodOverridingTests.MethodOverridingTestCase1 ]." );

		assertEquals( diagnostics.getDiagnostics().get( 11 ).getMessage( Locale.getDefault() ),
				"Method parameters do not respect inheritance rules. " +
				"If a subtype overrides/implements a method originally defined in several parallel types of the hierarchy, " +
				"no parameter constraints may be declared for that method nor parameters be marked for cascaded validation. " +
				"Parallel method definitions are in: [ java.lang.Object, org.hibernate.validator.ap.testmodel.overriding.MethodOverridingTests.Case15.SimpleService ]." );
	}

	@Test
	@TestForIssue(jiraKey = "HV-567")
	public void hibernateValidatorProvidedCustomConstraints() {
		File sourceFile = compilerHelper.getSourceFile( HibernateValidatorProvidedCustomConstraints.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 68 ),
				new DiagnosticExpectation( Kind.ERROR, 69 ),
				new DiagnosticExpectation( Kind.ERROR, 70 ),
				new DiagnosticExpectation( Kind.ERROR, 71 ),
				new DiagnosticExpectation( Kind.ERROR, 72 ),
				new DiagnosticExpectation( Kind.ERROR, 73 ),
				new DiagnosticExpectation( Kind.ERROR, 74 ),
				new DiagnosticExpectation( Kind.ERROR, 75 ),
				new DiagnosticExpectation( Kind.ERROR, 76 ),
				new DiagnosticExpectation( Kind.ERROR, 77 ),
				new DiagnosticExpectation( Kind.ERROR, 78 ),
				new DiagnosticExpectation( Kind.ERROR, 79 ),
				new DiagnosticExpectation( Kind.ERROR, 80 ),
				new DiagnosticExpectation( Kind.ERROR, 81 ),
				new DiagnosticExpectation( Kind.ERROR, 82 ),
				new DiagnosticExpectation( Kind.ERROR, 83 ),
				new DiagnosticExpectation( Kind.ERROR, 84 ),
				new DiagnosticExpectation( Kind.ERROR, 85 ),
				new DiagnosticExpectation( Kind.ERROR, 86 ),
				new DiagnosticExpectation( Kind.ERROR, 87 )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1297")
	public void beanValidationConstraints() {
		File sourceFile = compilerHelper.getSourceFile( BeanValidationConstraints.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 43 ),
				new DiagnosticExpectation( Kind.ERROR, 44 ),
				new DiagnosticExpectation( Kind.ERROR, 45 ),
				new DiagnosticExpectation( Kind.ERROR, 46 ),
				new DiagnosticExpectation( Kind.ERROR, 47 ),
				new DiagnosticExpectation( Kind.ERROR, 50 ),
				new DiagnosticExpectation( Kind.ERROR, 51 ),
				new DiagnosticExpectation( Kind.ERROR, 52 ),
				new DiagnosticExpectation( Kind.ERROR, 55 ),
				new DiagnosticExpectation( Kind.ERROR, 56 )
		);
	}

	/**
	 * HV-575. Make sure that the AP can be applied to projects which don't have
	 * the BV API or Hibernate Validator on the class path without failing.
	 */
	@Test
	public void modelWithoutConstraintsCanBeProcessedWithoutBvAndHvOnClassPath() {

		File sourceFile = compilerHelper.getSourceFile( ModelWithoutConstraints.class );

		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(),
						diagnostics,
						EnumSet.noneOf( Library.class ),
						sourceFile
				);

		assertTrue( compilationResult );
	}

	/**
	 * HV-575. Make sure that @Past/@Future can be validated for JDK types, also
	 * if JodaTime isn't available.
	 */
	@Test
	public void modelWithDateConstraintsCanBeProcessedWithoutJodaTimeOnClassPath() {

		File sourceFile = compilerHelper.getSourceFile( ModelWithDateConstraints.class );

		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(),
						diagnostics,
						EnumSet.of( Library.VALIDATION_API ),
						sourceFile
				);

		assertFalse( compilationResult );

		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 18 ),
				new DiagnosticExpectation( Kind.ERROR, 19 )
		);
	}

	/**
	 * HV-575. Missing classes shall not break the AP, instead the compiler
	 * should display appropriate errors.
	 */
	@Test
	public void missingClassesHandledByCompiler() {

		File sourceFile = compilerHelper.getSourceFile( ModelWithDateConstraints.class );

		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(),
						diagnostics,
						EnumSet.noneOf( Library.class ),
						sourceFile
				);

		assertFalse( compilationResult );

		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 10 ),
				new DiagnosticExpectation( Kind.ERROR, 11 ),
				new DiagnosticExpectation( Kind.ERROR, 18 ),
				new DiagnosticExpectation( Kind.ERROR, 19 ),
				new DiagnosticExpectation( Kind.ERROR, 22 ),
				new DiagnosticExpectation( Kind.ERROR, 23 )
		);
	}

	@Test
	public void testThatProcessorOptionsAreEvaluated() {

		File sourceFile = compilerHelper.getSourceFile( FieldLevelValidationUsingBuiltInConstraints.class );

		// compile with -AdiagnosticKind=Kind.WARNING and -Averbose=true
		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(),
						diagnostics,
						Kind.WARNING,
						true,
						false,
						EnumSet.allOf( Library.class ),
						sourceFile
				);

		// compilation succeeds as there are problems, but Kind.WARNING won't stop compilation
		assertTrue( compilationResult );

		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.NOTE, Diagnostic.NOPOS ), //says that verbose messaging is enabled
				new DiagnosticExpectation( Kind.WARNING, 43 ),
				new DiagnosticExpectation( Kind.WARNING, 49 )
		);
	}

	@Test
	public void fieldLevelValidationUsingCustomConstraints() {

		File sourceFile1 = compilerHelper.getSourceFile( FieldLevelValidationUsingCustomConstraints.class );
		File sourceFile2 = compilerHelper.getSourceFile( CheckCase.class );
		File sourceFile3 = compilerHelper.getSourceFile( CaseMode.class );
		File sourceFile4 = compilerHelper.getSourceFile( CheckCaseValidator.class );

		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(),
						diagnostics,
						sourceFile1,
						sourceFile2,
						sourceFile3,
						sourceFile4
				);

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch( diagnostics, new DiagnosticExpectation( Kind.ERROR, 19 ) );
	}

	@Test
	public void testThatInheritedValidatorClassesAreHandledCorrectly() {

		File sourceFile1 = compilerHelper.getSourceFile( FieldLevelValidationUsingInheritedValidator.class );
		File sourceFile2 = compilerHelper.getSourceFile( CustomConstraint.class );
		File sourceFile3 = compilerHelper.getSourceFile( AbstractCustomConstraintValidator.class );
		File sourceFile4 = compilerHelper.getSourceFile( CustomConstraintValidator.class );

		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(),
						diagnostics,
						sourceFile1,
						sourceFile2,
						sourceFile3,
						sourceFile4
				);

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch( diagnostics, new DiagnosticExpectation( Kind.ERROR, 19 ) );
	}

	@Test
	public void methodLevelValidationUsingBuiltInConstraints() {

		File sourceFile = compilerHelper.getSourceFile( MethodLevelValidationUsingBuiltInConstraints.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, false, false, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 21 ),
				new DiagnosticExpectation( Kind.ERROR, 28 ),
				new DiagnosticExpectation( Kind.ERROR, 36 ),
				new DiagnosticExpectation( Kind.ERROR, 43 ),
				new DiagnosticExpectation( Kind.ERROR, 51 ),
				new DiagnosticExpectation( Kind.ERROR, 59 ),
				new DiagnosticExpectation( Kind.ERROR, 67 ),
				new DiagnosticExpectation( Kind.ERROR, 74 )
		);
	}

	/**
	 * Constraints are allowed at non-getters per processor option, but all other
	 * checks (no static methods allowed etc.) still apply.
	 */
	@Test
	public void methodLevelConstraintsAllowedAtNonGetterMethods() {

		File sourceFile = compilerHelper.getSourceFile( MethodLevelValidationUsingBuiltInConstraints.class );

		//compile with -AmethodConstraintsSupported
		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, false, true, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 21 ),
				new DiagnosticExpectation( Kind.ERROR, 28 ),
				new DiagnosticExpectation( Kind.ERROR, 36 ),
				new DiagnosticExpectation( Kind.ERROR, 43 ),
				new DiagnosticExpectation( Kind.ERROR, 59 ),
				new DiagnosticExpectation( Kind.ERROR, 67 ),
				new DiagnosticExpectation( Kind.ERROR, 74 )
		);
	}

	@Test
	public void classLevelValidation() {

		File sourceFile1 = compilerHelper.getSourceFile( ClassLevelValidation.class );
		File sourceFile2 = compilerHelper.getSourceFile( ValidCustomer.class );
		File sourceFile3 = compilerHelper.getSourceFile( ValidCustomerValidator.class );

		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(), diagnostics, sourceFile1, sourceFile2, sourceFile3
				);

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch( diagnostics, new DiagnosticExpectation( Kind.ERROR, 17 ) );
	}

	@Test
	public void validationUsingComposedConstraint() {

		File sourceFile1 = compilerHelper.getSourceFile( FieldLevelValidationUsingComposedConstraint.class );
		File sourceFile2 = compilerHelper.getSourceFile( ValidOrderNumber.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile1, sourceFile2 );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch( diagnostics, new DiagnosticExpectation( Kind.ERROR, 18 ) );
	}

	@Test
	public void validationUsingComplexComposedConstraint() {

		File sourceFile1 = compilerHelper.getSourceFile( FieldLevelValidationUsingComplexComposedConstraint.class );
		File sourceFile2 = compilerHelper.getSourceFile( ComposedConstraint.class );
		File sourceFile3 = compilerHelper.getSourceFile( ComposingConstraint1.class );
		File sourceFile4 = compilerHelper.getSourceFile( ComposingConstraint1ValidatorForString.class );
		File sourceFile5 = compilerHelper.getSourceFile( ComposingConstraint1ValidatorForGregorianCalendar.class );
		File sourceFile6 = compilerHelper.getSourceFile( ComposingConstraint1ValidatorForList.class );
		File sourceFile7 = compilerHelper.getSourceFile( ComposingConstraint2.class );
		File sourceFile8 = compilerHelper.getSourceFile( ComposingConstraint2ValidatorForArrayList.class );
		File sourceFile9 = compilerHelper.getSourceFile( ComposingConstraint2ValidatorForCalendar.class );
		File sourceFile10 = compilerHelper.getSourceFile( ComposingConstraint2ValidatorForCollection.class );

		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(),
						diagnostics,
						sourceFile1,
						sourceFile2,
						sourceFile3,
						sourceFile4,
						sourceFile5,
						sourceFile6,
						sourceFile7,
						sourceFile8,
						sourceFile9,
						sourceFile10
				);

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 18 ),
				new DiagnosticExpectation( Kind.ERROR, 30 ),
				new DiagnosticExpectation( Kind.ERROR, 39 ),
				new DiagnosticExpectation( Kind.ERROR, 45 )
		);
	}

	@Test
	public void validationUsingBoxing() {

		File sourceFile1 = compilerHelper.getSourceFile( ValidationUsingBoxing.class );
		File sourceFile2 = compilerHelper.getSourceFile( ValidLong.class );
		File sourceFile3 = compilerHelper.getSourceFile( ValidLongValidator.class );

		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(), diagnostics, sourceFile1, sourceFile2, sourceFile3
				);

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 20 ),
				new DiagnosticExpectation( Kind.ERROR, 26 ),
				new DiagnosticExpectation( Kind.ERROR, 32 ),
				new DiagnosticExpectation( Kind.ERROR, 48 ),
				new DiagnosticExpectation( Kind.ERROR, 56 )
		);
	}

	@Test
	public void testThatNonUniqueValidatorResolutionCausesCompilationError() {

		File sourceFile1 = compilerHelper.getSourceFile( NoUniqueValidatorResolution.class );
		File sourceFile2 = compilerHelper.getSourceFile( Size.class );
		File sourceFile3 = compilerHelper.getSourceFile( SizeValidatorForCollection.class );
		File sourceFile4 = compilerHelper.getSourceFile( SizeValidatorForSerializable.class );
		File sourceFile5 = compilerHelper.getSourceFile( SerializableCollection.class );
		File sourceFile6 = compilerHelper.getSourceFile( SizeValidatorForSet.class );

		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(),
						diagnostics,
						sourceFile1,
						sourceFile2,
						sourceFile3,
						sourceFile4,
						sourceFile5,
						sourceFile6
				);

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 22 )
		);
	}

	@Test
	public void testThatMultiValuedConstrainedIsOnlyGivenAtSupportedType() {

		File sourceFile1 = compilerHelper.getSourceFile( MultipleConstraintsOfSameType.class );

		boolean compilationResult = compilerHelper.compile(
				new ConstraintValidationProcessor(), diagnostics, sourceFile1
		);

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 22 ),
				new DiagnosticExpectation( Kind.ERROR, 22 )
		);
	}

	@Test
	public void testThatAtValidAnnotationGivenAtNotSupportedTypesCausesCompilationErrors() {

		File sourceFile1 = compilerHelper.getSourceFile( ValidationUsingAtValidAnnotation.class );

		boolean compilationResult = compilerHelper.compile(
				new ConstraintValidationProcessor(), diagnostics, false, false, sourceFile1
		);

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 23 ),
				new DiagnosticExpectation( Kind.ERROR, 29 ),
				new DiagnosticExpectation( Kind.ERROR, 45 ),
				new DiagnosticExpectation( Kind.ERROR, 53 ),
				new DiagnosticExpectation( Kind.ERROR, 61 ),
				new DiagnosticExpectation( Kind.ERROR, 69 ),
				new DiagnosticExpectation( Kind.ERROR, 77 )
		);
	}

	@Test
	public void groupSequenceProvider() {
		File sourceFile1 = compilerHelper.getSourceFile( GroupSequenceProviderDefinition.class );
		File sourceFile2 = compilerHelper.getSourceFile( BazDefaultGroupSequenceProvider.class );
		File sourceFile3 = compilerHelper.getSourceFile( FooDefaultGroupSequenceProvider.class );
		File sourceFile4 = compilerHelper.getSourceFile( QuxDefaultGroupSequenceProvider.class );
		File sourceFile5 = compilerHelper.getSourceFile( SampleDefaultGroupSequenceProvider.class );
		File sourceFile6 = compilerHelper.getSourceFile( FooBarDefaultGroupSequenceProvider.class );
		File sourceFile7 = compilerHelper.getSourceFile( FooBarBazDefaultGroupSequenceProvider.class );

		boolean compilationResult = compilerHelper.compile(
				new ConstraintValidationProcessor(),
				diagnostics,
				sourceFile1,
				sourceFile2,
				sourceFile3,
				sourceFile4,
				sourceFile5,
				sourceFile6,
				sourceFile7
		);

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 25 ),
				new DiagnosticExpectation( Kind.ERROR, 34 ),
				new DiagnosticExpectation( Kind.ERROR, 42 ),
				new DiagnosticExpectation( Kind.ERROR, 50 ),
				new DiagnosticExpectation( Kind.ERROR, 58 ),
				new DiagnosticExpectation( Kind.ERROR, 66 )
		);
	}

	/**
	 * HV-418: No error shall be raised, when @Past/@Future are given at Joda
	 * date/time types.
	 */
	@Test()
	public void timeConstraintsAllowedAtJodaTypes() {

		File sourceFile1 = compilerHelper.getSourceFile( ModelWithJodaTypes.class );

		boolean compilationResult = compilerHelper.compile(
				new ConstraintValidationProcessor(), diagnostics, sourceFile1
		);

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 64 ),
				new DiagnosticExpectation( Kind.ERROR, 65 ),
				new DiagnosticExpectation( Kind.ERROR, 66 ),
				new DiagnosticExpectation( Kind.ERROR, 67 )
		);
	}

	@Test
	public void timeConstraintsAllowedAtJava8DateTime() {

		File sourceFile = compilerHelper.getSourceFile( ModelWithJava8DateTime.class );

		boolean compilationResult = compilerHelper.compile(
				new ConstraintValidationProcessor(), diagnostics, sourceFile
		);

		assertTrue( compilationResult, "Java 8 date/time API types fails at @Future/@Past/@FutureOrPresent/@PastOrPresent." );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1252")
	public void constraintsAllowedAtJavaMoneyTypes() {
		File sourceFile = compilerHelper.getSourceFile( ModelWithJavaMoneyTypes.class );

		EnumSet<Library> libraries = EnumSet.of(
				Library.VALIDATION_API,
				Library.HIBERNATE_VALIDATOR,
				Library.JAVA_MONEY_API
		);
		boolean compilationResult = compilerHelper.compile(
				new ConstraintValidationProcessor(), diagnostics, libraries, sourceFile
		);

		assertTrue( compilationResult, "Java Money API types (MonetaryAmount) fails with constraints annotations." );
	}

	@Test
	public void crossParameterConstraintsAllowed() {

		File[] sourceFiles = new File[] {
				compilerHelper.getSourceFile( ValidCrossParameterAndNormalConstraint.class ),
				compilerHelper.getSourceFile( ValidCrossParameterConstraint.class ),
				compilerHelper.getSourceFile( MethodLevelValidationUsingCrossParameterConstraints.class ),
				compilerHelper.getSourceFile( GenericCrossParameterValidatorObjectArray.class ),
				compilerHelper.getSourceFile( GenericCrossParameterValidator.class ),
				compilerHelper.getSourceFile( GenericNormalValidator.class )
		};

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, false, true, sourceFiles );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 41 ),
				new DiagnosticExpectation( Kind.ERROR, 63 )
		);

	}

	@Test
	@TestForIssue(jiraKey = "HV-1395")
	public void unwrappingConstraints() {
		File sourceFile = compilerHelper.getSourceFile( UnwrappingConstraints.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 33 ),
				new DiagnosticExpectation( Kind.ERROR, 36 ),
				new DiagnosticExpectation( Kind.ERROR, 39 ),
				new DiagnosticExpectation( Kind.ERROR, 42 ),
				new DiagnosticExpectation( Kind.ERROR, 45 ),
				new DiagnosticExpectation( Kind.ERROR, 48 ),
				new DiagnosticExpectation( Kind.WARNING, 54 )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1466")
	public void uniqueElementsConstraints() {
		File[] sourceFiles = new File[] {
				compilerHelper.getSourceFile( ModelWithUniqueElementsConstraints.class )
		};

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, false, true, sourceFiles );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 26 )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1530")
	public void codePointLengthConstraints() {
		File[] sourceFiles = new File[] {
				compilerHelper.getSourceFile( ModelWithCodePointLengthConstraints.class )
		};

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, false, true, sourceFiles );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 17 ),
				new DiagnosticExpectation( Kind.ERROR, 20 ),
				new DiagnosticExpectation( Kind.ERROR, 23 )
		);
	}

	@Test
	public void isbnConstraints() {
		File[] sourceFiles = new File[] {
				compilerHelper.getSourceFile( ModelWithISBNConstraints.class )
		};

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, false, true, sourceFiles );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 22 )
		);
	}
}
