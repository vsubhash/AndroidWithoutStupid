/*
 * AndroidWithoutStupid Java Library
 * Created by V. Subhash 
 * http://www.VSubhash.com
 * Released as Public Domain Software in 2014
 */
package com.vsubhash.droid.androidwithoutstupid;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

/**
 * This class makes it easy serialize and deserialize class instances
 * using a file store.
 *
<blockquote><code><pre>
String sStoreFile =  // File store 
MvFileIO.getExternalStoragePath().moResult.toString() + "/namelist.ser";

// Object to be saved in file store
ArrayList<String> alNames1 = new ArrayList<String>(), alNames2;
alNames1.add("hello");
alNames1.add("world");
alNames1.add("stupid");

// Save object to file store
MvObjectiFile mof = new MvObjectiFile(sStoreFile);
mof.save(alNames1);

// Load object from file store
alNames2 = (ArrayList<String>) mof.load();

// Access the read object
oink.showMessage(alNames2.get(0));
</pre></code></blockquote>

 * @author V. Subhash (<a href="http://www.VSubhash.com/">www.VSubhash.com</a>)
 */
public class MvObjectiFile implements Serializable {
  /**
	 * 
	 */
	private static final long serialVersionUID = 9047237167211091849L;
	Object moOriginalObject;
  String msFileStore;
  
	/**
	 * Create an instance of this class.
	 * 
	 * @param asPathname
	 *          Pathname of the file store
	 */
	public MvObjectiFile(String asPathname) {
  	super();
  	msFileStore = asPathname;
  }
  
  /**
   * Save specified object to the file store.
   * 
   * @param asInput object that needs to be serialized
   * @return whether the operation was successful
   */
  public boolean save(Object asInput) {
		FileOutputStream oFOS;
		
		try {
			oFOS = new FileOutputStream(msFileStore);
			ObjectOutputStream oOOS = new ObjectOutputStream(oFOS);
			oOOS.writeObject(asInput);
			oOOS.close();
			oFOS.close();  	
  	  return(true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			MvMessages.logMessage("Could not find the serialization file.");
  	  return(false);				
		} catch (IOException e) {
			MvMessages.logMessage("Could not write to serialization file.");
			e.printStackTrace();
  	  return(false);				
		}
  }
  
	/**
	 * Returns the deserialized object stored in the file store.
	 * 
	 * @return deserialized version of the saved object
	 * @throws ClassNotFoundException if the saved object cannot be deserialized
	 */
  public Object load() throws ClassNotFoundException {
  	Object oRet = null;
  	FileInputStream oFIS;
		try {
			oFIS = new FileInputStream(msFileStore);
			ObjectInputStream oOIS = new ObjectInputStream(oFIS);
			oRet =  oOIS.readObject();
			oOIS.close();
			oFIS.close();
		} catch (FileNotFoundException e) {
			MvMessages.logMessage("Serialization could not be found.");
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
      MvMessages.logMessage("Serialization could not be read because it is corrupt.");
			e.printStackTrace();
		} catch (IOException e) {
			MvMessages.logMessage("Serialization could not be read.");
			e.printStackTrace();
		} 
    return(oRet);
  }
  
}
