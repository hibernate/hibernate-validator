package org.hibernate.validation.test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jboss.testharness.api.DeploymentException;
import org.jboss.testharness.spi.StandaloneContainers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandaloneContainersImpl implements StandaloneContainers {
	private static Logger log = LoggerFactory.getLogger( StandaloneContainersImpl.class );

	public void deploy(Iterable<Class<?>> classes) throws DeploymentException {
	}

	public void undeploy() {
	}

	public void setup() {
	}

	public void cleanup() {
	}

	public void deploy(Iterable<Class<?>> classes, Iterable<URL> validationXmls) throws DeploymentException {
		if ( validationXmls == null || !validationXmls.iterator().hasNext() ) {
			Thread.currentThread()
					.setContextClassLoader( new IgnoringValidationXmlClassLoader() );
			return;
		}

		URL validationXmlUrl = validationXmls.iterator().next();
		log.info( "Using {} as validation.xml", validationXmlUrl.toString() );
		Thread.currentThread()
				.setContextClassLoader( new CustomValidationXmlClassLoader( validationXmlUrl.getPath() ) );
	}


	private static class CustomValidationXmlClassLoader extends ClassLoader {
		private final String customValidationXmlPath;

		CustomValidationXmlClassLoader(String pathToCustomValidationXml) {
			super( CustomValidationXmlClassLoader.class.getClassLoader() );
			customValidationXmlPath = pathToCustomValidationXml;
		}

		public InputStream getResourceAsStream(String path) {
			InputStream in;
			if ( "META-INF/validation.xml".equals( path ) ) {
				log.info( "Using {} as validation.xml", customValidationXmlPath );
				if ( customValidationXmlPath.contains( ".jar!" ) ) {
					path = customValidationXmlPath.substring( customValidationXmlPath.indexOf( "!" ) + 2 );
					in = super.getResourceAsStream( path );
				}
				else {
					in = loadFromDisk();
				}
			}
			else {
				in = super.getResourceAsStream( path );
			}
			return in;
		}

		private InputStream loadFromDisk() {
			InputStream in;
			try {
				in = new BufferedInputStream( new FileInputStream( customValidationXmlPath ) );
			}
			catch ( IOException ioe ) {
				String msg = "Unble to load " + customValidationXmlPath + " from  disk";
				log.error( msg );
				throw new RuntimeException( msg );
			}
			return in;
		}
	}

	private static class IgnoringValidationXmlClassLoader extends ClassLoader {
		IgnoringValidationXmlClassLoader() {
			super( IgnoringValidationXmlClassLoader.class.getClassLoader() );
		}

		public InputStream getResourceAsStream(String path) {
			if ( "META-INF/validation.xml".equals( path ) ) {
				log.info( "Ignoring call to load validation.xml" );
				return null;
			}
			return super.getResourceAsStream( path );
		}
	}
}

