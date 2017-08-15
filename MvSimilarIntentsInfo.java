/*
 * AndroidWithoutStupid Java Library
 * Created by V. Subhash 
 * http://www.VSubhash.com
 * Released as Public Domain Software in 2014
 */
package com.vsubhash.droid.androidwithoutstupid;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

/**
 * This class can be used to find activities in other apps that can handle a
 * specified Intent. This class excludes the activites in the current app, which
 * is an improvement over {@link Intent#createChooser(Intent, CharSequence)}
 * method. As it finds those activities, it also loads up on their package
 * names, class names, icons and other information.
 * 
 * @author V. Subhash (<a href="http://www.VSubhash.com/">www.VSubhash.com</a>)
 * @version 2015.02.13
 */
public class MvSimilarIntentsInfo {
	
	Activity mCallingActivity;
	/**
	 * Information about compatible activities.
	 */
	public ArrayList<ResolveInfo> mResolveInfoList;
	/**
	 * Names of the activities.
	 */
	public ArrayList<String> mAppNameList;
	/**
	 * Package names of the activities.
	 */
	public ArrayList<String> mPackageNameList;
	/**
	 * Class names of the activities.
	 */
	public ArrayList<String> mActivityClassNameList;
	/**
	 * Icons used by the apps.
	 */
	public ArrayList<Drawable> mAppDrawableList;
		
	/**
	 * Constructs an instance of this class with specified activity.
	 * 
	 * @param aoCallingActivity
	 *          activity from which the methods of this class will be called.
	 */
	public MvSimilarIntentsInfo(Activity aoCallingActivity) {
		mCallingActivity = aoCallingActivity;
		mResolveInfoList = new ArrayList<ResolveInfo>();
		mAppNameList = new ArrayList<String>();
		mPackageNameList = new ArrayList<String>();
		mAppDrawableList = new ArrayList<Drawable>();
		mActivityClassNameList = new ArrayList<String>();
	}
	
	/**
	 * Returns whether activities that can handle specified Intent, except whose
	 * class names that contain a specified name, exist in the device. Also loads
	 * up information about those activities and their packages.
	 * 
	 * @param aoOriginalIntent
	 *          Intent whose likes of which need to be found
	 * @param asExcludeThisClassName
	 *          name that should not be contained in the class names of the
	 *          activities
	 * @return whether similar activities exist on the device
	 */
	public MvException getSimilarIntentsInfo(Intent aoOriginalIntent, String asExcludeThisClassName) {
		MvException oResult = new MvException();		
		
		List<ResolveInfo> oOtherIntentsInfoList;
		PackageManager oPM = mCallingActivity.getPackageManager();
		
		oResult.moResult = this;
		oOtherIntentsInfoList = mCallingActivity.getPackageManager().queryIntentActivities(
				aoOriginalIntent, PackageManager.GET_ACTIVITIES);
		
		if (!oOtherIntentsInfoList.isEmpty()) {
			for (ResolveInfo oResolveInfo: oOtherIntentsInfoList) {
				//MvMessages.logMessage("Activity: " + oResolveInfo.activityInfo.name);
				if (oResolveInfo.activityInfo.name.toLowerCase().contains(asExcludeThisClassName.toLowerCase())) {				
					MvMessages.logMessage("Excluding: " + oResolveInfo.activityInfo.name);
				} else {					
					this.mResolveInfoList.add(oResolveInfo);
					this.mAppNameList.add(oPM.getApplicationLabel(oResolveInfo.activityInfo.applicationInfo).toString());
					this.mPackageNameList.add(oResolveInfo.activityInfo.packageName);
					this.mActivityClassNameList.add(oResolveInfo.activityInfo.name);
					this.mAppDrawableList.add(oResolveInfo.loadIcon(oPM));
				}
			}
		}
		
		if (!this.mResolveInfoList.isEmpty()) {

			
			oResult.mbSuccess = true;
		}
		
		return(oResult);		
	}

	
}
