//$Id: $
package org.hibernate.validator.interpolator;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.util.StringHelper;
import org.hibernate.validator.MessageInterpolator;
import org.hibernate.validator.Validator;

/**
 * Resource bundle based interpolator
 * Also interpolate annotation parameters inside the message
 *
 * @author Emmanuel Bernard
 */
public class DefaultMessageInterpolator implements MessageInterpolator, Serializable {
	private static Log log = LogFactory.getLog( DefaultMessageInterpolator.class );
	private Map<String, Object> annotationParameters = new HashMap<String, Object>();
	private transient ResourceBundle messageBundle;
	private transient ResourceBundle defaultMessageBundle;
	private String annotationMessage;
	private String interpolateMessage;

	//not an interface method
	public void initialize(ResourceBundle messageBundle, ResourceBundle defaultMessageBundle) {
		this.messageBundle = messageBundle;
		this.defaultMessageBundle = defaultMessageBundle;
	}

	public void initialize(Annotation annotation, MessageInterpolator defaultInterpolator) {
		Class clazz = annotation.getClass();
		for ( Method method  : clazz.getMethods() ) {
			try {
				//FIXME remove non serilalization elements on writeObject?
				if ( method.getReturnType() != void.class
						&& method.getParameterTypes().length == 0
						&& ! Modifier.isStatic( method.getModifiers() ) ) {
					//cannot use an exclude list because the parameter name could match a method name
					annotationParameters.put( method.getName(), method.invoke( annotation ) );
				}
			}
			catch (IllegalAccessException e) {
				//really should not happen, but we degrade nicely
				log.warn( "Unable to access " + StringHelper.qualify( clazz.toString(), method.getName() ) );
			}
			catch (InvocationTargetException e) {
				//really should not happen, but we degrade nicely
				log.warn( "Unable to access " + StringHelper.qualify( clazz.toString(), method.getName() ) );
			}
		}
		annotationMessage = (String) annotationParameters.get( "message" );
		if (annotationMessage == null) {
			throw new IllegalArgumentException( "Annotation " + clazz + " does not have an (accessible) message attribute");
		}
		//do not resolve the property eagerly to allow validator.apply to work wo interpolator
    }

	private String replace(String message) {
		StringTokenizer tokens = new StringTokenizer( message, "#{}", true );
		StringBuilder buf = new StringBuilder( 30 );
		boolean escaped = false;
		boolean el = false;
		while ( tokens.hasMoreTokens() ) {
			String token = tokens.nextToken();
			if ( !escaped && "#".equals( token ) ) {
				el = true;
			}
			if ( !el && "{".equals( token ) ) {
				escaped = true;
			}
			else if ( escaped && "}".equals( token ) ) {
				escaped = false;
			}
			else if ( !escaped ) {
				if ( "{".equals( token ) ) el = false;
				buf.append( token );
			}
			else {
				Object variable = annotationParameters.get( token );
				if ( variable != null ) {
					buf.append( variable );
				}
				else {
					String string = null;
					try {
						string = messageBundle != null ? messageBundle.getString( token ) : null;
					}
					catch( MissingResourceException e ) {
						//give a second chance with the default resource bundle
					}
					if (string == null) {
						try {
							string = defaultMessageBundle.getString( token );
						}
						catch( MissingResourceException e) {
                            //return the unchanged string
                            buf.append('{').append(token).append('}');
						}
					}
					if ( string != null ) buf.append( replace( string ) );
				}
			}
		}
		return buf.toString();
	}

	public String interpolate(String message, Validator validator, MessageInterpolator defaultInterpolator) {
		if ( annotationMessage.equals( message ) ) {
			//short cut
            if (interpolateMessage == null) {
                interpolateMessage = replace( annotationMessage );
            }
            return interpolateMessage;
		}
		else {
			//TODO keep them in a weak hash map, but this might not even be useful
			return replace( message );
		}
	}

	public String getAnnotationMessage() {
		return annotationMessage;
	}
}
