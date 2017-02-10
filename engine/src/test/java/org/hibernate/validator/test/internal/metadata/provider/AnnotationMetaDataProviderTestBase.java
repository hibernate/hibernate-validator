/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.provider;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;

import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;

/**
 * @author Gunnar Morling
 */
public abstract class AnnotationMetaDataProviderTestBase {

	protected <T> ConstrainedField findConstrainedField(BeanConfiguration<T> beanConfiguration,
														Class<? super T> clazz, String fieldName) throws Exception {

		return (ConstrainedField) findConstrainedElement( beanConfiguration, clazz.getDeclaredField( fieldName ) );
	}

	protected <T> ConstrainedExecutable findConstrainedMethod(BeanConfiguration<T> beanConfiguration,
															  Class<? super T> clazz, String methodName,
			Class<?>... parameterTypes) throws Exception {

		return (ConstrainedExecutable) findConstrainedElement( beanConfiguration, clazz.getMethod( methodName, parameterTypes ) );
	}

	protected <T> ConstrainedExecutable findConstrainedConstructor(BeanConfiguration<T> beanConfigurations, Class<T> clazz, Class<?>... parameterTypes)
			throws Exception {

		return (ConstrainedExecutable) findConstrainedElement( beanConfigurations, clazz.getConstructor( parameterTypes ) );
	}

	protected <T> ConstrainedType findConstrainedType(BeanConfiguration<T> beanConfiguration,
													  Class<? super T> type) {

		for ( ConstrainedElement constrainedElement : beanConfiguration.getConstrainedElements() ) {
			if ( constrainedElement.getKind() == ConstrainedElementKind.TYPE ) {
				ConstrainedType constrainedType = (ConstrainedType) constrainedElement;
				if ( constrainedType.getBeanClass().equals( type ) ) {
					return constrainedType;
				}
			}
		}

		throw new RuntimeException( "Found no constrained element for type " + type );
	}

	protected ConstrainedElement findConstrainedElement(BeanConfiguration<?> beanConfiguration,
														Member member) {

		for ( ConstrainedElement constrainedElement : beanConfiguration.getConstrainedElements() ) {
			if ( member instanceof Executable && constrainedElement instanceof ConstrainedExecutable ) {
				if ( member.equals( ( (ConstrainedExecutable) constrainedElement ).getExecutable() ) ) {
					return constrainedElement;
				}
			}
			else if ( member instanceof Field && constrainedElement instanceof ConstrainedField ) {
				if ( member.equals( ( (ConstrainedField) constrainedElement ).getField() ) ) {
					return constrainedElement;
				}
			}
		}

		throw new RuntimeException( "Found no constrained element for " + member );
	}
}
