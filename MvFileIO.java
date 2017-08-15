/*
 * AndroidWithoutStupid Java Library
 * Created by V. Subhash 
 * http://www.VSubhash.com
 * Released as Public Domain Software in 2014
 */
package com.vsubhash.droid.androidwithoutstupid;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.http.util.EncodingUtils;

import android.os.Debug;
import android.os.Environment;

/**
 * This class provides routines for performing file I/O operations.
 * 
 * @author V. Subhash (<a href="http://www.VSubhash.com/">www.VSubhash.com</a>)
 * @version 2017.08.15
 */
public class MvFileIO {
	
  private static final long FILE_COPY_BUFFER_SIZE = 1024*1024;	
  
	/**
	 * Checks if a bitmap with the specified size fits in memory
	 * @param bmpwidth Bitmap width
	 * @param bmpheight Bitmap height
	 * @param bmpdensity Bitmap bpp (use 2 as default)
	 * @return true if the bitmap fits in memory false otherwise
	 */
	public static boolean checkBitmapFitsInMemory(long bmpwidth,long bmpheight, int bmpdensity ){
	    long reqsize=bmpwidth*bmpheight*bmpdensity;
	    long allocNativeHeap = Debug.getNativeHeapAllocatedSize();


	    final long heapPad=(long) Math.max(4*1024*1024,Runtime.getRuntime().maxMemory()*0.1);
	    if ((reqsize + allocNativeHeap + heapPad) >= Runtime.getRuntime().maxMemory())
	    {
	        return false;
	    }
	    return true;
	}
	
	
	/**
	 * Copies contents from a specified stream to a specified file.
	 * 
	 * @param aoFromStream
	 *          stream containing the text to be copied
	 * @param asTo
	 *          pathname of the file to be copied
	 * @return outcome of the operation
	 */
  public static MvException copyFile(InputStream aoFromStream, String asTo) {
  	FileOutputStream oOutputFileStream;
  	MvException oResult = new MvException();
    byte[] bBuffer = new byte[1024*1024];
    int iBytesRead = 0;
    
    try {
      oOutputFileStream = new FileOutputStream(asTo);
      
    	do {
    		iBytesRead = aoFromStream.read(bBuffer);
    		if (iBytesRead < 1) { continue; }
    		oOutputFileStream.write(bBuffer, 0, iBytesRead);
    	} while (iBytesRead > 0);
    	aoFromStream.close();
    	oOutputFileStream.close();
    	oResult.mbSuccess = true;
    	oResult.moResult = asTo;
		} catch (FileNotFoundException e) {
			oResult.mbSuccess = false;			
			oResult.mException = e;
			oResult.msProblem = "File not found";
		} catch (IOException e) {
			oResult.mbSuccess = false;
			oResult.mException = e;
			oResult.msProblem = "Unable to write to storage";
		} catch (Exception e) {
			oResult.mbSuccess = false;
			oResult.mException = e;
			oResult.msProblem = "Some other error";
		}
    
    return(oResult);
  }
	
	
	/**
	 * Copies contents of a specified file to another specified file.
	 * 
	 * @param asFrom
	 *          pathname of the file from which text needs to be copied
	 * @param asTo
	 *          pathname of the file to which text needs to be copied
	 * @return outcome of the operation
	 */
  public static MvException copyFile(String asFrom, String asTo) {
  	MvException oResult = new MvException();
  	FileInputStream oFromStream;
  	
  	File oFromFile = new File(asFrom);
  	if (oFromFile.isFile() && oFromFile.canRead()) {
	  	try {
				oFromStream = new FileInputStream(asFrom);
				oResult = copyFile(oFromStream, asTo);
				if (oResult.mbSuccess) {
					oResult.mbSuccess = true;
					oResult.moResult = asTo;
				} else {
					oResult.moResult = oResult;
					oResult.msProblem = oResult.getMessage();					
				}
			} catch (FileNotFoundException e1) {
				oResult.moResult = e1;
				oResult.msProblem = "The file was not found";
				e1.printStackTrace();
			}
  	} else {
  		oResult.msProblem = "Not a file or cannot be read";
  	}
  	
    return(oResult);
  }
	
  
	/**
	 * Creates a directory with specified path.
	 * 
	 * @param asPathname path to the directory.
	 * @return information whether the operation was successful
	 */
	public static MvException createDirectory(String asPathname) {
		File oDir = new File(asPathname);
		MvException result = new MvException();

		if (!oDir.exists()) {
			if (oDir.mkdirs()) {
				result.mbSuccess = true;
				result.moResult = oDir;
			} else {
				result.mbSuccess = false;
				result.msProblem = "Could not create directory at " + asPathname;
			}
		} else {
			result.mbSuccess = true;
			result.msProblem = "Directory already exists";
		}
	
    return(result);
	}  
  
  
	/**
	 * Creates a file with specified pathname. This method returns false if the
	 * file already exists. So, check that file does not exist before calling 
	 * this method.
	 * 
	 * @param asPathname
	 *          file or directory that needs to be created
	 * @return whether it was successful or not
	 */
	public static MvException createNewFile(String asPathname) {
		MvException oRet = new MvException();
		File oFile = new File(asPathname);
		if (oFile.exists()) {
			oRet.mbSuccess = false;
			return(oRet);
		} else {
			try {
				oFile.createNewFile();
			} catch (Exception e) {
				oRet.mbSuccess = false;
				oRet.mException = e;
				return(oRet);
			}
			oRet.mbSuccess = true;
			oRet.moResult = oFile;
			return(oRet);
		}
	}
	
	
	/**
	 * 
	 * 
	 * @param asPath
	 * @return
	 */
	public static boolean deletePath(String asPath) {
		return(deletePath(asPath, false));
	}
	
	/**
	 * Deletes the file or directory. As this method internally calls
	 * itself, use {@link isPathExist} afterwards to see if the method was
	 * successful.
	 * 
	 * @param asPath path of the file or directory
	 * @param abKeepPath whether to retain the directory but delete everything in it 
	 * @return whether it was successful
	 */
	public static boolean deletePath(String asPath, boolean abKeepPath) {
		if (asPath == null) {
			return(false);
		}
		
		File oPath = new File(asPath);
		if (oPath.exists()) {
	    if (oPath.isDirectory()) {
	      for (File oEntry : oPath.listFiles()) {
	      	if (oEntry.isDirectory()) {
	      		if (!deletePath(oEntry.getAbsolutePath())) {
	      			return(false);      		
	      		}
	      	} else {
	       		if (oEntry.delete()) {
	       			MvMessages.logMessage(((oEntry.isFile())?"Deleted file":"Deleted directory") + " - " + 
	       					oEntry.getAbsolutePath());
	       		} else {	
	       		  MvMessages.logMessage(((oEntry.isFile())?"Could not delete file":"Could not delete directory") + " - " + 
	       		  		oEntry.getAbsolutePath());
	       		  return(false);
	       		}
	      	}
	      }
	    }
	    
	 		if (!abKeepPath) {
		    if (oPath.delete()) {
		 			MvMessages.logMessage(((oPath.isFile())?"Deleted file ":"Deleted Directory") + " - " + 
		 														oPath.getAbsolutePath());
		 			return(true);
		 		} else {	
		 		  MvMessages.logMessage(((oPath.isFile())?"Could not delete file ":"Could not delete directory") + " - " + 
		 		  											oPath.getAbsolutePath());
		 		  return(false);
		 		}
	 		} else {
	 			return(false);
	 		}
		} else {
 		  MvMessages.logMessage(((oPath.isFile())?"Could not find file ":"Could not find directory") + " - " + 
						oPath.getAbsolutePath());
			return(false);
		}
	}
	
	
	/**
	 * Returns path of the external storage (SD Card).
	 * 
	 * @return result of the operation
	 */
	public static MvException getExternalStoragePath() {
		String sDirPath;
		
		MvException oResult = new MvException();
		
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			sDirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
			oResult.moResult = sDirPath;
			oResult.mbSuccess = true;			
		} else {
			oResult.mbSuccess = false;
			oResult.msProblem = "External storage is not ready";
			oResult.msPossibleSolution = "Insert SD card or wait for SD card to be recognized";
		}
		return(oResult);
	}
	
	
	/**
	 * Returns file name with an incremented file counter if the file already exists. 
	 * 
	 * @param asPathname pathname of the file which needs to be parsed
	 * @return incremented file name if file already exists; else the original name
	 */
	public static String getIncrementedFileName(String asPathname) {
		File oOldFile, oNewFile;
		oOldFile = new File(asPathname);
		
		String sDirectory = oOldFile.getParent();
		String sFilename = getFileNameWithoutExtension(asPathname);
		String sExtn = getFileExtension(asPathname);
		
		if (!oOldFile.exists()) {
			MvMessages.logMessage("File does not exist = " + sFilename + "." + sExtn);
			return(sFilename + "." + sExtn);
		} else {
			int i;
			for (i = 2; i < 65000; i++) {
				oNewFile = new File(sDirectory + File.separator + sFilename + "(" + i + ")." + sExtn);
				if (oNewFile.exists()) {
					//MvMessages.logMessage("Exists " + sFilename + "(" + i + ")." + sExtn);
					continue;
				} else {
					break;
				}
			}
			return(sFilename + "(" + i + ")." + sExtn);
		}
	}
	
	/**
	 * Returns file name without the extension and the '.' character.
	 * 
	 * @param asPathname pathname of the file
	 * @return file name without the extension
	 */
	public static String getFileNameWithoutExtension(String asPathname) {
		String sRet = getFileNameFromPath(asPathname);
		if (sRet.lastIndexOf('.') > -1) {
			sRet = sRet.substring(0, sRet.lastIndexOf('.'));
		}
		return(sRet);
	}
	
	/**
	 * Returns the file name for specified path.
	 * 
	 * @param asPathname pathname whose file name is required
	 * @return the file name
	 */
	public static String getFileExtension(String asPathname) {
		String sFileName, sExtn = "";
		sFileName = getFileNameFromPath(asPathname); 
		if (sFileName.lastIndexOf('.') > -1) {
			sExtn = sFileName.substring(sFileName.lastIndexOf('.')+1);
		}
		return(sExtn);
	}
 
	
	/**
	 * Returns specified file name after eliminating illegal file system characters. 
	 * 
	 * @param asName file name that needs to parsed
	 * @return file system-safe name
	 */
	public static String getSafeFileNameFor(String asName) {
		char[] ILLEGAL_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '&', '<', '>', '|', '\"', ':' };
		String sRet = null;
		StringBuilder sBuf = new StringBuilder();
		boolean bFound;
		
		for (int i = 0; i < asName.length(); i++) {
			bFound = false;
			for (char c : ILLEGAL_CHARACTERS) {
				if (asName.charAt(i) == c) {
					bFound = true;
					break;
				}
			}
			if (bFound) {
				sBuf.append('_');
			} else {
				sBuf.append(asName.charAt(i));
			}
		}
		sRet = sBuf.toString();
		
		return(sRet);
	}
	
	/**
	 * Returns the file name for the specified file.
	 * 
	 * @param asPathname pathname of the file
	 * @return file name
	 */
	public static String getFileNameFromPath(String asPathname) {
		String sRet = "";
		
		if (asPathname.lastIndexOf('/') > -1) {
			sRet = asPathname.substring(asPathname.lastIndexOf('/')+1);
		}
		
		return(sRet);
	}
	
	/**
	 * Returns path of the parent directory containing the specified
	 * file/directory.
	 * 
	 * @param asPathname
	 *          pathname of the file or directory whose containing directory needs
	 *          to be found
	 * @return path of the parent directory if it exists; an empty string if not
	 */
	public static String getParentDirectoryFromPath(String asPathname) {
		File oFile = new File(asPathname);
		String sReturn = "";
		if (oFile.exists()) {
			if (oFile.getParent() != null) {
				sReturn = oFile.getParent();
			}
		} else if (asPathname.lastIndexOf("/") > -1) {
				sReturn = asPathname.substring(0, asPathname.lastIndexOf("/"));
		}
		
		return(sReturn);
	}
	
	/**
	 * Returns the size of the file.
	 * 
	 * @param asPathname pathname of the file
	 * @return size of the file
	 */
	public static long getFileSize(String asPathname) {
		long lFileSize = -1;
		if (asPathname != null) {
		  File oFile = new File(asPathname);
		  if (oFile.exists()) {
		  	lFileSize = oFile.length();
		  }
		}
		return(lFileSize);
	}

	
	/**
	 * Returns text content of specified text file.
	 * 
	 * @param asTextFile
	 *          text file whose text content is required.
	 * @return outcome of the operation
	 */
  public static MvException getFileAsText(String asTextFile, int iSize) {
  	MvException oRet = new MvException();
  	String sRet;
  	byte[] buf = new byte[iSize];
  	FileInputStream oInputStream;
		try {
			oInputStream = new FileInputStream(asTextFile);
			oInputStream.read(buf, 0, buf.length);
			
			if (((buf[0] & 0xFF) == 0xFF) && ((buf[1] & 0xFE) == 0xFE)) {
				sRet = EncodingUtils.getString(buf, "UTF-16LE");	
			} else if (((buf[0] & 0xFE) == 0xFE) && ((buf[1] & 0xFF) == 0xFF)) {
				sRet = EncodingUtils.getString(buf, "UTF-16BE");
			} else if (((buf[0] & 0xFF) == 0xFF) && ((buf[1] & 0xFE) == 0xFE) &&
								 ((buf[0] & 0x00) == 0x00) && ((buf[1] & 0x00) == 0x00)) {
				sRet = EncodingUtils.getString(buf, "UTF-32LE");
			} else if (((buf[0] & 0x00) == 0x00) && ((buf[1] & 0x00) == 0x00) &&
								 ((buf[0] & 0xFE) == 0xFE) && ((buf[1] & 0xFF) == 0xFF)) {
				sRet = EncodingUtils.getString(buf, "UTF-32BE");
			} else {
				sRet = EncodingUtils.getAsciiString(buf);
			}
			
			sRet = sRet.trim();
			
			oRet.moResult = new String(sRet.getBytes(), "UTF-8");
			oInputStream.close();		
			oRet.mbSuccess = true;
		} catch (UnsupportedEncodingException e) {
			oRet.msProblem = "The encoding was not correct/supported.";
			oRet.msPossibleSolution = "Check the encoding";
			oRet.moResult = "";
			oRet.mException = e;			
		} catch (FileNotFoundException e) {
			oRet.msProblem = "The file was not found.";
			oRet.msPossibleSolution = "Check the path";
			oRet.moResult = "";
			oRet.mException = e;
		} catch (IOException e) {
			oRet.msProblem = "The file could not be read.";
			oRet.msPossibleSolution = "Check permissions";
			oRet.moResult = "";
			oRet.mException = e;
		} catch (Exception e) {
			oRet.msProblem = "Unknown exception.";
			oRet.msPossibleSolution = "Check code";
			oRet.moResult = "";
			oRet.mException = e;
		}		  	
  	return(oRet);
  }
  
	
  
  
  /**
   * Returns stream contents as text.
   * 
   * @param aoFromStream stream whose contents are required
   * @return stream contents as text
   */
  public static MvException getStreamAsText(InputStream aoFromStream) {
    MvException oResult = new MvException();
    try {
    	java.util.Scanner s = new java.util.Scanner(aoFromStream).useDelimiter("\\A");
    	oResult.mbSuccess = true;
    	oResult.moResult = (s.hasNext())?s.next():"";
		} catch (Exception e) {
			oResult.mbSuccess = false;
			oResult.mException = e;
			oResult.msProblem = "Some other error";
		}
    
    return(oResult);
  }  
  

	/**
	 * Returns whether external storage (SD Card) is available.
	 * 
	 * @return true if card is available; false if otherwise
	 */
	public static boolean isSdCardPresent() {
    return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
  }

	
	/**
	 * Returns whether the path exists.
	 * 
	 * @param asPathname
	 *          file or directory that needs to be checked
	 * @return whether the path exists
	 */
	public static boolean isPathExists(String asPathname) {
		if (asPathname != null) {
		  File oFile = new File(asPathname);
		  return(oFile.exists());
		}
		return false;
	}
  
  
  /**
	 * Renames a file or a directory.
	 * 
	 * @param asOldPath Pathname of the file/directory that must be renamed/moved.
	 * @param asNewPath Pathname of the file/directory after it is renamed/moved.
	 * @return Whether the method was success or a failure
	 */
	public static MvException renameFile(String asOldPath, String asNewPath) {
		MvException oResult = new MvException();
		oResult.mbSuccess = false;
		File oOldFile = new File(asOldPath);
		File oNewFile = new File(asNewPath);
		
		if (!oOldFile.exists()) {
			oResult.msProblem = "The file does not exist.";
		} else if (oNewFile.exists()) {
			oResult.msProblem = "A file with new name already exists.";
		} else {
			if (oOldFile.renameTo(oNewFile)) {
				oResult.mbSuccess = true;
				oResult.moResult = oNewFile.getAbsolutePath();
			} else {
				oResult.msProblem = "The file could not be renamed because of a file system error.";
			}
		}
		
		return(oResult);
	}
	
	
	/**
	 * Saves specified text to specified file.
	 * 
	 * @param asTextToBeSaved
	 *          text to be saved
	 * @param asFileToBeSaved
	 *          file to be saved
	 * @return outcome of the operation
	 */
	public static MvException saveToTextFile(String asTextToBeSaved, String asFileToBeSaved) {
		MvException oRet = new MvException();
		
		FileOutputStream oFOS;
		try {
			oFOS = new FileOutputStream(asFileToBeSaved);
			oFOS.write(EncodingUtils.getBytes(asTextToBeSaved, "utf-8"));
			oFOS.flush();
			oFOS.close();			
			oRet.mbSuccess = true;
			oRet.moResult = asFileToBeSaved;
		} catch (FileNotFoundException e) {
			oRet.msProblem = "File could not be opened for writing";
		} catch (IOException e) {
			oRet.msProblem = "File could not be saved";
		}
		return(oRet);
	}
  
  
}
