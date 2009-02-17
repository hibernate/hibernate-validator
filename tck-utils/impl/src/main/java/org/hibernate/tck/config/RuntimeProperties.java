package org.hibernate.tck.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Utility class to load deployment properties
 * 
 * @author Pete Muir
 */
public class RuntimeProperties
{
   // The resource bundle used to control tck unit runtime properties
   public static final String RESOURCE_BUNDLE = "META-INF/tck-unit.properties";
   
   //private static final Logger log = Logger.getLogger(DeploymentProperties.class);
   
   // The class to work from
   private SimpleResourceLoader resourceLoader;

   /**
    * Constructor
    * 
    * @param classLoader The classloader to work on
    */
   public RuntimeProperties()
   {
      this.resourceLoader = new SimpleResourceLoader();
   }

   /**
    * Get a list of possible values for a given key.
    * 
    * First, System properties are tried, followed by the specified resource
    * bundle (first in classpath only).
    * 
    * @param key The key to search for
    * @return A list of possible values. An empty list is returned if there are
    *         no matches.
    */
   public List<String> getPropertyValues(String key)
   {
      List<String> values = new ArrayList<String>();
      addPropertiesFromSystem(key, values);
      addPropertiesFromResourceBundle(key, values);
      return values;
   }

   /**
    * Adds matches from system properties
    * 
    * @param key The key to match
    * @param values The currently found values
    */
   private void addPropertiesFromSystem(String key, List<String> values)
   {
      addProperty(key, System.getProperty(key), values);
   }

   /**
    * Adds matches from detected resource bundles
    * 
    * @param key The key to match
    * @param values The currently found values
    */
   private void addPropertiesFromResourceBundle(String key, List<String> values)
   {
      try
      {
         for (URL url : resourceLoader.getResources(RESOURCE_BUNDLE))
         {
            Properties properties = new Properties();
            InputStream propertyStream = url.openStream();
            try
            {
               properties.load(propertyStream);
               addProperty(key, properties.getProperty(key), values);
            }
            finally
            {
               if (propertyStream != null)
               {
                  propertyStream.close();
               }
            }
         }
      }
      catch (IOException e)
      {
         // No - op, file is optional
      }
   }

   /**
    * Add the property to the set of properties only if it hasn't already been
    * added
    * 
    * @param key The key searched for
    * @param value The value of the property
    * @param values The currently found values
    */
   private void addProperty(String key, String value, List<String> values)
   {
      if (value != null)
      {
         //String[] properties = Strings.split(value, "[^\\]:");
         //for (String property : properties)
         //{
            //values.add(property);
         //}
         values.add(value);

      }
   }
   
   /**
    * Gets the possible implementation class for a given property for which the
    * values are classanames
    * 
    * @param deploymentProperties The deployment properties object to use
    * @param resourceLoader The resource laoder to use to attempt
    * @param propertyName The name of the property to load
    * @return A set of classes specified
    */
   @SuppressWarnings("unchecked")
   public <T> Set<Class<T>> getClasses(String propertyName, Class<T> expectedType)
   {
      Set<Class<T>> classes = new HashSet<Class<T>>();
      for (String className : getPropertyValues(propertyName))
      {
         try
         {
            classes.add((Class<T>) resourceLoader.classForName(className));
         }
         catch (ResourceLoadingException e)
         {
            //log.debug("Unable to load class " + className + " for property " + propertyName, e);
         }
      }
      return classes;
   }
   
   public <T> Class<T> getClassValue(String propertyName, Class<T> expectedType, boolean required)
   {
      Set<Class<T>> classes = getClasses(propertyName, expectedType);
      if (classes.size() == 0)
      {
         if (required)
         {
            throw new IllegalArgumentException("Cannot find any implementations of " + expectedType.getSimpleName() + ", check that " + propertyName + " is specified");
         }
         else
         {
            return null;
         }
      }
      else if (classes.size() > 1)
      {
         throw new IllegalArgumentException("More than one implementation of " + expectedType.getSimpleName() + " specified by " + propertyName + ", not sure which one to use!");
      }
      else
      {
         return classes.iterator().next(); 
      }
   }
   
   public <T> T getInstanceValue(String propertyName, Class<T> expectedType, boolean required)
   {
      Class<T> clazz = getClassValue(propertyName, expectedType, required);
      if (clazz != null)
      {
         try
         {
            return clazz.newInstance();
         }
         catch (InstantiationException e)
         {
            throw new IllegalStateException("Error instantiating " + clazz + " specified by " + propertyName, e);
         }
         catch (IllegalAccessException e)
         {
            throw new IllegalStateException("Error instantiating " + clazz + " specified by " + propertyName, e);
         }
      }
      else
      {
         return null;
      }
   }
   
   public boolean getBooleanValue(String propertyName, boolean _default, boolean required)
   {
      return Boolean.valueOf(getStringValue(propertyName, _default ? "true" : "false", required));
   }
   
   public int getIntValue(String propertyName, int _default, boolean required)
   {
      return Integer.valueOf(getStringValue(propertyName, Integer.toString(_default), required)).intValue();
   }
   
   public String getStringValue(String propertyName, String _default, boolean required)
   {
      List<String> values = getPropertyValues(propertyName);
      
      if (values.size() == 0)
      {
         if (required)
         {
            throw new IllegalArgumentException("Cannot find required property " + propertyName + ", check that it is specified");
         }
         else
         {
            return _default;
         }
      }
      else if (values.size() > 1)
      {
         throw new IllegalArgumentException("More than one value given for " + propertyName + ", not sure which one to use!");
      }
      else
      {
         return values.iterator().next();
      }
   }

}