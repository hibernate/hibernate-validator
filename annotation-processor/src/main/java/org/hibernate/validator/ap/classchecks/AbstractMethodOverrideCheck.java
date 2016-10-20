/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.classchecks;


import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.hibernate.validator.ap.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.util.CollectionHelper;

/**
 * Abstract base class for {@link ClassCheck} implementations that check overridden methods.
 *
 * @author Marko Bekhta
 */
public abstract class AbstractMethodOverrideCheck extends AbstractClassCheck {

	private static final String JAVA_LANG_OBJECT = "java.lang.Object";

	private static final String JAVAX_VALIDATION_CONSTRAINT = "javax.validation.Constraint";

	private final Elements elementUtils;

	private final Types typeUtils;

	public AbstractMethodOverrideCheck(Elements elementUtils, Types typeUtils) {
		this.elementUtils = elementUtils;
		this.typeUtils = typeUtils;
	}

	@Override
	public Collection<ConstraintCheckIssue> checkMethod(ExecutableElement currentMethod) {
		if ( !needToPerformAnyChecks( currentMethod ) ) {
			return Collections.emptySet();
		}

		TypeElement currentTypeElement = getEnclosingTypeElement( currentMethod );

		// find if there's a method that was overridden by the current one.
		List<ExecutableElement> overriddenMethods = findAllOverriddenElements( currentTypeElement, currentMethod );
		if ( overriddenMethods.isEmpty() ) {
			return Collections.emptySet();
		}

		// if there's more than one overridden method we need to make sure that all of them match
		Set<ConstraintCheckIssue> errors = CollectionHelper.newHashSet();
		for ( ExecutableElement firstExecutable : overriddenMethods ) {
			for ( ExecutableElement secondExecutable : overriddenMethods ) {
				if ( firstExecutable.equals( secondExecutable ) ) {
					continue;
				}
				if ( !checkOverriddenMethod( firstExecutable, secondExecutable ) ) {
					errors.add( ConstraintCheckIssue.error( currentMethod, null, getErrorMessageKey(), getEnclosingTypeElement( secondExecutable ).getQualifiedName().toString() ) );
				}
			}
		}

		if ( !errors.isEmpty() ) {
			return errors;
		}

		// if we reached this part of code it means we need to check if the current method 'correctly' overrides super methods.
		for ( ExecutableElement overriddenMethod : overriddenMethods ) {
			if ( !checkOverriddenMethod( currentMethod, overriddenMethod ) ) {
				return CollectionHelper.asSet( ConstraintCheckIssue.error( currentMethod, null, getErrorMessageKey(), getEnclosingTypeElement( overriddenMethod ).getQualifiedName().toString() ) );

			}
		}

		return Collections.emptySet();
	}

	/**
	 * There can be situations in which no checks should be performed. So in such cases we will not look for any overridden
	 * methods and do any work at all.
	 *
	 * @param currentMethod the method under investigation
	 *
	 * @return {@code true} if we should proceed with checks and {@code false} otherwise
	 */
	protected abstract boolean needToPerformAnyChecks(ExecutableElement currentMethod);

	/**
	 * Determine if one method 'correctly' overrides another one in terms of annotated parameters
	 *
	 * @param currentMethod method from a current subclass
	 * @param overriddenMethod method from a super class
	 *
	 * @return {@code true} if method is overridden 'correctly', {@code false} otherwise
	 */
	protected abstract boolean checkOverriddenMethod(ExecutableElement currentMethod, ExecutableElement overriddenMethod);

	/**
	 * Method which returns a Error message key to be used if there's an error in method overriding.
	 *
	 * @return error message key
	 */
	protected abstract String getErrorMessageKey();


	/**
	 * Find a list of overridden methods from all super classes and all implemented interfaces.
	 *
	 * @param currentTypeElement the class in which the method is located
	 * @param currentMethod the method for which we want to find the overridden methods
	 *
	 * @return a list of overridden methods if there are any and an empty list otherwise
	 */
	private List<ExecutableElement> findAllOverriddenElements(
			TypeElement currentTypeElement,
			ExecutableElement currentMethod) {
		List<ExecutableElement> elements = CollectionHelper.newArrayList();

		// get a super class
		TypeElement parentTypeElement = (TypeElement) typeUtils.asElement( currentTypeElement.getSuperclass() );

		// look for implemented interfaces:
		elements.addAll( findOverriddenMethodInInterfaces(
				currentTypeElement,
				currentTypeElement.getInterfaces(),
				currentMethod
		) );

		// if super class is java.lang.Object, then there's no need to do any other work: no such method was found.
		while ( !JAVA_LANG_OBJECT.equals( parentTypeElement.toString() ) ) {
			ExecutableElement element = getOverriddenElement( currentTypeElement, parentTypeElement, currentMethod );
			if ( element != null ) {
				elements.add( element );
			}

			// need to check all the implemented interfaces as well
			elements.addAll( findOverriddenMethodInInterfaces( currentTypeElement, parentTypeElement.getInterfaces(), currentMethod ) );

			parentTypeElement = (TypeElement) typeUtils.asElement( parentTypeElement.getSuperclass() );
		}

		return elements;
	}

	/**
	 * Find a list of overridden methods from implemented interfaces.
	 *
	 * @param currentTypeElement the class in which the method is located
	 * @param interfaces a list of implemented interfaces
	 * @param currentMethod the method for which we want to find the overridden methods
	 *
	 * @return a list of overridden methods if there are any, an empty list otherwise
	 */
	private List<ExecutableElement> findOverriddenMethodInInterfaces(
			TypeElement currentTypeElement,
			List<? extends TypeMirror> interfaces,
			ExecutableElement currentMethod) {
		List<ExecutableElement> elements = CollectionHelper.newArrayList();

		for ( TypeMirror anInterface : interfaces ) {
			ExecutableElement element = getOverriddenElement( currentTypeElement, (TypeElement) typeUtils.asElement( anInterface ), currentMethod );
			if ( element != null ) {
				elements.add( element );
			}
		}

		return elements;
	}

	/**
	 * Find a method that is overridden by the one passed to this function.
	 *
	 * @param currentTypeElement a class in which method is located
	 * @param otherTypeElement a class/interface on which to look for overridden method
	 * @param currentMethod the method for which we want to find the overridden methods
	 *
	 * @return an overridden method if there's one, and {@code null} otherwise
	 */
	private ExecutableElement getOverriddenElement(
			TypeElement currentTypeElement,
			TypeElement otherTypeElement,
			ExecutableElement currentMethod) {

		if ( JAVA_LANG_OBJECT.equals( otherTypeElement.toString() ) ) {
			return null;
		}

		for ( Element element : elementUtils.getAllMembers( otherTypeElement ) ) {
			if ( !element.getKind().equals( ElementKind.METHOD ) ) {
				continue;
			}
			if ( elementUtils.overrides( currentMethod, (ExecutableElement) element, currentTypeElement ) ) {
				return (ExecutableElement) element;
			}
		}

		return null;
	}

	/**
	 * Find a {@link TypeElement} that enclose a given {@link ExecutableElement}.
	 *
	 * @param currentMethod a method that you want to find class/interface it belongs to
	 *
	 * @return a class/interface represented by {@link TypeElement} to which a method belongs to
	 */
	private TypeElement getEnclosingTypeElement(ExecutableElement currentMethod) {
		return (TypeElement) typeUtils.asElement( currentMethod.getEnclosingElement().asType() );
	}

	/**
	 * Returns a list containing those annotation mirrors from the input list,
	 * which are constraint annotations and filter out others. The input collection
	 * remains untouched.
	 *
	 * @param collectionToFilter a list to be filtered
	 *
	 * @return a filtered list with constraint annotations only
	 */
	protected List<? extends AnnotationMirror> listOnlyConstraintAnnotations(List<? extends AnnotationMirror> collectionToFilter) {
		if ( collectionToFilter.isEmpty() ) {
			return Collections.emptyList();
		}
		List<AnnotationMirror> result = CollectionHelper.newArrayList();

		for ( AnnotationMirror annotationMirror : collectionToFilter ) {
			if ( isConstraintAnnotation( annotationMirror ) ) {
				result.add( annotationMirror );
			}
		}

		return result;
	}

	/**
	 * Checks if annotation is a constraint annotation or not.
	 *
	 * @param annotationMirror an annotation to check
	 *
	 * @return {@code true} if annotation is a constraint annotation, {@code false} otherwise
	 */
	private boolean isConstraintAnnotation(AnnotationMirror annotationMirror) {
		for ( AnnotationMirror mirror : annotationMirror.getAnnotationType().asElement().getAnnotationMirrors() ) {
			if ( JAVAX_VALIDATION_CONSTRAINT.equals( mirror.getAnnotationType().toString() ) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if one list of annotation mirrors contains same kind of annotation as the other.
	 *
	 * @param listToCheck list that should contain annotations from the other one
	 * @param listShouldContain list of annotation that should be present in the first one
	 *
	 * @return {@code true} if all annotations from the second list are present in the first one,
	 * {@code false} otherwise
	 */
	protected boolean annotationMirrorContainsAll(List<? extends AnnotationMirror> listToCheck, List<? extends AnnotationMirror> listShouldContain) {
		Set<String> namesOfAnnotations = CollectionHelper.newHashSet();
		for ( AnnotationMirror annotationMirror : listToCheck ) {
			namesOfAnnotations.add( annotationMirror.getAnnotationType().toString() );
		}
		for ( AnnotationMirror annotationMirror : listShouldContain ) {
			if ( !namesOfAnnotations.contains( annotationMirror.getAnnotationType().toString() ) ) {
				return false;
			}
		}
		return true;
	}

}
