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

import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
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
 * Checks that the {@link org.hibernate.validator.group.GroupSequenceProvider}
 * annotation definition is valid.
 * <p>
 * This check ensure that :
 * <ul>
 * <li>The annotation is not defined on an interface.</li>
 * <li>The annotation defines an implementation of {@link DefaultGroupSequenceProvider}, not an interface or an abstract class.</li>
 * <li>The hosting class is not already annotated with {@linkplain GroupSequence @GroupSequence}.</li>
 * <li>The provider generic type definition is a (super-)type of the annotated class.</li>
 * </ul>
 * </p>
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class GroupSequenceProviderCheck extends AbstractConstraintCheck {

	private final Types typeUtils;
	private final AnnotationApiHelper annotationApiHelper;
	private final TypeMirror defaultGroupSequenceProviderType;

	public GroupSequenceProviderCheck(AnnotationApiHelper annotationApiHelper, Types typeUtils) {
		this.typeUtils = typeUtils;
		this.annotationApiHelper = annotationApiHelper;
		this.defaultGroupSequenceProviderType = annotationApiHelper.getMirrorForType( DefaultGroupSequenceProvider.class );
	}

	@Override
	public Set<ConstraintCheckError> checkNonAnnotationType(TypeElement element, AnnotationMirror annotation) {
		Set<ConstraintCheckError> checkErrors = CollectionHelper.newHashSet();

		checkErrors.addAll( checkHostingElement( element, annotation ) );
		checkErrors.addAll( checkGroupSequenceProviderAnnotationValue( element, annotation ) );

		return checkErrors;
	}

	private Set<ConstraintCheckError> checkHostingElement(TypeElement element, AnnotationMirror annotation) {
		Set<ConstraintCheckError> checkErrors = CollectionHelper.newHashSet();

		if ( !element.getKind().isClass() ) {
			checkErrors.add(
					new ConstraintCheckError(
							element, annotation, "GROUP_SEQUENCE_PROVIDER_MUST_BE_DEFINED_ON_CLASS"
					)
			);

		}

		if ( element.getAnnotation( GroupSequence.class ) != null ) {
			checkErrors.add(
					new ConstraintCheckError(
							element,
							annotation,
							"GROUP_SEQUENCE_PROVIDER_NOT_ALLOWED_ON_CLASS_ANNOTATED_WITH_GROUP_SEQUENCE"
					)
			);
		}

		return checkErrors;
	}

	private Set<ConstraintCheckError> checkGroupSequenceProviderAnnotationValue(TypeElement element, AnnotationMirror annotation) {
		Set<ConstraintCheckError> checkErrors = CollectionHelper.newHashSet();
		AnnotationValue value = annotationApiHelper.getAnnotationValue( annotation, "value" );
		TypeMirror valueType = (TypeMirror) value.getValue();
		Element valueElement = typeUtils.asElement( valueType );

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

		if ( valueElement.getKind().isInterface() || valueElement.getModifiers().contains( Modifier.ABSTRACT ) ) {
			checkErrors.add(
					new ConstraintCheckError(
							element,
							annotation,
							"GROUP_SEQUENCE_PROVIDER_CLASS_MUST_BE_AN_IMPLEMENTATION"
					)
			);
		}

		if ( !typeUtils.isSubtype( element.asType(), defaultGroupSequenceProviderGenericType ) ) {
			checkErrors.add(
					new ConstraintCheckError(
							element,
							annotation,
							"GROUP_SEQUENCE_PROVIDER_DEFINE_PROVIDER_FOR_WRONG_TYPE",
							defaultGroupSequenceProviderGenericType
					)
			);
		}

		return checkErrors;
	}
}
