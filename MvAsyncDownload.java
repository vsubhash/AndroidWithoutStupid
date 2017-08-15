/*
 * AndroidWithoutStupid Java Library
 * Created by V. Subhash 
 * http://www.VSubhash.com
 * Released as Public Domain Software in 2014
 */

package com.vsubhash.droid.androidwithoutstupid;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.webkit.URLUtil;

/**
 * This class can be used to asynchronously download files from the Internet. To
 * download a file, call the constructor with the URL of the file and the full
 * pathname to which the file needs to be saved to. The constructor automatically 
 * starts the download. As this class extends {@link android.os.AsyncTask}, 
 * override its methods to handle download events.
<blockquote><code><pre>
MvAsyncDownload dl = 
   new MvAsyncDownload(
      "http://www.example.com/rss.xml", 
      "/mnt/sdcard/rss.xml") { 
  {@Override}Override 
  protected void onPostExecute(MvException result) { 
    if (result.mbSuccess) { 
      MvMessages.showMessage(this, "Downloaded to " + result.moResult.toString()); 
    } else { 
      MvMessages.showMessage(this, "Failed " + result.msProblem );
    } 
    super.onPostExecute(result); 
  } 
}; 
</pre></code></blockquote>
 * @see android.os.AsyncTask
 * @author V. Subhash (<a href="http://www.VSubhash.com/">www.VSubhash.com</a>)
 * @version 2017.08.15
 * 
 */
public class MvAsyncDownload extends AsyncTask<String, Long, MvException> {
	String msFilename, msFilePathname, msFileDirectory, msMimeType;
	String msRemoteUrl, msHeaderFileName, msUserAgent;
	HttpURLConnection moURLConnection;
	long mlDownloadSize;
	long mlBytesDownloaded = 0;
	boolean mbGuessFileName = false;
	
	BufferedInputStream in = null;
	FileOutputStream of = null;
	
	/**
	 * Returns bytes that have been downloaded.
	 * 
	 * @return number of downloaded bytes
	 */
	public long getDownloadedSize() { return(mlBytesDownloaded); }
	/**
	 * Returns byte size of the download (if known).
	 * @return byte size of the download
	 */
	public long getDownloadSize() { return(mlDownloadSize); }
	/**
	 * Returns name of the download file (if known).
	 * 
	 * @return name of the download file
	 */
	public String getDownloadFilename() { return(msFilename); }
	/**
	 * Returns pathname of the download file.
	 * 
	 * @return pathname of the download file
	 */
	public String getDownloadFilePathname() { return(msFilePathname); }
	/**
	 * Returns mimetype of the download.
	 * @return mimetype of the download
	 */
	public String getMimeType() { return(msMimeType); }
	
	private MvAsyncDownload() {
		super();
	}	
	
	/**
	 * Asynchronously downloads the specified URL to specified file.
	 * 
	 * @param asURL download URL
	 * @param asFile pathname of local file to which the download needs to be saved
	 */
	public MvAsyncDownload(String asURL, String asFile) {
	  this(asURL, asFile, "");
	}
	
	/**
	 * Asynchronously downloads from specified URL and save the download to
	 * specified file. The specified user agent (or http client) will be 
	 * mimicked to download the file.
	 * 
	 * @param asURL
	 *          download URL
	 * @param asFile
	 *          pathname of local file to which the download needs to be saved
	 * @param asUserAgent
	 *          user agent string of the http client that needs to be mimicked          
	 */
	public MvAsyncDownload(String asURL, String asFile, String asUserAgent) {
	  this();
		msRemoteUrl = asURL;
		msFilePathname = asFile;
		msUserAgent = asUserAgent;
		mbGuessFileName = false;
		this.execute(msRemoteUrl, msFilePathname);
	}
	
	/**
	 * Asynchronously downloads specified URL to specified directory and (if
	 * specified) guess the file name based on the specified mimetype.
	 * 
	 * @param asURL
	 *          download URL
	 * @param asPath
	 *          directory where the file needs to be saved
	 * @param abGuessFileName
	 *          whether to guess the file name
	 * @param asMimeType
	 *          mimetype of the file with which the filename needs to be guessed
	 */
	public MvAsyncDownload(String asURL, String asPath, boolean abGuessFileName, String asMimeType) {
	  this(asURL, asPath, abGuessFileName, asMimeType, "");
	}
	
	/**
	 * Asynchronously downloads from specified URL, guesses the filename if
	 * specified, and saves the download to specfied directory.
	 * 
	 * @param asURL
	 *          download URL
	 * @param asPath
	 *          if abGuessFileName is true, asPath represents the pathname of the
	 *          directory to which the download needs to be saved; if
	 *          abGuessFileName is false, asPath represents the file name to which
	 *          the download needs to be saved
	 * @param abGuessFileName
	 *          whether the filename needs to be guessed
	 * @param asMimeType
	 *          mimetype of the download (used only for guessing the filename)
	 * @param asUserAgent
	 *          user agent string of the http client that needs to be mimicked          
	 */
	public MvAsyncDownload(String asURL, String asPath, boolean abGuessFileName, String asMimeType, String asUserAgent) {
	  this();
  	  
	  msRemoteUrl = asURL;
	  msUserAgent = asUserAgent;
	  mbGuessFileName = abGuessFileName;
	  
	  if (mbGuessFileName) {
	  	msFileDirectory = asPath;
	  	String sGuessedFilename = URLUtil.guessFileName(asURL, null, asMimeType);
	  	if (sGuessedFilename.contains(".bin")) {
	  		msFilename = "unknown-download-" + MvGeneral.getRandomNumber() + ".bin"; 	
	  	} else {
	  		msFilename = sGuessedFilename;
	  	}
	  	msFilePathname = msFileDirectory + File.separator + msFilename;
	  } else {
	  	msFileDirectory =	MvFileIO.getParentDirectoryFromPath(asPath);	
	  	msFilePathname = asPath;
	  }
		
		MvMessages.logMessage("Set to download " + msRemoteUrl + "\n\tto " + msFilePathname);
		this.execute(msRemoteUrl, msFilePathname);	  
	}	
	  
	@Override
	protected void onCancelled() {
		try {
			in.close();
			of.flush();
			of.close();
		} catch (IOException ioe) {
			MvMessages.logMessage("IO Exception closing streams.");
		} catch (Exception e) {
			MvMessages.logMessage("Other exception cancelling download.");
		}
		
		super.onCancelled();
	}
	
	@Override
	protected MvException doInBackground(String... asLinks) {
		URL oURL;
		MvException oRet = new MvException();
		byte[] buf = new byte[1024];
		int n = 0, iTries = 0;
		
		if (asLinks.length == 2) { // url, file
			try {
				oURL = new URL(msRemoteUrl);
				moURLConnection = (HttpURLConnection) oURL.openConnection();
				//HttpURLConnection.setFollowRedirects(true);
				if (msUserAgent.length() > "Wget 1".length()) {
				  moURLConnection.setRequestProperty("User-Agent", msUserAgent);
					// MvMessages.logMessage("Mimicking " + msUserAgent);
					//MvMessages.logMessage("Mimicking useragent");
				}
				
				mlDownloadSize = moURLConnection.getContentLength();
				if (mlDownloadSize > -1) {
					MvMessages.logMessage("File size is " + mlDownloadSize);
				} else {
					MvMessages.logMessage("File size is unknown.");
				}

				if (moURLConnection.getHeaderFields() != null) {
					MvMessages.logMessage("Headers are: " + moURLConnection.getHeaderFields().toString());
					
					String sDispositionHeader = moURLConnection.getHeaderField("content-disposition");

					if (sDispositionHeader != null) {
						if (sDispositionHeader.length() > 0) {
							msHeaderFileName = getFileNameFromHeader(sDispositionHeader);							
							if (msHeaderFileName.length() > 0) {
								MvMessages.logMessage("Changed output file from " + msFilePathname);
								msFilePathname = msFileDirectory + File.separator + MvFileIO.getSafeFileNameFor(msHeaderFileName);
						  	MvMessages.logMessage("\tto " + msFilePathname);
						  	msFilename = msHeaderFileName;
						  }
						}
					}
					
					mlBytesDownloaded = MvFileIO.getFileSize(msFilePathname);
					MvMessages.logMessage("Existing file size is "  + mlBytesDownloaded);
					
					if ((mlDownloadSize > 0) && (mlBytesDownloaded == mlDownloadSize)) {
					  MvMessages.logMessage("File already downloaded");
					  oRet.moResult = msFilePathname;
					  msMimeType = moURLConnection.getContentType();
					  oRet.mbSuccess = true;
					  return(oRet);
					} else if ((mlDownloadSize > 0) && (mlBytesDownloaded > 0) && (mlBytesDownloaded < mlDownloadSize)) {
						moURLConnection.disconnect();
						moURLConnection = (HttpURLConnection) oURL.openConnection();
						MvMessages.logMessage("Resuming download from " + mlBytesDownloaded);
						moURLConnection.setRequestProperty("Range", "bytes=" + mlBytesDownloaded + "-");
					}
					
					moURLConnection.setConnectTimeout(4000);				
					moURLConnection.connect();	
					MvMessages.logMessage("Connection response is " + moURLConnection.getResponseCode());
					
					for (iTries = 1; iTries < 6; iTries++) {
						if (iTries > 1) {						
							moURLConnection.disconnect();
							moURLConnection = (HttpURLConnection) oURL.openConnection();
							if (mlDownloadSize > -1) {
								MvMessages.logMessage("Resuming download from " + mlBytesDownloaded);
							  moURLConnection.setRequestProperty("Range", "bytes=" + mlBytesDownloaded + "-");
							}  
							moURLConnection.connect();
						} else {		
							//MvMessages.logMessage(msFilePathname + " ~ \n" + asLinks[0]);
							if ((mlDownloadSize > 0) && (mlBytesDownloaded > 0) && (mlBytesDownloaded < mlDownloadSize)) {
								MvMessages.logMessage("Using existing download file.");
							  of = new FileOutputStream(msFilePathname, true);
							} else {
								MvMessages.logMessage("Using new download file.");
								of = new FileOutputStream(msFilePathname, false);
							}
						}
						
						try {						
							in = new BufferedInputStream(moURLConnection.getInputStream());			
							do {
								if (isCancelled()) {
									oRet.mbSuccess = false;
									oRet.msProblem = "Download cancelled.";
									oRet.msPossibleSolution = "None required";
									iTries =6;
									of.flush();
									in.close();
									of.close();			
									MvMessages.logMessage("Download cancelled");
									break;
								}
								
								n = in.read(buf, 0, 1024);
								mlBytesDownloaded = mlBytesDownloaded + n;
								if (n > 0) {
									of.write(buf, 0, n);
									if (msFilename == null) {
										msFilename = MvFileIO.getFileNameFromPath(msFilePathname);
									}
									 publishProgress(mlBytesDownloaded);
								} else {
									of.flush();
									in.close();
									of.close();
									msMimeType = moURLConnection.getContentType();
									MvMessages.logMessage("Mime = " + msMimeType);
									
									String sNewExtension, sNewFileName;
									if ((mbGuessFileName) && (msFilename.endsWith(".bin"))) {
										if (msMimeType.contains("text/html")) {
											sNewExtension = ".htm";
										} else if (msMimeType.contains("text/plain")) {
											sNewExtension = ".txt";
										} else if (msMimeType.contains("application/pdf")) {
											sNewExtension = ".pdf";
										} else if (msMimeType.contains("application/zip")) {
											sNewExtension = ".zip";
										} else if (msMimeType.contains("audio/mpeg")) {
											sNewExtension = ".mp3";
										} else if (msMimeType.contains("image/png")) {
											sNewExtension = ".png";
										} else if (msMimeType.contains("image/jpeg")) {
											sNewExtension = ".jpg";
										} else if (msMimeType.contains("image/gif")) {
											sNewExtension = ".gif";
										} else if (msMimeType.contains("video/mp4")) {
											sNewExtension = ".mp4";
										} else if (msMimeType.contains("video/mpeg")) {
											sNewExtension = ".mpg";
										} else {
											sNewExtension = ".bin";
										}
										
										sNewFileName = MvFileIO.getFileNameWithoutExtension(msFilePathname) + sNewExtension;
										sNewFileName = MvFileIO.getSafeFileNameFor(sNewFileName);
										sNewFileName = MvFileIO.getIncrementedFileName(msFileDirectory + File.separator + sNewFileName);
										
										MvException oRename = MvFileIO.renameFile(msFilePathname, msFileDirectory + File.separator + sNewFileName);		
										if (oRename.mbSuccess) {
											msFilename = sNewFileName;
											msFilePathname = msFileDirectory + File.separator + sNewFileName;
										} else {
											MvMessages.logMessage("Unable to rename file as " + sNewFileName + " because of " + oRename.msProblem);
										}
									}
									oRet.mbSuccess = true;
								}
							} while(n != -1);		
							
							oRet.moResult = msFilePathname;
													
							break;
					  } catch (IOException e) {
						  oRet.mbSuccess = false;
							oRet.mException = e;
							oRet.msProblem = "Download failed. Tries: " + iTries;
							oRet.msPossibleSolution = "A better download URL or network conditions.";
							MvMessages.logMessage(oRet.msProblem);
							e.printStackTrace();					
						}
					}
				} else {
					MvMessages.logMessage("Headers are null");
					oRet.mbSuccess = false;
					oRet.mException = null;
					oRet.msProblem = "Headers are null";
					oRet.msPossibleSolution = "A valid URL (link) is required.";
				}
			} catch (MalformedURLException e) {
				oRet.mbSuccess = false;
				oRet.mException = e;
				oRet.msProblem = "This is an invalid URL (link).";
				oRet.msPossibleSolution = "A valid URL (link) is required.";
				e.printStackTrace();
			} catch (UnknownHostException e) {
				oRet.mbSuccess = false;
				oRet.mException = e;
				oRet.msProblem = "There is no Internet connection or the website does not exist.";
				oRet.msPossibleSolution = "An working Internet connection or a valid website address is required.";
				MvMessages.logMessage("Have you added INTERNET permission?");
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				oRet.mbSuccess = false;
				oRet.mException = e;
				oRet.msProblem = "The link (URL) or the file path is invalid." + msFilePathname;
				oRet.msPossibleSolution = "An existing link (URL) is required.";
				e.printStackTrace();
			} catch (IOException e) {
				oRet.mbSuccess = false;
				oRet.mException = e;
				oRet.msProblem = "There is no network connection.";
				oRet.msPossibleSolution = "A good connection to the network is required.";
				e.printStackTrace();
			}
		}		
		return oRet;
  }
	
	
	String getFileNameFromHeader(String asHeader) {
		String sFileName, sReturn = "";
		
		if (asHeader.indexOf("filename=") > -1) {
		  if (asHeader.length() > asHeader.indexOf("filename=")) {
		  	sFileName = asHeader.substring(asHeader.indexOf("filename=") + "filename=".length());
		  	if (sFileName.indexOf(";") > -1) {
		  		sFileName = sFileName.substring(0,sFileName.indexOf(";"));
		  	}
		  	sReturn = sFileName.replace("\"", "").replace("\\", "").replace("\\", "");
		  }
		}
		return(sReturn);
	}
	
	
}
