/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.ap.checks;

import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.util.Types;

import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.CollectionHelper;
import org.hibernate.validator.ap.util.ConstraintHelper;
import org.hibernate.validator.ap.util.ConstraintHelper.AnnotationType;

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

	private ConstraintHelper constraintHelper;

	private static final SingleValuedChecks NULL_CHECKS = new SingleValuedChecks();

	public ConstraintCheckFactory(Types typeUtils, ConstraintHelper constraintHelper, AnnotationApiHelper annotationApiHelper, boolean methodConstraintsSupported) {

		this.constraintHelper = constraintHelper;

		fieldChecks = CollectionHelper.newHashMap();
		fieldChecks.put(
				AnnotationType.CONSTRAINT_ANNOTATION,
				new SingleValuedChecks( new StaticCheck(), new TypeCheck( constraintHelper ) )
		);
		fieldChecks.put(
				AnnotationType.MULTI_VALUED_CONSTRAINT_ANNOTATION,
				new MultiValuedChecks( constraintHelper, new StaticCheck(), new TypeCheck( constraintHelper ) )
		);
		fieldChecks.put(
				AnnotationType.GRAPH_VALIDATION_ANNOTATION,
				new SingleValuedChecks( new StaticCheck(), new PrimitiveCheck() )
		);
		fieldChecks.put( AnnotationType.NO_CONSTRAINT_ANNOTATION, NULL_CHECKS );

		methodChecks = CollectionHelper.newHashMap();
		methodChecks.put(
				AnnotationType.CONSTRAINT_ANNOTATION,
				new SingleValuedChecks( new GetterCheck(methodConstraintsSupported), new StaticCheck(), new TypeCheck( constraintHelper ) )
		);
		methodChecks.put(
				AnnotationType.MULTI_VALUED_CONSTRAINT_ANNOTATION, new MultiValuedChecks(
						constraintHelper, new GetterCheck(methodConstraintsSupported), new StaticCheck(), new TypeCheck( constraintHelper )
				)
		);
		methodChecks.put(
				AnnotationType.GRAPH_VALIDATION_ANNOTATION,
				new SingleValuedChecks( new GetterCheck(methodConstraintsSupported), new StaticCheck(), new PrimitiveCheck() )
		);
		methodChecks.put( AnnotationType.NO_CONSTRAINT_ANNOTATION, NULL_CHECKS );

		annotationTypeChecks = CollectionHelper.newHashMap();
		annotationTypeChecks.put(
				AnnotationType.CONSTRAINT_ANNOTATION,
				new SingleValuedChecks( new AnnotationTypeCheck( constraintHelper ) )
		);
		annotationTypeChecks.put(
				AnnotationType.MULTI_VALUED_CONSTRAINT_ANNOTATION,
				new MultiValuedChecks( constraintHelper, new AnnotationTypeCheck( constraintHelper ) )
		);
		annotationTypeChecks.put(
				AnnotationType.CONSTRAINT_META_ANNOTATION,
				new SingleValuedChecks(
						new RetentionPolicyCheck( annotationApiHelper ),
						new TargetCheck( annotationApiHelper ),
						new ConstraintValidatorCheck( constraintHelper, annotationApiHelper ),
						new AnnotationTypeMemberCheck( annotationApiHelper, typeUtils )
				)
		);
		annotationTypeChecks.put( AnnotationType.NO_CONSTRAINT_ANNOTATION, NULL_CHECKS );

		nonAnnotationTypeChecks = CollectionHelper.newHashMap();
		nonAnnotationTypeChecks.put(
				AnnotationType.CONSTRAINT_ANNOTATION, new SingleValuedChecks( new TypeCheck( constraintHelper ) )
		);
		nonAnnotationTypeChecks.put(
				AnnotationType.MULTI_VALUED_CONSTRAINT_ANNOTATION,
				new MultiValuedChecks( constraintHelper, new TypeCheck( constraintHelper ) )
		);
		nonAnnotationTypeChecks.put( AnnotationType.NO_CONSTRAINT_ANNOTATION, NULL_CHECKS );
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
