package org.hibernate.validation.engine;

import java.util.Set;
import javax.validation.BeanDescriptor;
import javax.validation.PropertyDescriptor;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class BeanDescriptorImpl<T> extends ElementDescriptorImpl implements BeanDescriptor {
	private final BeanMetaData<T> metadataBean;

	public BeanDescriptorImpl(BeanMetaData<T> metadataBean) {
		super( metadataBean.getBeanClass() );
		this.metadataBean = metadataBean;
	}

	public boolean isBeanConstrained() {
		return metadataBean.getConstrainedProperties().size() > 0;
	}

	public PropertyDescriptor getConstraintsForProperty(String propertyName) {
		return metadataBean.getPropertyDescriptor( propertyName );
	}

	public Set<PropertyDescriptor> getConstrainedProperties() {
		return metadataBean.getConstrainedProperties();
	}
}
