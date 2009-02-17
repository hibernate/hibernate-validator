package org.hibernate.tck.config;

/**
 * Exception thrown when errors occur while loading resource
 * 
 * @author Pete Muir
 *
 */
class ResourceLoadingException extends RuntimeException
{
   private static final long serialVersionUID = 1L;

   /**
    * Constructor
    */
   public ResourceLoadingException()
   {
      super();
   }

   /**
    * Constructor
    * 
    * @param message The message
    * @param throwable The exception
    */
   public ResourceLoadingException(String message, Throwable throwable)
   {
      super(message, throwable);
   }

   /**
    * Constructor
    * 
    * @param message The message
    */
   public ResourceLoadingException(String message)
   {
      super(message);
   }

   /**
    * Constructor
    * 
    * @param throwable The exception
    */
   public ResourceLoadingException(Throwable throwable)
   {
      super(throwable);
   }
   
}