package org.hibernate.validation.engine;

import java.util.Collections;
import java.util.Set;
import javax.validation.BeanDescriptor;
import javax.validation.PropertyDescriptor;

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
	public boolean isBeanConstrained() {
		return metadataBean.geMetaConstraintList().size() > 0;
	}

	public PropertyDescriptor getConstraintsForProperty(String propertyName) {
		return metadataBean.getPropertyDescriptors().get( propertyName );
	}

	public Set<String> getConstrainedProperties() {
		return Collections.unmodifiableSet( metadataBean.getPropertyDescriptors().keySet() );
	}
}
