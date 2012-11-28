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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementKindVisitor6;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.lang.model.util.Types;

import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.CollectionHelper;
import org.hibernate.validator.ap.util.TypeNames.BeanValidationTypes;
import org.hibernate.validator.ap.util.TypeNames.HibernateValidatorTypes;

/**
 * Checks that the {@link org.hibernate.validator.group.GroupSequenceProvider}
 * annotation definition is valid.
 * <p>
 * This check ensure that :
 * <ul>
 * <li>The annotation is not defined on an interface.</li>
 * <li>The annotation defines an implementation class of
 * {@link org.hibernate.validator.spi.group.DefaultGroupSequenceProvider}, not
 * an interface or an abstract class.</li>
 * <li>The annotation defines a class with a public default constructor.</li>
 * <li>The annotation defines a default group sequence provider class for a
 * (super-)type of the annotated class.</li>
 * <li>The class hosting the annotation is not already annotated with
 * {@link javax.validation.GroupSequence}.</li>
 * </ul>
 * </p>
 *
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public class GroupSequenceProviderCheck extends AbstractConstraintCheck {

	private final Types typeUtils;
	private final AnnotationApiHelper annotationApiHelper;
	private final TypeMirror defaultGroupSequenceProviderType;

	public GroupSequenceProviderCheck(AnnotationApiHelper annotationApiHelper, Types typeUtils) {
		this.typeUtils = typeUtils;
		this.annotationApiHelper = annotationApiHelper;
		this.defaultGroupSequenceProviderType = annotationApiHelper.getDeclaredTypeByName( HibernateValidatorTypes.DEFAULT_GROUP_SEQUENCE_PROVIDER );
	}

	@Override
	public Set<ConstraintCheckError> checkNonAnnotationType(TypeElement element, AnnotationMirror annotation) {
		Set<ConstraintCheckError> errors = CollectionHelper.newHashSet();

		errors.addAll( checkHostingElement( element, annotation ) );
		errors.addAll( checkAnnotationValue( element, annotation ) );

		return errors;
	}

	private Set<ConstraintCheckError> checkHostingElement(TypeElement element, AnnotationMirror annotation) {
		if ( !element.getKind().isClass() ) {
			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element, annotation, "GROUP_SEQUENCE_PROVIDER_ANNOTATION_MUST_BE_DEFINED_ON_A_CLASS"
					)
			);
		}

		//this error should be raised only if the GroupSequenceProvider annotations is on a class
		if ( annotationApiHelper.getMirror(
				element.getAnnotationMirrors(),
				BeanValidationTypes.GROUP_SEQUENCE
		) != null ) {
			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element,
							annotation,
							"GROUP_SEQUENCE_PROVIDER_ANNOTATION_NOT_ALLOWED_ON_CLASS_WITH_GROUP_SEQUENCE_ANNOTATION"
					)
			);
		}

		return Collections.emptySet();
	}

	private Set<ConstraintCheckError> checkAnnotationValue(TypeElement element, AnnotationMirror annotation) {
		Set<ConstraintCheckError> errors = CollectionHelper.newHashSet();
		AnnotationValue value = annotationApiHelper.getAnnotationValue( annotation, "value" );
		TypeMirror valueType = (TypeMirror) value.getValue();
		TypeElement valueElement = (TypeElement) typeUtils.asElement( valueType );

		if ( valueElement.getKind().isInterface() || valueElement.getModifiers().contains( Modifier.ABSTRACT ) ) {
			errors.add(
					new ConstraintCheckError(
							element,
							annotation,
							"GROUP_SEQUENCE_PROVIDER_ANNOTATION_VALUE_MUST_BE_AN_IMPLEMENTATION_CLASS"
					)
			);

		}
		else {
			//the TypeElement hosting the annotation is a concrete implementation of the DefaultGroupSequenceProvider
			//interface. In that case, we need to check that it has a public default constructor.
			if ( !hasPublicDefaultConstructor( valueElement ) ) {
				errors.add(
						new ConstraintCheckError(
								element,
								annotation,
								"GROUP_SEQUENCE_PROVIDER_ANNOTATION_VALUE_CLASS_MUST_HAVE_DEFAULT_CONSTRUCTOR",
								valueType
						)
				);
			}
		}

		TypeMirror genericProviderType = retrieveGenericProviderType( valueType );
		if ( !typeUtils.isSubtype( element.asType(), genericProviderType ) ) {
			errors.add(
					new ConstraintCheckError(
							element,
							annotation,
							"GROUP_SEQUENCE_PROVIDER_ANNOTATION_VALUE_DEFINED_PROVIDER_CLASS_WITH_WRONG_TYPE",
							genericProviderType,
							element.asType()
					)
			);
		}

		return errors;
	}

	/**
	 * Checks that the given {@code TypeElement} has a public
	 * default constructor.
	 *
	 * @param element The {@code TypeElement} to check.
	 *
	 * @return True if the given {@code TypeElement} has a public default constructor, false otherwise
	 */
	private boolean hasPublicDefaultConstructor(TypeElement element) {
		return element.accept(
				new ElementKindVisitor6<Boolean, Void>( Boolean.FALSE ) {

					@Override
					public Boolean visitTypeAsClass(TypeElement typeElement, Void aVoid) {
						List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
						for ( Element enclosedElement : enclosedElements ) {
							if ( enclosedElement.accept( this, aVoid ) ) {
								return Boolean.TRUE;
							}
						}
						return Boolean.FALSE;
					}

					@Override
					public Boolean visitExecutableAsConstructor(ExecutableElement constructorElement, Void aVoid) {
						if ( constructorElement.getModifiers().contains( Modifier.PUBLIC )
								&& constructorElement.getParameters().isEmpty() ) {

							return Boolean.TRUE;
						}
						return Boolean.FALSE;
					}

				}, null
		);
	}

	/**
	 * Retrieves the default group sequence provider generic type defined by the given {@code TypeMirror}.
	 *
	 * @param typeMirror The {@code TypeMirror} instance.
	 *
	 * @return The generic type or {@code null} if the given type doesn't implement the {@link org.hibernate.validator.group.DefaultGroupSequenceProvider} interface.
	 */
	private TypeMirror retrieveGenericProviderType(TypeMirror typeMirror) {
		return typeMirror.accept(
				new SimpleTypeVisitor6<TypeMirror, Void>() {

					@Override
					public TypeMirror visitDeclared(DeclaredType declaredType, Void aVoid) {
						TypeMirror eraseType = typeUtils.erasure( declaredType );
						if ( typeUtils.isSameType( eraseType, defaultGroupSequenceProviderType ) ) {
							List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
							if ( !typeArguments.isEmpty() ) {
								return typeArguments.get( 0 );
							}
							return null;
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
	}
}
