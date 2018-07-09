/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.manager;

import java.util.List;

import org.hibernate.validator.internal.engine.MethodValidationConfiguration;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.TypeResolutionHelper;

/**
 * TODO: provide correct description.
 * <p>
 * This manager is in charge of providing all constraint related meta data
 * required by the validation engine.
 * <p>
 * Actual retrieval of meta data is delegated to {@link MetaDataProvider}
 * implementations which load meta-data based e.g. based on annotations or XML.
 * <p>
 * For performance reasons a cache is used which stores all meta data once
 * loaded for repeated retrieval. Upon initialization this cache is populated
 * with meta data provided by the given <i>eager</i> providers. If the cache
 * doesn't contain the meta data for a requested type it will be retrieved on
 * demand using the annotation based provider.
 *
 * @author Gunnar Morling
 * @author Chris Beckey &lt;cbeckey@paypal.com&gt;
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public class ConstraintMetaDataManager {

	private final BeanMetaDataProvider beanMetaDataProvider;

	public ConstraintMetaDataManager(ConstraintHelper constraintHelper,
			ExecutableHelper executableHelper,
			TypeResolutionHelper typeResolutionHelper,
			ExecutableParameterNameProvider parameterNameProvider,
			ValueExtractorManager valueExtractorManager,
			JavaBeanHelper javaBeanHelper,
			ValidationOrderGenerator validationOrderGenerator,
			List<MetaDataProvider> optionalMetaDataProviders,
			MethodValidationConfiguration methodValidationConfiguration) {
		this.beanMetaDataProvider = new BeanMetaDataProvider(
				constraintHelper,
				executableHelper,
				typeResolutionHelper,
				parameterNameProvider,
				valueExtractorManager,
				javaBeanHelper,
				validationOrderGenerator,
				optionalMetaDataProviders,
				methodValidationConfiguration
		);
	}

	public <T> BeanMetaData<T> getBeanMetaData(Class<T> beanClass) {
		return beanMetaDataProvider.getBeanMetaData( beanClass );
	}

	public void clear() {
		beanMetaDataProvider.clear();
	}

	public int numberOfCachedBeanMetaDataInstances() {
		return beanMetaDataProvider.numberOfCachedBeanMetaDataInstances();
	}
}
