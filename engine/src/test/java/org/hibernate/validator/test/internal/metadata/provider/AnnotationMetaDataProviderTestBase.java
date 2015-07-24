/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.provider;

import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;

import java.lang.reflect.Member;

/**
 * @author Gunnar Morling
 */
public abstract class AnnotationMetaDataProviderTestBase {

	protected <T> ConstrainedField findConstrainedField(Iterable<BeanConfiguration<? super T>> beanConfigurations,
														Class<? super T> clazz, String fieldName) throws Exception {
		return (ConstrainedField) findConstrainedElement( beanConfigurations, clazz.getDeclaredField( fieldName ) );
	}

	protected <T> ConstrainedExecutable findConstrainedMethod(Iterable<BeanConfiguration<? super T>> beanConfigurations,
															  Class<? super T> clazz, String methodName, Class<?>... parameterTypes)
			throws Exception {
		return (ConstrainedExecutable) findConstrainedElement(
				beanConfigurations,
				clazz.getMethod( methodName, parameterTypes )
		);
	}

	protected <T> ConstrainedExecutable findConstrainedConstructor(
			Iterable<BeanConfiguration<? super T>> beanConfigurations, Class<? super T> clazz,
			Class<?>... parameterTypes) throws Exception {
		return (ConstrainedExecutable) findConstrainedElement(
				beanConfigurations,
				clazz.getConstructor( parameterTypes )
		);
	}

	protected <T> ConstrainedType findConstrainedType(Iterable<BeanConfiguration<? super T>> beanConfigurations,
													  Class<? super T> type) {
		for ( BeanConfiguration<?> oneConfiguration : beanConfigurations ) {
			for ( ConstrainedElement constrainedElement : oneConfiguration.getConstrainedElements() ) {
				if ( constrainedElement.getLocation().getMember() == null ) {
					ConstrainedType constrainedType = (ConstrainedType) constrainedElement;
					if ( constrainedType.getLocation().getDeclaringClass().equals( type ) ) {
						return constrainedType;
					}
				}
			}
		}

		throw new RuntimeException( "Found no constrained element for type " + type );
	}

	protected ConstrainedElement findConstrainedElement(Iterable<? extends BeanConfiguration<?>> beanConfigurations,
														Member member) {
		for ( BeanConfiguration<?> oneConfiguration : beanConfigurations ) {
			for ( ConstrainedElement constrainedElement : oneConfiguration.getConstrainedElements() ) {
				if ( constrainedElement.getLocation().getMember().equals( member ) ) {
					return constrainedElement;
				}
			}
		}

		throw new RuntimeException( "Found no constrained element for " + member );
	}
}
