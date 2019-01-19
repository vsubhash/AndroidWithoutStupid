/*
 * AndroidWithoutStupid Java Library
 * Created by V. Subhash 
 * http://www.VSubhash.com
 * Released as Public Domain Software in 2014
 */
package com.vsubhash.droid.androidwithoutstupid;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.vsubhash.droid.androidwithoutstupid.MvMessages;

/**
 * This class makes it easy to use Java reflection to invoke APIs available
 * only in higher Android SDK versions.
 * 
 * @author V. Subhash (<a href="http://www.VSubhash.com/">www.VSubhash.com</a>)
 * @version 2015.10.10
 */
public class MvReflection {	
	
	private MvReflection() {
		super();
	}
	
  
	/**
	 * Creates a new instance of a class with specified name. This method will
	 * invoke a constructor with specified arguments. If it returns a null, it
	 * means that the class or the constructor is not available in the SDK version
	 * of the device.
	 * 
	 * @param asClassName
	 *          full canonical name of the class
	 * @param aoConstructorArguments
	 *          arguments passed to the constructor
	 * @return new class instance if successful; null if otherwise
	 */
	@SuppressWarnings("rawtypes")
	static public Object getNewInstance(String asClassName, Object... aoConstructorArguments) {
  	Object oReturnedInstance = null;

  	try {
			Class oClass = Class.forName(asClassName);
			
			if (aoConstructorArguments == null) {
				Constructor[] oClassConstructors = oClass.getConstructors();
				for (Constructor oClassConstructor: oClassConstructors) {
					if (oClassConstructor.getParameterTypes().length == 0) {
						oReturnedInstance = oClassConstructor.newInstance(new Object[0]);
						break;
					}
				}
			} else {
				ArrayList<Class> oConstructorArgumentsClassesList = new ArrayList<Class>();
				for (Object oObject: aoConstructorArguments) {
					oConstructorArgumentsClassesList.add(oObject.getClass());
				}
				Constructor oClassConstructor = oClass.getConstructor(oConstructorArgumentsClassesList.toArray(new Class[] { }));
				oReturnedInstance = oClassConstructor.newInstance(aoConstructorArguments);
			}
			
		} catch (ClassNotFoundException e) {
			MvMessages.logMessage("Error: getNewInstance() raised " + e.getClass().getCanonicalName());
			// e.printStackTrace();
		} catch (IllegalArgumentException e) {
			MvMessages.logMessage("Error: getNewInstance() raised " + e.getClass().getCanonicalName());
			// e.printStackTrace();
		} catch (InstantiationException e) {
			MvMessages.logMessage("Error: getNewInstance() raised " + e.getClass().getCanonicalName());
			// e.printStackTrace();
		} catch (IllegalAccessException e) {
			MvMessages.logMessage("Error: getNewInstance() raised " + e.getClass().getCanonicalName());
			// e.printStackTrace();
		} catch (InvocationTargetException e) {
			MvMessages.logMessage("Error: getNewInstance() raised " + e.getClass().getCanonicalName());
			// e.printStackTrace();
		} catch (SecurityException e) {
			MvMessages.logMessage("Error: getNewInstance() raised " + e.getClass().getCanonicalName());
			// e.printStackTrace();
		} catch (NoSuchMethodException e) {
			MvMessages.logMessage("Error: getNewInstance() raised " + e.getClass().getCanonicalName() + " - " + e.getMessage());
			// e.printStackTrace();
		}
  	
  	return(oReturnedInstance);
  }


  
	/**
	 * Creates a new instance of a class with specified name in the class of the
	 * specified instance. This method will invoke a constructor with specified
	 * arguments. If it returns a null, it means that the class or the constructor
	 * is not available in the SDK version of the device. As nested class
	 * instances cannot be created unless the enclosing class instance is
	 * provided, pass an enclosing class instance after creating it first using
	 * the {@link #getNewInstance(String, Object...)} method. Remember that each
	 * level of the nesting needs to be delimited by a '$'. For example:
	 * <code>android.graphics.pdf.PdfDocument$PageInfo$Builder</code> As Java
	 * reflection will not match arguments from base-class types or derived-class
	 * types, this method will attempt to iterate through whatever constructors it
	 * finds and create an instance with the the first constructor with which it
	 * succeeds.
	 * 
	 * @param oEnclosingInstance
	 *          an instance of the enclosing class of the nested class
	 * @param asClassName
	 *          full canonical name of the class instance
	 * @param aoConstructorArguments
	 *          arguments passed to the constructor
	 * @return new class instance if successful; null if otherwise
	 */
  @SuppressWarnings({ "rawtypes", "static-access" })
	static public Object getNewNestedInstance(Object oEnclosingInstance, String asClassName, Object... aoConstructorArguments) {
  	Object oReturnedInstance = null;
  	
  	try {
			Class oClass = oEnclosingInstance.getClass().forName(asClassName);
			
			if (aoConstructorArguments == null) {
				Constructor[] oClassConstructors = oClass.getConstructors();
				for (Constructor oClassConstructor: oClassConstructors) {
					// Find the zero-argument constructor
					if (oClassConstructor.getParameterTypes().length == 0) {
						try {
							oReturnedInstance = oClassConstructor.newInstance(new Object[0]);
							break;
						} catch (IllegalArgumentException e) {
							MvMessages.logMessage("Error: getNewNestedInstance(Object, String, Object...) raised " + e.getClass().getCanonicalName());
							// e.printStackTrace();
						} catch (InstantiationException e) {
							MvMessages.logMessage("Error: getNewNestedInstance(Object, String, Object...) raised " + e.getClass().getCanonicalName());
							// e.printStackTrace();
						} catch (IllegalAccessException e) {
							MvMessages.logMessage("Error: getNewNestedInstance(Object, String, Object...) raised " + e.getClass().getCanonicalName());
							// e.printStackTrace();
						} catch (InvocationTargetException e) {
							MvMessages.logMessage("Error: getNewNestedInstance(Object, String, Object...) raised " + e.getClass().getCanonicalName());
							// e.printStackTrace();
						}
					}
				}
			} else {
				// Create a list of class types of the arguments
				ArrayList<Class> oConstructorArgumentsClassesList = new ArrayList<Class>();
				for (Object oObject: aoConstructorArguments) {
					MvMessages.logMessage("here" + oObject.getClass().getCanonicalName());
					oConstructorArgumentsClassesList.add(oObject.getClass());
				}
				try {
					// This getConstructor() method may fail easily, as it requires an
					// exact match.
					Constructor oClassConstructor = oClass.getConstructor(oConstructorArgumentsClassesList.toArray(new Class[] { }));
					oReturnedInstance = oClassConstructor.newInstance(aoConstructorArguments);
				} catch (Exception e) {  // No such constructor exists
					// Brute force method - find the first constructor that succeeds with
					// the given arguments. When base class types of the arguments match 
					// those of a constructor, a new instance may get created.
					for (Constructor oClassConstructor: oClass.getConstructors()) {
						try {
						  oReturnedInstance = oClassConstructor.newInstance(aoConstructorArguments);
						  MvMessages.logMessage("We have a winner: " + oClassConstructor.toGenericString());
						  break;
						} catch(Exception e1) {
						  MvMessages.logMessage("A constructor failed: " + oClassConstructor.toGenericString());
						}
					}	
				}
			}			
		} catch (ClassNotFoundException e) {
			MvMessages.logMessage("Error: getNewNestedInstance(Object, String, Object...) raised " + e.getClass().getCanonicalName());
			// e.printStackTrace();
		} 
  	
  	return(oReturnedInstance);
  }
  
  

  
	/**
	 * Use specified class instance and execute its method with specified name and
	 * arguments.
	 * 
	 * @param aoInvokingInstance
	 *          instance whose method needs to be called
	 * @param asMethodName
	 *          name of the method
	 * @param aoMethodArguments
	 *          arguments passed to the method
	 * @return any value returned by the method; null if the method does not exist
	 *         or if its return type is void
	 */
  @SuppressWarnings("rawtypes")
	static public Object invokeMethod(Object aoInvokingInstance, String asMethodName, Object... aoMethodArguments) {
  	Object oReturnedInstance = null;
  	ArrayList<Class> oArgsClassesList = new ArrayList<Class>();
  	
  	if (aoMethodArguments != null) {
	  	for (Object oObject : aoMethodArguments) {
				oArgsClassesList.add(oObject.getClass());
			}
  	}
  	
  	try {
			// This getMethod() method may fail easily as it requires an exact match.
			Method oMethod = aoInvokingInstance.getClass().getMethod(asMethodName, oArgsClassesList.toArray(new Class[] {}));
			oReturnedInstance = oMethod.invoke(aoInvokingInstance, aoMethodArguments);
		} catch (SecurityException e) {
			MvMessages.logMessage(e.getClass().getCanonicalName());
			// e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// Brute force tactic - find the first method overload that succeeds
			// with the given arguments. When the base types of the arguments
			// match those of a method overload, the invocation may succeed.
			for (Method oCurrentMethod : aoInvokingInstance.getClass().getMethods()) {
				if (oCurrentMethod.getName().contentEquals(asMethodName)) {
					if (oCurrentMethod.getParameterTypes().length == aoMethodArguments.length) {
						try {
							oReturnedInstance = oCurrentMethod.invoke(aoInvokingInstance, aoMethodArguments);
							MvMessages.logMessage("We have a winner: " + oCurrentMethod.toGenericString());
							break;
						} catch (Exception e1) {
							MvMessages.logMessage("A method failed:" + oCurrentMethod.toGenericString());
						  // e.printStackTrace();
						} 
					}
				}
			}
		} catch (IllegalArgumentException e) {
			MvMessages.logMessage("Error: invokeMethod(Object, String, Object...) raised " + e.getClass().getCanonicalName());
			// e.printStackTrace();
		} catch (IllegalAccessException e) {
			MvMessages.logMessage("Error: invokeMethod(Object, String, Object...) raised " + e.getClass().getCanonicalName());
			// e.printStackTrace();
		} catch (InvocationTargetException e) {
			MvMessages.logMessage("Error: invokeMethod(Object, String, Object...) raised " + e.getClass().getCanonicalName());
			// e.printStackTrace();
		}
  	return(oReturnedInstance);
  }
  
  
	/**
	 * Returns int value of specified field in the class of specified instance.
	 * 
	 * @param asFieldName
	 *          name of the field
	 * @param aoInstance
	 *          instance in whose class the field needs to be found
	 * @return result of the operation
	 */
  public static MvException getFieldAsInt(String asFieldName, Object aoInstance) {
  	MvException oResult = new MvException();
  	for (Field oField : aoInstance.getClass().getFields()) {
			MvMessages.logMessage(oField.getName());
  		if (oField.getName().contentEquals(asFieldName)) {
				try {
					oResult.moResult = oField.getInt(aoInstance);
					oResult.mbSuccess = true;
				} catch (IllegalArgumentException e) {
					oResult.mException = e;
				} catch (IllegalAccessException e) {
					oResult.mException = e;
				}
				break;
			}
		}
  	return(oResult);
  }
  
	/**
	 * Returns a field with specified name in the class of specified instance.
	 * 
	 * @param asFieldName
	 *          name of the field
	 * @param aoInstance
	 *          instance in whose class the field needs to be found
	 * @return the field if successful; null otherwise
	 */
  public static Field getField(String asFieldName, Object aoInstance) {
  	Field oReturn = null;
  	for (Field oField : aoInstance.getClass().getDeclaredFields()) {
			MvMessages.logMessage(oField.getName());
  		if (oField.getName().contentEquals(asFieldName)) {
  			oReturn = oField;
				break;
			}
		}
  	return(oReturn);
  }
  
}
