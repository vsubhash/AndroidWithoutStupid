/*
 * AndroidWithoutStupid Java Library
 * Created by V. Subhash 
 * http://www.VSubhash.com
 * Released as Public Domain Software in 2014
 */

package com.vsubhash.droid.androidwithoutstupid;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.webkit.URLUtil;

/**
 * This class provides general-purpose routines. Most methods can be called
 * statically while a few require an instance.
 * 
 * <blockquote><code><pre>
 MvGeneral.setClipBoardText("Hello, world!");
 
 ...
 
 MvGeneral mvg = new MvGeneral(MyActivity.this);
 mvg.playSound(R.raw.my_audio_file);
 * </pre></code></blockquote>
 * 
 * @author V. Subhash (<a href="http://www.VSubhash.com/">www.VSubhash.com</a>)
 * @version 2015.02.13
 *
 */
public class MvGeneral {
	ClipboardManager mClipboardManager;
	Context mApplicationContext;
	MvMediaPlayer mMvMediaPlayer = null;
	SharedPreferences moPrefs = null;
	public static Ringtone moRingTone;
	
	
	/**
	 * Specifies whether MediaPlayer routines such as {@link #playSound(int)}
	 * need to play audio. Use this field to mute all calls to such routines.
	 * 
	 */
	public boolean mIsSoundOn = true;
	
	public MvGeneral(Activity aoCallingActivity) {
		mApplicationContext = aoCallingActivity.getApplicationContext();
		mClipboardManager = 
				(ClipboardManager) mApplicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
		moPrefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
		
	}
	
	public MvGeneral(Context aoApplicationContext) {
		mApplicationContext = aoApplicationContext;
		mClipboardManager = 
				(ClipboardManager) mApplicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
    moPrefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
		
	}	
	
	
	public void stopRingTone() {
		if (moRingTone != null) {
			if (moRingTone.isPlaying()) {
				moRingTone.stop();
			}
		}
	}
	
	public boolean playRingTone() {
		if (moRingTone == null) {
			Uri oAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			moRingTone = RingtoneManager.getRingtone(mApplicationContext, oAlarmUri);
		}
		
		if (mIsSoundOn) {
			if (moRingTone != null) {
				if (moRingTone.isPlaying()) {
					moRingTone.stop();
				}
				moRingTone.play();
				return(true);
			}
		}
		return(false);
	}
	
	/**
	 * Plays a sound file specified by its resource ID.
	 * @param aiSoundResource resource ID of the sound file
	 */
	public void playSound(int aiSoundResource) {
		if (mIsSoundOn) {
			mMvMediaPlayer = new MvMediaPlayer(mApplicationContext, aiSoundResource);
		}
	}
	
	/**
	 * Stops the instance media player if it is playing.
	 */
	public void stopSound() {
		if (mMvMediaPlayer != null) {
			try {
				if (mMvMediaPlayer.mPlayer != null) {
				  if (mMvMediaPlayer.mPlayer.isPlaying()) {
				  	mMvMediaPlayer.mPlayer.pause();
				  	mMvMediaPlayer.mPlayer.stop();
				  	mMvMediaPlayer.mPlayer.release();
				  } 
				}
			} catch (Exception e) {
				
			}
		}
	}
	
	/**
	 * Launches an specified by its package name.
	 * 
	 * @param asPackageName name of the package
	 */
	public void launchApp(String asPackageName) {
	  try {
		  launchApp(asPackageName, false);
	  } catch (Exception e) {
	  	MvMessages.logMessage("Package not found: " + asPackageName);
	  }
	}
	

	/**
	 * Launches an app specified by its package name.
	 * 
	 * @param asPackageName package name of the app
	 * @param abThrow whether to throw {@link NameNotFoundException} if app is not found
	 * @throws NameNotFoundException if the package or app is not found
	 */
	public void launchApp(String asPackageName, boolean abThrow) throws NameNotFoundException  {
		Intent oLaunchIntent;
		PackageManager oPackageManager;
		
		if (asPackageName != null) {
			oPackageManager = mApplicationContext.getPackageManager();
			oLaunchIntent = oPackageManager.getLaunchIntentForPackage(asPackageName);
			if (oLaunchIntent != null) {
			  oLaunchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			  oLaunchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			  mApplicationContext.startActivity(oLaunchIntent);
			} else {
				if (abThrow) {
				  throw(new NameNotFoundException());
				}
			}
		}
	}
	
	/**
	 * Returns text in the clipboard memory.
	 * @return text in the clipboard (empty string if clipboard contains no data)
	 * @see #setClipboardText(String)
	 */
	public String getClipboardText() {
		String sReturn ="";
		if (mClipboardManager.getText() != null) {
			sReturn = mClipboardManager.getText().toString();
		}
		
		return(sReturn);
	}
	
	
	/**
	 * Copies specified text to clipboard memory.
	 * @param asTextToCopy text that needs to be copied to the clipboard
	 * @see #getClipboardText()
	 */
	public void setClipboardText(String asTextToCopy) {
		if (asTextToCopy != null) {
		  mClipboardManager.setText(asTextToCopy);
		} else {
			mClipboardManager.setText("");
		}
	}

  /**
   * Returns name of specified constant in specified class.
   * 
   * @param aoTargetClass class containing the constant
   * @param aiConstant value whose corresponding name is required
   * @return name of the constant
   */
  @SuppressWarnings("rawtypes")
	public static String getFieldName(Class aoTargetClass, int aiConstant) {
  	String sResult = "";
    
  	for (Field oField : aoTargetClass.getDeclaredFields()) {
  		try {
				if (oField.getInt(null) == aiConstant) {
					sResult = oField.getName();
				}
			} catch (IllegalArgumentException e) {
				MvMessages.logMessage("Error: getFieldName(Class, int) raised " + e.getClass().getCanonicalName());
				// e.printStackTrace();
			} catch (IllegalAccessException e) {
				MvMessages.logMessage("Error: getFieldName(Class, int) raised " + e.getClass().getCanonicalName());
				// e.printStackTrace();
			}
  	}
  	
    return(sResult);
  }
	
	
	
	/**
	 * Returns an underscore-delimited string containing specified date stamp.
	 * You could use this method, say, for time-stamping generated files.
	 * 
	 * @param dtInput date or time for which the date stamp needs to be created
	 * @return date stamp
	 */
	public static String getTimeStamp(Date dtInput) {
   	SimpleDateFormat oDateFormat;
   	String sTimestamp;
   	
   	oDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
  	
   	if (dtInput == null) {
  		dtInput = new Date();
  	} 
  	
  	sTimestamp = oDateFormat.format(dtInput);
  	
		return(sTimestamp);
	}
	
	public static Date getCurrentDateWithZeroedTime() {
		Date dtReturn = new Date();
		
		dtReturn.setHours(0);
		dtReturn.setMinutes(0);
		dtReturn.setSeconds(0);
		
		return(dtReturn);
	}
	
	public static String getCurrentDate() {
		Date dt = new Date();
   	String sReturn = "";
   	
   	DateFormat oDF = new SimpleDateFormat("dd MMMM yyyy");
  	
  	sReturn = oDF.format(dt);
  	
		return(sReturn);
	}
	
	public static String getCurrentTime() {
		Date dt = new Date();
   	String sReturn = "";
   	
   	DateFormat oDF = new SimpleDateFormat("HH:mm:ss");
  	
  	sReturn = oDF.format(dt);
  	
		return(sReturn);
	}
	
	public static String getCurrentDateTime() {
		Date dt = new Date();
   	String sReturn = "";
   	
   	DateFormat oDF = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");
  	
  	sReturn = oDF.format(dt);
  	
		return(sReturn);
	}
	
	
  /**
	 * Returns string array (that can be used in the WHERE IN clause of a SQL
	 * query) from an integer list.
	 * 
	 * @param olArray
	 *          integer list that needs to be converted
	 * @return string containing the WHERE IN array (including the opening and
	 *         closing brackets)
	 */
  public static String getSqlInArrayFromIntegerList(ArrayList<Integer> olArray) {
  	StringBuffer oRetBuff = new StringBuffer();
  	int i;
  	
  	if (olArray == null) {
  		oRetBuff.append("()");
  	} else if (olArray.size() == 0) {
  		oRetBuff.append("()");
  	} else if (olArray.size() == 1) {
  		oRetBuff.append("(").append(olArray.get(0)).append(")");
  	} else {
			oRetBuff.append("(");
			for (i = 0; i < olArray.size(); i++) {
				oRetBuff.append(olArray.get(i));
				if (i != olArray.size()-1) {
				  oRetBuff.append(",");
				}
			}
			oRetBuff.append(")");  			
		}
  	return(oRetBuff.toString());
  }
	
  public static String getSqlDate(Date adDateToBeConverted) {

  	StringBuffer sRet = new StringBuffer();
  	
  	sRet.append(adDateToBeConverted.getYear() + 1900);
  	
  	if (adDateToBeConverted.getMonth() < 10) {
  		sRet.append("-0");
  	} else {
  		sRet.append("-");
  	}
  	sRet.append(adDateToBeConverted.getMonth());
  	
  	if (adDateToBeConverted.getDate() < 10) {
  		sRet.append("-0");
  	} else {
  		sRet.append("-");
  	}  	
  	sRet.append(adDateToBeConverted.getDate());
  	
  	if (adDateToBeConverted.getHours() < 10) {
  		sRet.append(" 0");
  	} else {
  		sRet.append(" ");
  	}  	
  	sRet.append(adDateToBeConverted.getHours());
  	
  	if (adDateToBeConverted.getMinutes() < 10) {
  		sRet.append(":0");
  	} else {
  		sRet.append(":");
  	}  	
  	sRet.append(adDateToBeConverted.getMinutes());
  	
  	if (adDateToBeConverted.getSeconds() < 10) {
  		sRet.append(":0");
  	} else {
  		sRet.append(":");
  	}  	
  	sRet.append(adDateToBeConverted.getSeconds());

  	return(sRet.toString());
  }
  
	
	/**
	 * A fail-safe method to obtain a date from a string. Currently, very useful
	 * in parsing date strings encountered in RSS/ATOM XML feeds.
	 * 
	 * @param asDate
	 *          date string that needs to be parsed
	 * @return parsed date instance
	 */
	public static Date getDateFromString(String asDate) {
		Date dtReturn;		
	  DateFormat formatter;
	  
	  formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
	  try {
		  dtReturn = formatter.parse(asDate);
	  } catch (ParseException e) {
	  	formatter = new SimpleDateFormat("dd-MM-yy HH:mm");
	  	try {
			  dtReturn = formatter.parse(asDate);
		  } catch (ParseException e2) {
		  	formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		  	try {
				  dtReturn = formatter.parse(asDate);
			  } catch (ParseException e3) {
			  	formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			  	try {
					  dtReturn = formatter.parse(asDate);
				  } catch (ParseException e4) {
				  	formatter = new SimpleDateFormat("yyyy-MM-dd");
				  	try {
				  		dtReturn = formatter.parse(asDate);
				  	} catch (ParseException e5) {
				  		try {
					  		dtReturn = new Date(asDate); // leaving it to JRE's best guess
					  	} catch (IllegalArgumentException e6) {
					  		dtReturn = new Date();  // defaults to current date
					  	}	
				  	}				  					  	
				  }
			  }
		  }
	  }
	  
	  return(dtReturn);
	}
	
	
	/**
	 * Returns specified string after replacing all non-alphanumeric characters
	 * with underscore ('_') character. Makes it safe for use in URLs and file 
	 * names. 
	 * 
	 * @param asInput string that needs to converted
	 * @return transformed string
	 */
	public static String getEasyString(String asInput) {
		return(getEasyString(asInput, '_'));
	}
	
	/**
	 * Returns specified string after replacing all non-alphanumeric 
	 * and non-ANSI characters with specified character.  Makes it safe for 
	 * use in URLs and file names. 
	 * 
	 * @param asInput
	 *          string that needs to be transformed
	 * @param acPreferredSeparator
	 *          character with which all non-alphanumeric characters need to be
	 *          replaced with
	 * @return transformed string
	 */
	public static String getEasyString(String asInput, char acPreferredSeparator) {
		String sRet, sSeparator;
		if (asInput == null) {
			sRet = "";
		} else if (asInput.length() < 1) {
			sRet = "";
		} else {
			if (acPreferredSeparator == Character.forDigit(0, 10)) {
				sSeparator = "_";
			} else {
				sSeparator = Character.toString(acPreferredSeparator);
			}
			
			sRet = asInput;
//			if (abOnlyAnsi) {
				// sRet = sRet.replaceAll("[^A-Za-z0-9_\\-]", "_");
	//		} 
			
			sRet = sRet.replaceAll("[^A-Za-z0-9]", sSeparator);
			// sRet = sRet.replaceAll("[\\s\\c/:\\?\\=[:punct:]#\\|]+", sSeparator);
			
			sRet = sRet.replaceAll(sSeparator + sSeparator, sSeparator);
			sRet = sRet.replaceAll(sSeparator + sSeparator, sSeparator);

		}
		
		return(sRet);		
	}
	
	/**
	 * Returns a random nummber;
	 * @return a random number (below Integer.MAX_VALUE)
	 */
	public static int getRandomNumber() {
		return(getRandomNumber(Integer.MAX_VALUE));
	}
	
	/**
	 * Returns a random number below specified number.
	 * @param iLimit
	 * @return
	 */
	public static int getRandomNumber(int iLimit) {
		int iRet;
		Random rnd = new Random();
		if (iLimit < 4) {
			iRet = rnd.nextInt(4);
		} else {
			iRet = rnd.nextInt(iLimit);
		}
		return(iRet);
	}
	
/*	
	public ArrayList<Intent> getChooserIntentsExcept(Intent aoOriginalIntent, String asExclude) {
		ArrayList<Intent> oValidIntents = new ArrayList<Intent>();
		
		List<ResolveInfo> oMatchingActivities = mCallingActivity.getApplicationContext().getPackageManager().queryIntentActivities(aoOriginalIntent, PackageManager.MATCH_DEFAULT_ONLY);
		
		if (!oMatchingActivities.isEmpty()) {
			for (ResolveInfo oActivityInfo : oMatchingActivities) {
				if (!oActivityInfo.activityInfo.packageName.toLowerCase().contentEquals(asExclude.toLowerCase())) {
					try {
						oValidIntents.add(
								new Intent(mCallingActivity.getApplicationContext(),
										Class.forName(oActivityInfo.activityInfo.packageName)));
					} catch (ClassNotFoundException e) {
						//e.printStackTrace();
					}
				}
			}
		}
		
		return(oValidIntents);
	}
	*/
	
	
	/**
	 * Starts a synchronous download from specified URL and save it
	 * to specified file path. This method should not be called in 
	 * the UI thread. It is suitable for {@link IntentService} where
	 * the download needs to happen sequentially.
	 * 
	 * @param asURL address from which the file needs to be download
	 * @param asFile path to which the file needs to be saved
	 * @return download information
	 */
	public static MvException startSyncDownload(String asURL, String asFile) {
		return(startSyncDownload(asURL, asFile, false, ""));
	}

	/**
	 * Starts a synchronous download from specified URL and save it
	 * to specified file path. This method should not be called in 
	 * the UI thread. It is suitable for {@link IntentService} where
	 * the download needs to happen sequentially. The specified 
	 * user agent (browser or http client program) will be mimicked to 
	 * download the file.
	 * 
	 * @param asURL address from which the file needs to be download
	 * @param asFile path to which the file needs to be saved
	 * @param asUserAgent user agent string of the http client that needs to be mimicked
	 * @return download information
	 */	
	public static MvException startSyncDownload(String asURL, String asFile, String asUserAgent) {
		return(startSyncDownload(asURL, asFile, false, "", asUserAgent));
	}

	/**
	 * Starts a synchronous download from specified URL and save it
	 * to specified file path or directory. This method should not be called in 
	 * the UI thread. It is suitable for {@link IntentService} where
	 * the download needs to happen sequentially. if abGuessFileName is true,
	 * then the method tries to guess the download file name from the URL or from the 
	 * specfied mime type. If abGuessFileName is false, then the method is same as
	 * calling {@link #startSyncDownload(String, String) and other parameters will
	 * be ignored.
	 * 
	 * @param asURL address from which the file needs to be download
	 * @param asPath file or directory pathname (depending on abGuessFileName)
	 * @param abGuessFileName whether the file name should be guessed from asURL
	 * @param asMimeType mime type of the download
	 * @return download information
	 */	
	public static MvException startSyncDownload(String asURL, String asPath, boolean abGuessFileName, String asMimeType) {
	  return(startSyncDownload(asURL, asPath, abGuessFileName, asMimeType, ""));
	}
	
/**
	 * Starts a synchronous download from specified URL and save it
	 * to specified file path or directory. This method should not be called in 
	 * the UI thread. It is suitable for {@link IntentService} where
	 * the download needs to happen sequentially. if abGuessFileName is true,
	 * then the method tries to guess the download file name from the URL or from the 
	 * specfied mime type. If abGuessFileName is false, then the method is same as
	 * calling {@link #startSyncDownload(String, String) and other parameters will
	 * be ignored. The specified user agent (browser or http client program) will be 
	 * mimicked to download the file.
	 * 
	 * @param asURL address from which the file needs to be download
	 * @param asPath file or directory pathname (depending on abGuessFileName)
	 * @param abGuessFileName whether the file name should be guessed from asURL
	 * @param asMimeType mime type of the download
	 * @param asUserAgent user agent string of the http client that needs to be mimicked
	 * @return download information
	 */
	public static MvException startSyncDownload(String asURL, String asPath, boolean abGuessFileName, String asMimeType, String asUserAgent) {
		URL oURL;
		URLConnection mURLConnection;
		MvException oRet = new MvException();
		byte[] buf = new byte[1024];
		int n = 0;
		String sDownloadedFile, sDownloadPath;
		
	  if (abGuessFileName) {
	  	sDownloadedFile = URLUtil.guessFileName(asURL, null, asMimeType);
		  sDownloadPath = asPath + "/" + sDownloadedFile;	  	
	  } else {
	  	sDownloadPath = asPath;	  	
	  }		
		
		try {
			oURL = new URL(asURL);
			mURLConnection = oURL.openConnection();
			if (asUserAgent.length() > "Mozilla".length()) {
				mURLConnection.setRequestProperty("User-Agent", asUserAgent);
				//MvMessages.logMessage("Mimicking" + asUserAgent);
				MvMessages.logMessage("Mimicking");
			}
			mURLConnection.setConnectTimeout(5000);	
			mURLConnection.connect();	
			BufferedInputStream in = new BufferedInputStream(mURLConnection.getInputStream());
			
			try {
				FileOutputStream of = new FileOutputStream(asPath);
	
				do {
					n = in.read(buf, 0, 1024);
					if (n != -1) {
						of.write(buf, 0, n);
					} else {
						of.flush();
						in.close();
						of.close();							
					}
				} while(n != -1);					
				oRet.mbSuccess = true;
				oRet.moResult = sDownloadPath;
	    } catch (IOException e) {
			  oRet.mbSuccess = false;
				oRet.mException = e;
				oRet.msProblem = "There is a local storage issue.";
				oRet.msPossibleSolution = "A writable location is required.";
				e.printStackTrace();
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
			e.printStackTrace();
		} catch (FileNotFoundException e) {
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
		return oRet;
  }

  private class MvMediaPlayer implements MediaPlayer.OnCompletionListener {
  	MediaPlayer mPlayer;
  	
  	public MvMediaPlayer(Context aContext, int aiSoundResource) {
  		super();
  		mPlayer = MediaPlayer.create(aContext, aiSoundResource);
  		if (mPlayer != null) {
  			mPlayer.setOnCompletionListener(this);
  			mPlayer.start();
  		}
  	}
		
  	@Override
  	public void onCompletion(MediaPlayer mp) {
  		mPlayer.release();
  	}	 
  	
  	public MvMediaPlayer(Context aoContext, Uri aoAlarmUri) {
  		super();
  		mPlayer = MediaPlayer.create(aoContext, aoAlarmUri);
  		try {
  			if (mPlayer != null) {
	  			mPlayer.setDataSource(aoContext, aoAlarmUri);
	  			mPlayer.setOnCompletionListener(this);
	  			mPlayer.start();
  			}
  		} catch (Exception e) {
  			
  		}
  	}
  	
 }

}
