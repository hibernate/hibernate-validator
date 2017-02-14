/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import org.hibernate.validator.ap.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.testmodel.annotationparameters.TypeAnnotationParameters;
import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.CollectionHelper;
import org.hibernate.validator.ap.util.Configuration;
import org.hibernate.validator.ap.util.ConstraintHelper;
import org.hibernate.validator.ap.util.DiagnosticExpectation;
import org.hibernate.validator.ap.util.MessagerAdapter;
import org.hibernate.validator.ap.util.TypeNames;

import org.testng.annotations.Test;

import static org.hibernate.validator.ap.testutil.CompilerTestHelper.assertThatDiagnosticsMatch;
import static org.testng.AssertJUnit.assertFalse;

/**
 * @author Marko Bekhta
 */
public class TypeAnnotationValidationTest extends ConstraintValidationProcessorTestBase {

	@Test
	public void testValidGroupSequenceParameter() {
		File sourceFile = compilerHelper.getSourceFile( TypeAnnotationParameters.class );

		boolean compilationResult =
				compilerHelper.compile( new TypeArgumentValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch( diagnostics,
				new DiagnosticExpectation( Diagnostic.Kind.ERROR, 24 )
		);
	}

	@SupportedAnnotationTypes("*")
	@SupportedSourceVersion(SourceVersion.RELEASE_8)
	@SupportedOptions({
			Configuration.DIAGNOSTIC_KIND_PROCESSOR_OPTION,
			Configuration.VERBOSE_PROCESSOR_OPTION,
			Configuration.METHOD_CONSTRAINTS_SUPPORTED_PROCESSOR_OPTION
	})
	public class TypeArgumentValidationProcessor extends AbstractProcessor {

		/**
		 * The messager to be used for error reports.
		 */
		private MessagerAdapter messager;

		/**
		 * Provides access to this processor's configuration options.
		 */
		private Configuration configuration;

		@Override
		public synchronized void init(ProcessingEnvironment processingEnv) {

			super.init( processingEnv );

			configuration = new Configuration( processingEnv.getOptions(), processingEnv.getMessager() );
			messager = new MessagerAdapter( processingEnv.getMessager(), configuration.getDiagnosticKind() );
		}

		@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

			ElementVisitor<Void, Void> visitor = new TypeArgumentVisitor( processingEnv, messager, configuration );

			for ( Element element : roundEnv.getRootElements() ) {
				visitor.visit( element );
			}

			return false;
		}
	}

	/**
	 * Simple example of implementation for type arguments annotations
	 */
	public static class TypeArgumentVisitor extends AbstractElementVisitor<Void, Void> {

		private final Elements elementUtils;

		private final Types typeUtils;

		private final ConstraintHelper constraintHelper;

		private final AnnotationApiHelper annotationApiHelper;

		public TypeArgumentVisitor(
				ProcessingEnvironment processingEnvironment,
				MessagerAdapter messager,
				Configuration configuration) {
			super( messager, configuration );
			this.elementUtils = processingEnvironment.getElementUtils();
			this.typeUtils = processingEnvironment.getTypeUtils();
			this.annotationApiHelper = new AnnotationApiHelper( this.elementUtils, this.typeUtils );
			this.constraintHelper = new ConstraintHelper( this.typeUtils, annotationApiHelper );

		}

		/**
		 * Doesn't perform any checks at the moment but calls a visit methods on its own elements.
		 *
		 * @param element a class element to check
		 * @param aVoid
		 */
		@Override
		public Void visitTypeAsClass(TypeElement element, Void aVoid) {
			visitAllMyElements( element );

			return null;
		}

		/**
		 * Doesn't perform any checks at the moment but calls a visit methods on its own elements.
		 *
		 * @param element a class element to check
		 * @param aVoid
		 */
		@Override
		public Void visitTypeAsInterface(TypeElement element, Void aVoid) {
			visitAllMyElements( element );

			return null;
		}

		/**
		 * Visits variable and checks if it has type argument annotations and if so - checks if the variable is annotated with @Valid
		 *
		 * @param variableElement an element to check
		 */
		@Override
		public Void visitVariable(VariableElement variableElement, Void aVoid) {

			List<AnnotationMirror> annotationMirrorList = TypeArgumentUtils.getTypeArgumentAnnotations( variableElement );

			boolean hasConstraint = false;
			for ( AnnotationMirror annotationMirror : annotationMirrorList ) {
				if ( constraintHelper.isConstraintAnnotation( typeUtils.asElement( annotationMirror.getAnnotationType() ) ) ) {
					hasConstraint = true;
				}
			}
			if ( hasConstraint ) {
				//need to check if there's a @Valid annotation present or not
				if ( annotationApiHelper.getMirror( variableElement.getAnnotationMirrors(), TypeNames.BeanValidationTypes.VALID ) == null ) {
					messager.reportErrors(
							CollectionHelper.asSet( ConstraintCheckIssue.error( variableElement, null, "TYPE_ARGUMENT_ANNOTATION_MISSING_VALID" ) ) );
				}
			}

			return super.visitVariable( variableElement, aVoid );
		}

		/**
		 * Visits all inner elements of provided {@link TypeElement}.
		 *
		 * @param typeElement inner elements of which you want to visit
		 */
		private void visitAllMyElements(TypeElement typeElement) {
			for ( Element element : elementUtils.getAllMembers( typeElement ) ) {
				visit( element );
			}
		}

	}

	protected static class TypeArgumentUtils {

		private TypeArgumentUtils() {
		}

		public static List<AnnotationMirror> getTypeArgumentAnnotations(VariableElement variableElement) {
			//	Symbol.VarSymbol symbol = (Symbol.VarSymbol) variableElement;
			//	return new ArrayList<>( symbol.getMetadata().getTypeAttributes() );
			if ( variableElement.getClass().getName().startsWith( "com.sun.tools.javac.code.Symbol" ) ) {

				try {
					Object metadata = variableElement.getClass().getMethod( "getMetadata" ).invoke( variableElement );
					return (List<AnnotationMirror>) metadata.getClass().getMethod( "getTypeAttributes" ).invoke( metadata );
				}
				catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
					//TODO: log the warning that type argument annotations are not processed
				}
			}

			return Collections.emptyList();
		}
	}

}
