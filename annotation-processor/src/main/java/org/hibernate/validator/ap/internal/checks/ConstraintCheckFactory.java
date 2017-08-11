/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.checks;

import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.hibernate.validator.ap.internal.checks.annotationparameters.AnnotationDefaultMessageCheck;
import org.hibernate.validator.ap.internal.checks.annotationparameters.AnnotationParametersDecimalMinMaxCheck;
import org.hibernate.validator.ap.internal.checks.annotationparameters.AnnotationParametersDigitsCheck;
import org.hibernate.validator.ap.internal.checks.annotationparameters.AnnotationParametersGroupsCheck;
import org.hibernate.validator.ap.internal.checks.annotationparameters.AnnotationParametersPatternCheck;
import org.hibernate.validator.ap.internal.checks.annotationparameters.AnnotationParametersScriptAssertCheck;
import org.hibernate.validator.ap.internal.checks.annotationparameters.AnnotationParametersSizeLengthCheck;
import org.hibernate.validator.ap.internal.checks.annotationparameters.AnnotationPayloadUnwrappingCheck;
import org.hibernate.validator.ap.internal.checks.annotationparameters.AnnotationUserMessageCheck;
import org.hibernate.validator.ap.internal.checks.annotationparameters.GroupSequenceCheck;
import org.hibernate.validator.ap.internal.util.AnnotationApiHelper;
import org.hibernate.validator.ap.internal.util.CollectionHelper;
import org.hibernate.validator.ap.internal.util.ConstraintHelper;
import org.hibernate.validator.ap.internal.util.ConstraintHelper.AnnotationType;

/**
 * A factory in charge of determining the {@link ConstraintCheck}s required for
 * the validation of annotations at given elements.
 *
 * @author Gunnar Morling
 */
public class ConstraintCheckFactory {

	/**
	 * Holds the checks to be executed for field elements.
	 */
	private final Map<AnnotationType, ConstraintChecks> fieldChecks;

	/**
	 * Holds the checks to be executed for method parameter elements.
	 */
	private final Map<AnnotationType, ConstraintChecks> parameterChecks;

	/**
	 * Holds the checks to be executed for method elements.
	 */
	private final Map<AnnotationType, ConstraintChecks> methodChecks;

	/**
	 * Holds the checks to be executed for annotation type declarations.
	 */
	private final Map<AnnotationType, ConstraintChecks> annotationTypeChecks;

	/**
	 * Holds the checks to be executed for class/interface/enum declarations.
	 */
	private final Map<AnnotationType, ConstraintChecks> nonAnnotationTypeChecks;

	private final ConstraintHelper constraintHelper;

	private static final SingleValuedChecks NULL_CHECKS = new SingleValuedChecks();

	public ConstraintCheckFactory(Types typeUtils, Elements elementUtils, ConstraintHelper constraintHelper, AnnotationApiHelper annotationApiHelper, boolean methodConstraintsSupported) {
		this.constraintHelper = constraintHelper;

		parameterChecks = CollectionHelper.newHashMap();

		parameterChecks.put(
				AnnotationType.CONSTRAINT_ANNOTATION,
				new SingleValuedChecks(
						new AnnotationParametersSizeLengthCheck( annotationApiHelper ),
						new AnnotationParametersPatternCheck( annotationApiHelper ),
						new AnnotationParametersScriptAssertCheck( annotationApiHelper ),
						new AnnotationParametersDigitsCheck( annotationApiHelper ),
						new AnnotationParametersDecimalMinMaxCheck( annotationApiHelper ),
						new AnnotationParametersGroupsCheck( annotationApiHelper )
				)
		);
		parameterChecks.put(
				AnnotationType.MULTI_VALUED_CONSTRAINT_ANNOTATION,
				new MultiValuedChecks(
						constraintHelper,
						new AnnotationParametersSizeLengthCheck( annotationApiHelper ),
						new AnnotationParametersPatternCheck( annotationApiHelper ),
						new AnnotationParametersScriptAssertCheck( annotationApiHelper ),
						new AnnotationParametersDigitsCheck( annotationApiHelper ),
						new AnnotationParametersDecimalMinMaxCheck( annotationApiHelper ),
						new AnnotationParametersGroupsCheck( annotationApiHelper )
				)
		);
		parameterChecks.put(
				AnnotationType.GRAPH_VALIDATION_ANNOTATION, NULL_CHECKS
		);
		parameterChecks.put( AnnotationType.NO_CONSTRAINT_ANNOTATION, NULL_CHECKS );

		fieldChecks = CollectionHelper.newHashMap();
		fieldChecks.put(
				AnnotationType.CONSTRAINT_ANNOTATION,
				new SingleValuedChecks(
						new StaticCheck(),
						new TypeCheck( constraintHelper, typeUtils, annotationApiHelper ),
						new AnnotationParametersSizeLengthCheck( annotationApiHelper ),
						new AnnotationParametersPatternCheck( annotationApiHelper ),
						new AnnotationParametersScriptAssertCheck( annotationApiHelper ),
						new AnnotationParametersDigitsCheck( annotationApiHelper ),
						new AnnotationParametersDecimalMinMaxCheck( annotationApiHelper ),
						new AnnotationParametersGroupsCheck( annotationApiHelper ),
						new AnnotationUserMessageCheck( annotationApiHelper ),
						new AnnotationPayloadUnwrappingCheck( annotationApiHelper, typeUtils )
				)
		);
		fieldChecks.put(
				AnnotationType.MULTI_VALUED_CONSTRAINT_ANNOTATION,
				new MultiValuedChecks(
						constraintHelper,
						new StaticCheck(),
						new TypeCheck( constraintHelper, typeUtils, annotationApiHelper ),
						new AnnotationParametersSizeLengthCheck( annotationApiHelper ),
						new AnnotationParametersPatternCheck( annotationApiHelper ),
						new AnnotationParametersScriptAssertCheck( annotationApiHelper ),
						new AnnotationParametersDigitsCheck( annotationApiHelper ),
						new AnnotationParametersDecimalMinMaxCheck( annotationApiHelper ),
						new AnnotationParametersGroupsCheck( annotationApiHelper ),
						new AnnotationUserMessageCheck( annotationApiHelper ),
						new AnnotationPayloadUnwrappingCheck( annotationApiHelper, typeUtils )
				)
		);
		fieldChecks.put(
				AnnotationType.GRAPH_VALIDATION_ANNOTATION,
				new SingleValuedChecks( new StaticCheck(), new PrimitiveCheck() )
		);
		fieldChecks.put( AnnotationType.NO_CONSTRAINT_ANNOTATION, NULL_CHECKS );

		methodChecks = CollectionHelper.newHashMap();
		methodChecks.put(
				AnnotationType.CONSTRAINT_ANNOTATION,
				new SingleValuedChecks(
						new GetterCheck( methodConstraintsSupported ),
						new StaticCheck(),
						new MethodAnnotationCheck( constraintHelper ),
						new TypeCheck( constraintHelper, typeUtils, annotationApiHelper ),
						new AnnotationParametersSizeLengthCheck( annotationApiHelper ),
						new AnnotationParametersPatternCheck( annotationApiHelper ),
						new AnnotationParametersScriptAssertCheck( annotationApiHelper ),
						new AnnotationParametersDigitsCheck( annotationApiHelper ),
						new AnnotationParametersDecimalMinMaxCheck( annotationApiHelper ),
						new AnnotationParametersGroupsCheck( annotationApiHelper ),
						new AnnotationUserMessageCheck( annotationApiHelper ),
						new AnnotationPayloadUnwrappingCheck( annotationApiHelper, typeUtils )
				)
		);
		methodChecks.put(
				AnnotationType.MULTI_VALUED_CONSTRAINT_ANNOTATION,
				new MultiValuedChecks(
						constraintHelper,
						new GetterCheck( methodConstraintsSupported ),
						new StaticCheck(),
						new MethodAnnotationCheck( constraintHelper ),
						new TypeCheck( constraintHelper, typeUtils, annotationApiHelper ),
						new AnnotationParametersSizeLengthCheck( annotationApiHelper ),
						new AnnotationParametersPatternCheck( annotationApiHelper ),
						new AnnotationParametersScriptAssertCheck( annotationApiHelper ),
						new AnnotationParametersDigitsCheck( annotationApiHelper ),
						new AnnotationParametersDecimalMinMaxCheck( annotationApiHelper ),
						new AnnotationParametersGroupsCheck( annotationApiHelper ),
						new AnnotationUserMessageCheck( annotationApiHelper ),
						new AnnotationPayloadUnwrappingCheck( annotationApiHelper, typeUtils )
				)
		);
		methodChecks.put(
				AnnotationType.GRAPH_VALIDATION_ANNOTATION,
				new SingleValuedChecks( new GetterCheck( methodConstraintsSupported ), new StaticCheck(), new MethodAnnotationCheck( constraintHelper ),
						new PrimitiveCheck() )
		);
		methodChecks.put( AnnotationType.NO_CONSTRAINT_ANNOTATION, NULL_CHECKS );

		annotationTypeChecks = CollectionHelper.newHashMap();
		annotationTypeChecks.put(
				AnnotationType.CONSTRAINT_ANNOTATION,
				new SingleValuedChecks(
						new AnnotationTypeCheck( constraintHelper ),
						new AnnotationParametersSizeLengthCheck( annotationApiHelper ),
						new AnnotationParametersPatternCheck( annotationApiHelper ),
						new AnnotationParametersScriptAssertCheck( annotationApiHelper ),
						new AnnotationParametersDigitsCheck( annotationApiHelper ),
						new AnnotationParametersDecimalMinMaxCheck( annotationApiHelper ),
						new AnnotationParametersGroupsCheck( annotationApiHelper ),
						new AnnotationUserMessageCheck( annotationApiHelper ),
						new AnnotationPayloadUnwrappingCheck( annotationApiHelper, typeUtils )
				)
		);
		annotationTypeChecks.put(
				AnnotationType.MULTI_VALUED_CONSTRAINT_ANNOTATION,
				new MultiValuedChecks(
						constraintHelper,
						new AnnotationTypeCheck( constraintHelper ),
						new MixDirectAndListAnnotationCheck( constraintHelper ),
						new AnnotationParametersSizeLengthCheck( annotationApiHelper ),
						new AnnotationParametersPatternCheck( annotationApiHelper ),
						new AnnotationParametersScriptAssertCheck( annotationApiHelper ),
						new AnnotationParametersDigitsCheck( annotationApiHelper ),
						new AnnotationParametersDecimalMinMaxCheck( annotationApiHelper ),
						new AnnotationParametersGroupsCheck( annotationApiHelper )
				)
		);
		annotationTypeChecks.put(
				AnnotationType.CONSTRAINT_META_ANNOTATION,
				new SingleValuedChecks(
						new RetentionPolicyCheck( annotationApiHelper ),
						new TargetCheck( annotationApiHelper ),
						new ConstraintValidatorCheck( constraintHelper, annotationApiHelper ),
						new AnnotationTypeMemberCheck( annotationApiHelper, typeUtils ),
						new CrossParameterConstraintCheck( annotationApiHelper, constraintHelper, typeUtils ),
						new AnnotationDefaultMessageCheck( annotationApiHelper, elementUtils )
				)
		);
		annotationTypeChecks.put( AnnotationType.NO_CONSTRAINT_ANNOTATION, NULL_CHECKS );

		nonAnnotationTypeChecks = CollectionHelper.newHashMap();
		nonAnnotationTypeChecks.put(
				AnnotationType.CONSTRAINT_ANNOTATION,
				new SingleValuedChecks(
						new TypeCheck( constraintHelper, typeUtils, annotationApiHelper ),
						new AnnotationParametersSizeLengthCheck( annotationApiHelper ),
						new AnnotationParametersPatternCheck( annotationApiHelper ),
						new AnnotationParametersScriptAssertCheck( annotationApiHelper ),
						new AnnotationParametersDigitsCheck( annotationApiHelper ),
						new AnnotationParametersDecimalMinMaxCheck( annotationApiHelper ),
						new AnnotationParametersGroupsCheck( annotationApiHelper )
				)
		);
		nonAnnotationTypeChecks.put(
				AnnotationType.MULTI_VALUED_CONSTRAINT_ANNOTATION,
				new MultiValuedChecks(
						constraintHelper,
						new TypeCheck( constraintHelper, typeUtils, annotationApiHelper ),
						new AnnotationParametersSizeLengthCheck( annotationApiHelper ),
						new AnnotationParametersPatternCheck( annotationApiHelper ),
						new AnnotationParametersScriptAssertCheck( annotationApiHelper ),
						new AnnotationParametersDigitsCheck( annotationApiHelper ),
						new AnnotationParametersDecimalMinMaxCheck( annotationApiHelper ),
						new AnnotationParametersGroupsCheck( annotationApiHelper )
				)
		);
		nonAnnotationTypeChecks.put( AnnotationType.NO_CONSTRAINT_ANNOTATION, NULL_CHECKS );
		nonAnnotationTypeChecks.put(
				AnnotationType.GROUP_SEQUENCE_ANNOTATION,
				new SingleValuedChecks(
						new GroupSequenceCheck( annotationApiHelper, typeUtils, constraintHelper )
				)
		);
		nonAnnotationTypeChecks.put(
				AnnotationType.GROUP_SEQUENCE_PROVIDER_ANNOTATION,
				new SingleValuedChecks( new GroupSequenceProviderCheck( annotationApiHelper, typeUtils ) )
		);
	}

	/**
	 * Returns those checks that have to be performed to validate the given
	 * annotation at the given element. In case no checks have to be performed
	 * (e.g. because the given annotation is no constraint annotation) an empty
	 * {@link ConstraintChecks} instance will be returned. It's therefore always
	 * safe to operate on the returned object.
	 *
	 * @param annotatedElement An annotated element, e.g. a type declaration or a method.
	 * @param annotation An annotation.
	 *
	 * @return The checks to be performed to validate the given annotation at
	 *         the given element.
	 */
	public ConstraintChecks getConstraintChecks(Element annotatedElement, AnnotationMirror annotation) {
		AnnotationType annotationType = constraintHelper.getAnnotationType( annotation );

		switch ( annotatedElement.getKind() ) {
			case PARAMETER:
				return parameterChecks.get( annotationType );
			case FIELD:
				return fieldChecks.get( annotationType );
			case METHOD:
				return methodChecks.get( annotationType );
			case ANNOTATION_TYPE:
				return annotationTypeChecks.get( annotationType );
			case CLASS:
			case INTERFACE:
			case ENUM:
				return nonAnnotationTypeChecks.get( annotationType );
			default:
				return NULL_CHECKS;
		}
	}

}
