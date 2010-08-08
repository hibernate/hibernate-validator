// $Id$
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

import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;

import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.CollectionHelper;

import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * Checks, that each constraint annotation type declares the members message(), groups() and payload().
 *
 * @author Gunnar Morling
 */
public class AnnotationTypeMemberCheck extends AbstractConstraintCheck {

	private final AnnotationApiHelper annotationApiHelper;

	private final Types typeUtils;

	public AnnotationTypeMemberCheck(AnnotationApiHelper annotationApiHelper, Types typeUtils) {

		this.annotationApiHelper = annotationApiHelper;
		this.typeUtils = typeUtils;
	}

	@Override
	public Set<ConstraintCheckError> checkAnnotationType(TypeElement element, AnnotationMirror annotation) {

		Set<ConstraintCheckError> theValue = CollectionHelper.newHashSet();

		theValue.addAll( checkMessageAttribute( element ) );
		theValue.addAll( checkGroupsAttribute( element ) );
		theValue.addAll( checkPayloadAttribute( element ) );

		return theValue;
	}

	private Set<ConstraintCheckError> checkMessageAttribute(TypeElement element) {

		ExecutableElement messageMethod = getMethod( element, "message" );

		if ( messageMethod == null ) {
			return CollectionHelper.asSet(
					new ConstraintCheckError( element, null, "CONSTRAINT_TYPE_MUST_DECLARE_MESSAGE_MEMBER" )
			);
		}

		if ( !typeUtils.isSameType(
				annotationApiHelper.getMirrorForType( String.class ), messageMethod.getReturnType()
		) ) {
			return CollectionHelper.asSet(
					new ConstraintCheckError( messageMethod, null, "RETURN_TYPE_MUST_BE_STRING" )
			);
		}

		return Collections.emptySet();
	}

	private Set<ConstraintCheckError> checkGroupsAttribute(TypeElement element) {
		return Collections.emptySet();
	}

	private Set<ConstraintCheckError> checkPayloadAttribute(TypeElement element) {
		return Collections.emptySet();
	}

	private ExecutableElement getMethod(TypeElement element, String name) {

		for ( ExecutableElement oneMethod : methodsIn( element.getEnclosedElements() ) ) {

			if ( oneMethod.getSimpleName().contentEquals( name ) ) {
				return oneMethod;
			}
		}

		return null;
	}

}
