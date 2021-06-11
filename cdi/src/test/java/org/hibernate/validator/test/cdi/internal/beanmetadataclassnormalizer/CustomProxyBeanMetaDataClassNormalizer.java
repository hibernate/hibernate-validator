/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal.beanmetadataclassnormalizer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.hibernate.validator.cdi.spi.BeanNames;
import org.hibernate.validator.metadata.BeanMetaDataClassNormalizer;


public class CustomProxyBeanMetaDataClassNormalizer
		implements BeanMetaDataClassNormalizer, Bean<BeanMetaDataClassNormalizer> {

	@Override
	public <T> Class<? super T> normalize(Class<T> clazz) {
		if ( CustomProxy.class.isAssignableFrom( clazz ) ) {
			return clazz.getSuperclass();
		}
		return clazz;
	}

	@Override
	public Class<?> getBeanClass() {
		return BeanMetaDataClassNormalizer.class;
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return Collections.emptySet();
	}

	// TODO to be removed once using CDI API 4.x
	public boolean isNullable() {
		return false;
	}

	@Override
	public BeanMetaDataClassNormalizer create(CreationalContext<BeanMetaDataClassNormalizer> creationalContext) {
		return new CustomProxyBeanMetaDataClassNormalizer();
	}

	@Override
	public void destroy(BeanMetaDataClassNormalizer beanMetaDataClassNormalizer,
			CreationalContext<BeanMetaDataClassNormalizer> creationalContext) {
		// Nothing to do
	}

	@Override
	public Set<Type> getTypes() {
		return Collections.singleton( BeanMetaDataClassNormalizer.class );
	}

	@Override
	public Set<Annotation> getQualifiers() {
		return Collections.singleton( NamedLiteral.of( BeanNames.BEAN_META_DATA_CLASS_NORMALIZER ) );
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return ApplicationScoped.class;
	}

	@Override
	public String getName() {
		return BeanNames.BEAN_META_DATA_CLASS_NORMALIZER;
	}

	@Override
	public Set<Class<? extends Annotation>> getStereotypes() {
		return Collections.emptySet();
	}

	@Override
	public boolean isAlternative() {
		return false;
	}
}
