package org.hibernate.tck.config;

import java.io.IOException;
import java.net.URL;

class SimpleResourceLoader
{
   
   public Class<?> classForName(String name)
   {
      
      try
      {
         return Class.forName(name);
      }
      catch (ClassNotFoundException e)
      {
         throw new ResourceLoadingException(e);
      }
      catch (NoClassDefFoundError e)
      {
         throw new ResourceLoadingException(e);
      }
   }
   
   public URL getResource(String name)
   {
      return getClass().getResource(name);
   }
   
   public Iterable<URL> getResources(String name)
   {
      try
      {
         return new EnumerationIterable<URL>(getClass().getClassLoader().getResources(name));
      }
      catch (IOException e)
      {
         throw new ResourceLoadingException(e);
      }
   }
   
}