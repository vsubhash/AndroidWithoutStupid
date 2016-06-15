/*
 * AndroidWithoutStupid Java Library
 * Created by V. Subhash 
 * http://www.VSubhash.com
 * Released as Public Domain Software in 2014
 */

package com.vsubhash.droid.androidwithoutstupid;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * This class provides methods to retrieve and store app 
 * preferences. To use the class, first call the constructor
 * with the activity instance as the parameter. 
 * 
 * @author V. Subhash (<a
 *         href="http://www.vsubhash.com/">www.VSubhash.com</a>)
 * @version 2015.02.13
 */
public class MvPreferences {
  Activity mCallingActivity;
  Context mContext;
	SharedPreferences mPreferences;
  Editor mEditor;
  public Preference mLoadedPreference  = null;
  

	/**
	 * Creates an instance of this class.
	 * 
	 * @param aCallingActivity
	 *          activity where the preferences will be changed
	 */
  public MvPreferences(Activity aCallingActivity) {
  	mCallingActivity = aCallingActivity;
  	mContext = mCallingActivity.getApplicationContext();
  	reload();
  }
  
  public MvPreferences(Context aoApplicationContext) {
  	mContext = aoApplicationContext;   	
  	reload();
  }
  
  public MvPreferences(Activity aCallingActivity, int iXmlPreferences) {
  	this(aCallingActivity.getApplicationContext(), iXmlPreferences);
  	this.mCallingActivity = aCallingActivity;
  	this.mContext = this.mCallingActivity.getApplicationContext();  	
  }
  
  public MvPreferences(Context aoContext, int iXmlPreferences) {
  	this.mContext = aoContext;
  	PreferenceManager.setDefaultValues(
  					mContext,
  					iXmlPreferences, 
  					true);  	
    reload();  	
  }
  
  void reload() {
  	mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
  }
  
  /**
   * Finds preference for specified key.
   * @param asKey key for which the preference should be found
   * @return preference if successful; or null otherwise
   */
  public Preference findPreference(CharSequence asKey) {
  	if (mCallingActivity != null) {
  	  this.mLoadedPreference = ((PreferenceActivity) mCallingActivity).findPreference(asKey);
  	}
  	return(this.mLoadedPreference);
  }
  
  public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener aoListener) {
  	this.mPreferences.registerOnSharedPreferenceChangeListener(aoListener);
  }
  
  public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener aoListener) {
  	this.mPreferences.unregisterOnSharedPreferenceChangeListener(aoListener);
  }

  public boolean putInt(String asKey, int aInt) {
  	this.mEditor = mPreferences.edit();
  	this.mEditor.putInt(asKey, aInt);
  	return(this.mEditor.commit());	   
  }
  
  public int getIntent(String asKey, int aiDefaultValue) {
  	int iRet;
  	this.reload();
  	iRet = this.mPreferences.getInt(asKey, aiDefaultValue);  	
  	return(iRet);
  }
  
  /**
   * Sets specified boolean value with specified key.
   * 
   * @param asKey key to find the preference
   * @param bNewValue value of the preference
   * @return whether it succeeded
   * @see #getBoolean(String, boolean)
   */
  public boolean putBoolean(String asKey, boolean bNewValue) {
  	this.mEditor = mPreferences.edit();
  	this.mEditor.putBoolean(asKey, bNewValue);
  	return(this.mEditor.commit());
  } 
  
  /**
   * Returns boolean value of specified key;
   * @param asKey key to find the preference
   * @param bDefaultValue default if the method is unable to retrieve the original value
   * @return boolean value of specified key
   */
  public boolean getBoolean(String asKey, boolean bDefaultValue) {
  	boolean bRet;
  	this.reload();
  	bRet = this.mPreferences.getBoolean(asKey, bDefaultValue);  	
  	return(bRet);
  }
  
  
  public String getString(String asKey, String asDefaultValue) {
  	String sRet;
  	this.reload();
  	sRet = this.mPreferences.getString(asKey, asDefaultValue);
  	return(sRet);
  } 
  
  public boolean putString(String asKey, String asNewValue) {
  	this.mEditor = mPreferences.edit();
  	this.mEditor.putString(asKey, asNewValue);
  	return(this.mEditor.commit());
  }
  
  public long getLong(String asKey, long alDefaultValue) {
  	long lRet;
  	this.reload();
  	lRet = this.mPreferences.getLong(asKey, alDefaultValue);
  	return(lRet);
  }
  
  public boolean putLong(String asKey, long alNewValue) {
  	this.mEditor = mPreferences.edit();
  	this.mEditor.putLong(asKey, alNewValue);
  	return(this.mEditor.commit());  	
  }
	
}
