/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util.classhierarchy;

/**
 * Provides filters to be used when invoking
 * {@link ClassHierarchyHelper#getHierarchy(Class, Filter...)}.
 *
 * @author Gunnar Morling
 */
public class Filters {

	private static final Filter PROXY_FILTER = new WeldProxyFilter();

	private Filters() {
		// Not allowed
	}

	/**
	 * Returns a filter which excludes interfaces.
	 *
	 * @return a filter which excludes interfaces
	 */
	public static Filter excludeInterfaces(Class<?> self) {
		return new InterfacesFilter( self );
	}

	/**
	 * Returns a filter which excludes proxy objects.
	 *
	 * @return a filter which excludes proxy objects
	 */
	public static Filter excludeProxies() {
		return PROXY_FILTER;
	}

	private static class InterfacesFilter implements Filter {

		private final Class<?> self;

		public InterfacesFilter(Class<?> self) {
			this.self = self;
		}

		@Override
		public boolean accepts(Class<?> clazz) {
			return !clazz.isInterface() || self.equals( clazz );
		}
	}

	private static class WeldProxyFilter implements Filter {

		private static final String WELD_PROXY_INTERFACE_NAME = "org.jboss.weld.bean.proxy.ProxyObject";

		@Override
		public boolean accepts(Class<?> clazz) {
			return !isWeldProxy( clazz );
		}

		/**
		 * Whether the given class is a proxy created by Weld or not. This is
		 * the case if the given class implements the interface
		 * {@code org.jboss.weld.bean.proxy.ProxyObject}.
		 *
		 * @param clazz the class of interest
		 *
		 * @return {@code true} if the given class is a Weld proxy,
		 *         {@code false} otherwise
		 */
		private boolean isWeldProxy(Class<?> clazz) {
			for ( Class<?> implementedInterface : clazz.getInterfaces() ) {
				if ( implementedInterface.getName().equals( WELD_PROXY_INTERFACE_NAME ) ) {
					return true;
				}
			}

			return false;
		}
	}
}
