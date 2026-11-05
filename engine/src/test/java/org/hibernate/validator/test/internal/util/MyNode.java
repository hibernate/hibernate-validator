package org.hibernate.validator.test.internal.util;

/**
 * @author Hardy Ferentschik
 */
public class MyNode extends Node<Integer> {
	public MyNode(Integer data) {
		super( data );
	}

	@Override
	public void setData(Integer data) {
		super.setData( data );
	}
}
