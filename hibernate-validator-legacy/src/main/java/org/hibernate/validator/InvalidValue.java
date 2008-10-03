//$Id$
package org.hibernate.validator;

import java.io.Serializable;

/**
 * A single violation of a class level or method level constraint.
 *
 * @author Gavin King
 */
public class InvalidValue implements Serializable {
	private final String message;
	private final Object value;
	private final String propertyName;
	private final Class beanClass;
	private final Object bean;
	private Object rootBean;

	public Object getRootBean() {
		return rootBean;
	}

	public String getPropertyPath() {
		return propertyPath;
	}

	private String propertyPath;

	public InvalidValue(String message, Class beanClass, String propertyName, Object value, Object bean) {
		this.message = message;
		this.value = value;
		this.beanClass = beanClass;
		this.propertyName = propertyName;
		this.bean = bean;
		this.rootBean = bean;
		this.propertyPath = propertyName;
	}

	public void addParentBean(Object parentBean, String propertyName) {
		this.rootBean = parentBean;
		this.propertyPath = propertyName + "." + this.propertyPath;
	}

	public Class getBeanClass() {
		return beanClass;
	}

	public String getMessage() {
		return message;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Object getValue() {
		return value;
	}

	public Object getBean() {
		return bean;
	}

	public String toString() {
		return propertyName + ' ' + message;
	}

}
