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
 * @version 2015.02.13
 * 
 */
public class MvAsyncDownload extends AsyncTask<String, Integer, MvException> {
	public String mRemoteFileURL, mLocalFilePath, mLocalPath, msUserAgent;
	HttpURLConnection mURLConnection;
	public String msMimeType;
	public int miFileSize;
	int miBytesRead = 0;	
	public String msFilePathWithHeaderName = "", msFileNameHeader = "";	
	public boolean mbDoesHeadersHaveFileName = false, mbIsHTML = false;
	
	
	BufferedInputStream in = null;
	FileOutputStream of = null;
	
	private MvAsyncDownload() {
		super();
	}	
	
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
		mRemoteFileURL = asURL;
		mLocalFilePath = asFile;
		msUserAgent = asUserAgent;
		this.execute(mRemoteFileURL, mLocalFilePath);
	}
	
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
	  String sDownloadedFile, sDownloadPath;
	  mLocalPath = asPath;
	  mRemoteFileURL = asURL;
	  msUserAgent = asUserAgent;
	  
	  
	  if (abGuessFileName) {
	  	sDownloadedFile = URLUtil.guessFileName(asURL, null, asMimeType);
		  sDownloadPath = asPath + "/" + sDownloadedFile;	  	
	  } else {
	  	sDownloadPath = asPath;	  	
	  }
		
		mLocalFilePath = sDownloadPath;
		this.execute(mRemoteFileURL, mLocalFilePath);	  
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
		
		
		if (asLinks.length == 2) {
			try {
				oURL = new URL(asLinks[0]);
				mURLConnection = (HttpURLConnection) oURL.openConnection();
				
				if (msUserAgent.length() > "Mozilla".length()) {
					mURLConnection.setRequestProperty("User-Agent", msUserAgent);
					//MvMessages.logMessage("Mimicking " + msUserAgent);
					MvMessages.logMessage("Mimicking ");
				}
				
				if (mURLConnection.getHeaderField("content-disposition") != null) {
					msFileNameHeader = mURLConnection.getHeaderField("content-disposition");
					if (msFileNameHeader.indexOf("filename=") != -1) {
					  if (msFileNameHeader.length() > msFileNameHeader.indexOf("filename=")) {
					  	msFileNameHeader = msFileNameHeader.substring(msFileNameHeader.indexOf("filename=") + "filename=".length());
					  	if (msFileNameHeader.indexOf(";") > -1) {
					  		msFileNameHeader = msFileNameHeader.substring(0,msFileNameHeader.indexOf(";"));
					  	}
					  	msFileNameHeader = msFileNameHeader.replace("\"", "").replace("\\", "").replace("\\", "");
					  	msFilePathWithHeaderName = mLocalPath + File.separator + msFileNameHeader;
					  	mbDoesHeadersHaveFileName = true;
					  	MvMessages.logMessage("Filename is " + msFileNameHeader);
					  }
					}
				}
				
				mURLConnection.setConnectTimeout(4000);	
				mURLConnection.connect();	
				MvMessages.logMessage("Connection response is " + mURLConnection.getResponseCode());
				
				miFileSize = mURLConnection.getContentLength();
				MvMessages.logMessage("Headers " + mURLConnection.getHeaderFields().toString());
				MvMessages.logMessage(miFileSize + " to be downloaded;");
				
				if (mURLConnection.getHeaderField("content-type") != null) {
					if (mURLConnection.getHeaderField("content-type").contains("text/html")) {
						mbIsHTML = true;
					}
				}
				
				for (iTries = 1; iTries < 6; iTries++) {
					if (iTries > 1) {						
						mURLConnection.disconnect();
						mURLConnection = (HttpURLConnection) oURL.openConnection();
						mURLConnection.setRequestProperty("Range", "bytes=" + miBytesRead + "-");
						mURLConnection.connect();
					} else {
						if (mbDoesHeadersHaveFileName) {							
							of = new FileOutputStream(msFilePathWithHeaderName);
						} else {
							if (mbIsHTML) {
								of = new FileOutputStream(asLinks[1] + ".htm");
							} else {
								of = new FileOutputStream(asLinks[1]);
							}
						}
					}
					
					try {						
						in = new BufferedInputStream(mURLConnection.getInputStream());			
						do {
							if (isCancelled()) {
								oRet.mbSuccess = false;
								oRet.msProblem = "Download cancelled.";
								oRet.msPossibleSolution = "None required";
								break;
							}
							
							n = in.read(buf, 0, 1024);
							miBytesRead = miBytesRead + n;
							if (n != -1) {
								of.write(buf, 0, n);
								if (miFileSize > 0) {
								  publishProgress(miBytesRead);
								}
							} else {
								of.flush();
								in.close();
								of.close();
								msMimeType = mURLConnection.getContentType();
								oRet.mbSuccess = true;
							}
						} while(n != -1);		
						if (mbDoesHeadersHaveFileName) {
							oRet.moResult = msFilePathWithHeaderName;
						} else {
							if (mbIsHTML) {
								oRet.moResult = asLinks[1] + ".htm";	
							} else {
								oRet.moResult = asLinks[1];
							}
						}
						
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
			}catch (FileNotFoundException e) {
				oRet.mbSuccess = false;
				oRet.mException = e;
				oRet.msProblem = "The link (URL) is broken or missing.";
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
}
