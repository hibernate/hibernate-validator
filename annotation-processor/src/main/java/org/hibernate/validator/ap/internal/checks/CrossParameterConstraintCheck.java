/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.checks;

import java.util.Collections;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementKindVisitor8;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import javax.lang.model.util.TypeKindVisitor8;
import javax.lang.model.util.Types;

import org.hibernate.validator.ap.internal.util.AnnotationApiHelper;
import org.hibernate.validator.ap.internal.util.CollectionHelper;
import org.hibernate.validator.ap.internal.util.ConstraintHelper;
import org.hibernate.validator.ap.internal.util.ConstraintHelper.AnnotationProcessorValidationTarget;
import org.hibernate.validator.ap.internal.util.ConstraintHelper.ConstraintCheckResult;
import org.hibernate.validator.ap.internal.util.TypeNames.BeanValidationTypes;

/**
 * Checks that a cross-parameter constraint is defined correctly with reference to the specifications.
 * <ul>
 * <li>It must have at most one cross-parameter validator.</li>
 * <li>The cross-parameter validator must resolve to Object or Object[].</li>
 * <li>If the constraint is both normal and cross-parameter, it must define a 'validationAppliesTo()' attribute.</li>
 * <li>The 'validationAppliesTo' method, if any, must return a {@code ConstraintTarget}.</li>
 * <li>The 'validationAppliesTo' method, if any, must declare {@code ConstraintTarget#IMPLICIT} as default return value.</li>
 * </ul>
 *
 * @author Nicola Ferraro
 */
public class CrossParameterConstraintCheck extends AbstractConstraintCheck {

	private final AnnotationApiHelper annotationApiHelper;

	private final ConstraintHelper constraintHelper;

	private final Types typeUtils;

	public CrossParameterConstraintCheck(AnnotationApiHelper annotationApiHelper, ConstraintHelper constraintHelper, Types typeUtils) {
		this.annotationApiHelper = annotationApiHelper;
		this.constraintHelper = constraintHelper;
		this.typeUtils = typeUtils;
	}

	@Override
	public Set<ConstraintCheckIssue> checkAnnotationType(TypeElement element, AnnotationMirror annotation) {

		// this check applies to constraint annotations
		if ( !constraintHelper.isConstraintAnnotation( element ) ) {
			return Collections.emptySet();
		}

		DeclaredType elementType = element.asType().accept( new TypeKindVisitor8<DeclaredType, Void>() {

			@Override
			public DeclaredType visitDeclared(DeclaredType t, Void p) {
				return t;
			}
		}, null );

		Set<AnnotationProcessorValidationTarget> targets = constraintHelper.getSupportedValidationTargets( elementType );
		if ( !targets.contains( AnnotationProcessorValidationTarget.PARAMETERS ) ) {
			return Collections.emptySet();
		}

		// Check cross parameter validators
		ConstraintCheckResult res = constraintHelper.checkCrossParameterTypes( elementType );
		if ( res == ConstraintCheckResult.MULTIPLE_VALIDATORS_FOUND ) {
			return CollectionHelper.asSet(
					ConstraintCheckIssue.error(
							element,
							annotation,
							"CROSS_PARAMETER_CONSTRAINT_MULTIPLE_VALIDATORS",
							element.getSimpleName().toString() ) );
		}
		else if ( res == ConstraintCheckResult.DISALLOWED ) {
			return CollectionHelper.asSet(
					ConstraintCheckIssue.error(
							element,
							annotation,
							"CROSS_PARAMETER_CONSTRAINT_VALIDATOR_HAS_INVALID_TYPE",
							element.getSimpleName() ) );
		}

		// Check validationAppliesTo method
		ExecutableElement validationAppliesTo = getValidationAppliesToMethod( element );

		if ( validationAppliesTo == null && targets.size() > 1 ) {
			// validationAppliesTo is required to let the user specify the constraint target
			return CollectionHelper.asSet(
					ConstraintCheckIssue.error(
							element,
							annotation,
							"CROSS_PARAMETER_VALIDATION_APPLIES_TO_REQUIRED",
							element.getSimpleName() ) );
		}

		if ( validationAppliesTo != null ) {

			if ( !checkValidationAppliesToReturnType( validationAppliesTo ) ) {
				return CollectionHelper.asSet(
						ConstraintCheckIssue.error(
								element,
								annotation,
								"CROSS_PARAMETER_VALIDATION_APPLIES_TO_MUST_HAVE_CONSTRAINT_TARGET_RETURN_TYPE",
								element.getSimpleName() ) );
			}
			else if ( !checkValidationAppliesToDefaultValue( validationAppliesTo ) ) {
				return CollectionHelper.asSet(
						ConstraintCheckIssue.error(
								element,
								annotation,
								"CROSS_PARAMETER_VALIDATION_APPLIES_TO_MUST_HAVE_IMPLICIT_DEFAULT_VALUE",
								element.getSimpleName() ) );
			}

		}

		return Collections.emptySet();
	}

	private boolean checkValidationAppliesToReturnType(ExecutableElement validationAppliesToMethod) {

		final DeclaredType constraintTargetType = annotationApiHelper.getDeclaredTypeByName( BeanValidationTypes.CONSTRAINT_TARGET );

		// Check the return type
		return validationAppliesToMethod.getReturnType().accept( new TypeKindVisitor8<Boolean, Void>() {

			@Override
			public Boolean visitDeclared(DeclaredType t, Void p) {
				if ( typeUtils.isSameType( constraintTargetType, t ) ) {
					return true;
				}

				return false;
			}
		}, null );
	}

	private boolean checkValidationAppliesToDefaultValue(ExecutableElement validationAppliesToMethod) {

		final DeclaredType constraintTargetType = annotationApiHelper.getDeclaredTypeByName( BeanValidationTypes.CONSTRAINT_TARGET );

		// Check the return type
		return validationAppliesToMethod.getDefaultValue().accept( new SimpleAnnotationValueVisitor8<Boolean, Void>() {

			@Override
			public Boolean visitEnumConstant(VariableElement c, Void p) {
				if ( typeUtils.isSameType( constraintTargetType, c.asType() ) ) {
					return c.getSimpleName().contentEquals( "IMPLICIT" );
				}
				return false;
			}
		}, null );
	}

	private ExecutableElement getValidationAppliesToMethod(Element annotation) {
		for ( Element e : annotation.getEnclosedElements() ) {
			ExecutableElement method = e.accept( new ElementKindVisitor8<ExecutableElement, Void>() {

				@Override
				public ExecutableElement visitExecutableAsMethod(ExecutableElement e, Void p) {
					if ( e.getSimpleName().contentEquals( "validationAppliesTo" ) ) {
						return e;
					}
					return null;
				}
			}, null );

			if ( method != null ) {
				return method;
			}
		}
		return null;
	}

}
