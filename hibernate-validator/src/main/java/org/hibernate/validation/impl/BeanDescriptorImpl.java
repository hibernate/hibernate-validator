package org.hibernate.validation.impl;

import java.util.Set;
import java.util.Collections;
import javax.validation.BeanDescriptor;
import javax.validation.PropertyDescriptor;

import org.hibernate.validation.engine.BeanMetaData;

/**
 * @author Emmanuel Bernard
 */
public class BeanDescriptorImpl<T> extends ElementDescriptorImpl implements BeanDescriptor {
	private final BeanMetaData<T> metadataBean;

	public BeanDescriptorImpl(Class<T> returnType, BeanMetaData<T> metadataBean) {
		super(returnType, false, "");
		this.metadataBean = metadataBean;
	}

	/**
	 * @todo add child validation
	 */
	public boolean hasConstraints() {
		return metadataBean.geMetaConstraintList().size() > 0;
	}

	public PropertyDescriptor getConstraintsForProperty(String propertyName) {
		return metadataBean.getPropertyDescriptors().get( propertyName );
	}

	public Set<String> getPropertiesWithConstraints() {
		return Collections.unmodifiableSet( metadataBean.getPropertyDescriptors().keySet() );
	}
}
