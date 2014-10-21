/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
	private static final Filter INTERFACES_FILTER = new InterfacesFilter();

	/**
	 * Returns a filter which excludes interfaces.
	 *
	 * @return a filter which excludes interfaces
	 */
	public static Filter excludeInterfaces() {
		return INTERFACES_FILTER;
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

		@Override
		public boolean accepts(Class<?> clazz) {
			return !clazz.isInterface();
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
