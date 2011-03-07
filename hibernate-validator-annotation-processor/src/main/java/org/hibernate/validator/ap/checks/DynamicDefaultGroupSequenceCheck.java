/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.lang.model.util.Types;
import javax.validation.GroupSequence;

import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.CollectionHelper;
import org.hibernate.validator.group.DefaultGroupSequenceProvider;

/**
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class DynamicDefaultGroupSequenceCheck extends AbstractConstraintCheck {

	private final Types typeUtils;
	private final AnnotationApiHelper annotationApiHelper;
	private final TypeMirror defaultGroupSequenceProviderType;

	public DynamicDefaultGroupSequenceCheck(AnnotationApiHelper annotationApiHelper, Types typeUtils) {
		this.typeUtils = typeUtils;
		this.annotationApiHelper = annotationApiHelper;
		this.defaultGroupSequenceProviderType = annotationApiHelper.getMirrorForType( DefaultGroupSequenceProvider.class );
	}

	@Override
	public Set<ConstraintCheckError> checkNonAnnotationType(TypeElement element, AnnotationMirror annotation) {
		if ( !element.getKind().isClass() ) {
			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element, annotation, "GROUP_SEQUENCE_PROVIDER_MUST_BE_DEFINED_ON_CLASS"
					)
			);
		}

		if ( element.getAnnotation( GroupSequence.class ) != null ) {
			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element,
							annotation,
							"GROUP_SEQUENCE_PROVIDER_NOT_ALLOWED_ON_CLASS_ANNOTATED_WITH_GROUP_SEQUENCE"
					)
			);
		}

		AnnotationValue value = annotationApiHelper.getAnnotationValue( annotation, "value" );
		TypeMirror valueType = (TypeMirror) value.getValue();
		Element valueElement = typeUtils.asElement( valueType );

		if ( !valueElement.getKind().isClass() ) {
			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element,
							annotation,
							"GROUP_SEQUENCE_PROVIDER_CLASS_MAY_NOT_BE_AN_INTERFACE"
					)
			);
		}

		TypeMirror defaultGroupSequenceProviderGenericType = valueType.accept(
				new SimpleTypeVisitor6<TypeMirror, Void>() {

					@Override
					public TypeMirror visitDeclared(DeclaredType declaredType, Void aVoid) {
						TypeMirror eraseType = typeUtils.erasure( declaredType );
						if ( typeUtils.isSameType( eraseType, defaultGroupSequenceProviderType ) ) {
							return declaredType.getTypeArguments().get( 0 );
						}

						List<? extends TypeMirror> superTypes = typeUtils.directSupertypes( declaredType );
						for ( TypeMirror superType : superTypes ) {
							TypeMirror genericProviderType = superType.accept( this, aVoid );
							if ( genericProviderType != null ) {
								return genericProviderType;
							}
						}

						return null;
					}
				}, null
		);

		if ( !typeUtils.isSubtype( element.asType(), defaultGroupSequenceProviderGenericType ) ) {
			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element,
							annotation,
							"GROUP_SEQUENCE_PROVIDER_DEFINE_PROVIDER_FOR_WRONG_TYPE",
							defaultGroupSequenceProviderGenericType
					)
			);
		}

		return Collections.emptySet();
	}
}
