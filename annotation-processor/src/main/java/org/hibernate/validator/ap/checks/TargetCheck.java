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

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.CollectionHelper;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

/**
 * Checks, that at least one of the {@link ElementType}s FIELD, METHOD, TYPE or ANNOTATION_TYPE is specified
 * using the {@link Target} meta-annotation for constraint annotation types.
 *
 * @author Gunnar Morling
 */
public class TargetCheck extends AbstractConstraintCheck {

	private final AnnotationApiHelper annotationApiHelper;

	/**
	 * Each constraint annotation type must be targeted at one of these element types at least.
	 */
	private final EnumSet<ElementType> supportedTypes = EnumSet.of( FIELD, METHOD, TYPE, ANNOTATION_TYPE );

	public TargetCheck(AnnotationApiHelper annotationApiHelper) {
		this.annotationApiHelper = annotationApiHelper;
	}

	@Override
	public Set<ConstraintCheckError> checkAnnotationType(TypeElement element, AnnotationMirror annotation) {

		Target target = element.getAnnotation( Target.class );

		//no target given allows the annotation to be declared at any type
		if ( target == null ) {
			return Collections.emptySet();
		}

		if ( !containsAtLeastOneSupportedElementType( target ) ) {
			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element,
							annotationApiHelper.getMirror( element.getAnnotationMirrors(), Target.class ),
							"CONSTRAINT_TYPE_WITH_WRONG_TARGET"
					)
			);
		}

		return Collections.emptySet();
	}

	private boolean containsAtLeastOneSupportedElementType(Target target) {

		ElementType[] elementTypes = target.value();

		for ( ElementType oneElementType : elementTypes ) {
			if ( supportedTypes.contains( oneElementType ) ) {
				return true;
			}
		}

		return false;
	}

}
