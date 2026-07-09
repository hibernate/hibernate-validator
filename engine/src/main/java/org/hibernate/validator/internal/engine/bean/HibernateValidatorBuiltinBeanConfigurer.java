/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.bean;

import org.hibernate.validator.bean.BeanHolder;

import org.hibernate.validator.internal.engine.DefaultPropertyNodeNameProvider;
import org.hibernate.validator.internal.engine.messageinterpolation.DefaultLocaleResolver;
import org.hibernate.validator.internal.engine.scripting.DefaultScriptEvaluatorFactory;
import org.hibernate.validator.internal.metadata.DefaultBeanMetaDataClassNormalizer;
import org.hibernate.validator.internal.properties.DefaultGetterPropertySelectionStrategy;
import org.hibernate.validator.metadata.BeanMetaDataClassNormalizer;
import org.hibernate.validator.spi.bean.BeanConfigurationContext;
import org.hibernate.validator.spi.bean.BeanConfigurer;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolver;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;

import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

public class HibernateValidatorBuiltinBeanConfigurer implements BeanConfigurer {

	@Override
	public void configure(BeanConfigurationContext context) {
		context.define( ScriptEvaluatorFactory.class, DefaultScriptEvaluatorFactory.NAME,
				resolver -> BeanHolder.of( new DefaultScriptEvaluatorFactory( context.classLoader() ) ) );
		context.define( GetterPropertySelectionStrategy.class, DefaultGetterPropertySelectionStrategy.NAME,
				resolver -> BeanHolder.of( new DefaultGetterPropertySelectionStrategy() ) );
		context.define( PropertyNodeNameProvider.class, DefaultPropertyNodeNameProvider.NAME,
				resolver -> BeanHolder.of( new DefaultPropertyNodeNameProvider() ) );
		context.define( LocaleResolver.class, DefaultLocaleResolver.NAME,
				resolver -> BeanHolder.of( new DefaultLocaleResolver() ) );
		context.define( BeanMetaDataClassNormalizer.class, DefaultBeanMetaDataClassNormalizer.NAME,
				resolver -> BeanHolder.of( new DefaultBeanMetaDataClassNormalizer() ) );
	}
}
