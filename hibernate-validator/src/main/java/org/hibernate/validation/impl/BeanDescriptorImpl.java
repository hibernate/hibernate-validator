package org.hibernate.validation.impl;

import java.util.Set;
import java.util.Collections;
import javax.validation.BeanDescriptor;
import javax.validation.PropertyDescriptor;

import org.hibernate.validation.engine.MetaDataProvider;

/**
 * @author Emmanuel Bernard
 */
public class BeanDescriptorImpl<T> extends ElementDescriptorImpl implements BeanDescriptor {
	private final MetaDataProvider<T> metadataProvider;

	public BeanDescriptorImpl(Class<T> returnType, MetaDataProvider<T> metadataProvider) {
		super(returnType, false, "");
		this.metadataProvider = metadataProvider;
	}

	/**
	 * @todo add child validation
	 */
	public boolean hasConstraints() {
		return metadataProvider.getConstraintMetaDataList().size() > 0;
	}

	public PropertyDescriptor getConstraintsForProperty(String propertyName) {
		return metadataProvider.getPropertyDescriptors().get( propertyName );
	}

	public Set<String> getPropertiesWithConstraints() {
		return Collections.unmodifiableSet( metadataProvider.getPropertyDescriptors().keySet() );
	}
}
