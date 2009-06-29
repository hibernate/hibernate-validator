package org.hibernate.validation.metadata;

import java.util.Set;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.PropertyDescriptor;

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
		return metadataBean.geMetaConstraintsAsMap().size() > 0;
	}

	public PropertyDescriptor getConstraintsForProperty(String propertyName) {
		if ( propertyName == null ) {
			throw new IllegalArgumentException( "The property name cannot be null" );
		}
		return metadataBean.getPropertyDescriptor( propertyName );
	}

	public Set<PropertyDescriptor> getConstrainedProperties() {
		return metadataBean.getConstrainedProperties();
	}
}
