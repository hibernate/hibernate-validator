/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap;

import java.util.Collection;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import org.hibernate.validator.ap.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.classchecks.ClassCheck;
import org.hibernate.validator.ap.classchecks.ClassCheckFactory;
import org.hibernate.validator.ap.util.CollectionHelper;
import org.hibernate.validator.ap.util.Configuration;
import org.hibernate.validator.ap.util.MessagerAdapter;

/**
 * An {@link javax.lang.model.element.ElementVisitor} that visits methods of classes and interfaces and applies
 * different checks to them. Each {@link ConstraintCheckIssue} occurred will be reported using the
 * {@link javax.annotation.processing.Messager} API.
 *
 * @author Marko Bekhta
 */
public class ClassVisitor extends AbstractElementVisitor<Void, Void> {

	private final ClassCheckFactory factory;

	private final Elements elementUtils;

	public ClassVisitor(
			ProcessingEnvironment processingEnvironment,
			MessagerAdapter messager,
			Configuration configuration) {
		super( messager, configuration );
		this.elementUtils = processingEnvironment.getElementUtils();

		this.factory = ClassCheckFactory.getInstance(
				processingEnvironment.getTypeUtils(),
				processingEnvironment.getElementUtils()
		);
	}

	/**
	 * Doesn't perform any checks at the moment but calls a visit methods on its own elements.
	 *
	 * @param element a class element to check
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
	 */
	@Override
	public Void visitTypeAsInterface(TypeElement element, Void aVoid) {
		visitAllMyElements( element );

		return null;
	}

	/**
	 * Checks whether the constraints of the given method are valid.
	 *
	 * @param element a method under investigation
	 * @param aVoid
	 */
	@Override
	public Void visitExecutableAsMethod(ExecutableElement element, Void aVoid) {
		processClassChecks( element );

		return null;
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

	private void processClassChecks(Element element) {
		try {
			Set<ConstraintCheckIssue> allIssues = CollectionHelper.newHashSet();
			Collection<ClassCheck> classChecks = factory.getClassChecks( element );
			for ( ClassCheck classCheck : classChecks ) {
				allIssues.addAll( classCheck.execute( element ) );
			}
			reportIssues( allIssues );
		}
		//HV-293: if single constraints can't be properly checked, report this and
		//proceed with next constraints
		catch (Exception e) {
			if ( verbose ) {
				messager.getDelegate().printMessage(
						Diagnostic.Kind.NOTE,
						e.getMessage() != null ? e.getMessage() : e.toString(),
						element
				);
			}
		}
	}

}
