AndroidWithoutStupid Java Library
=================================
I created AndroidWithoutStupid for use with my Subhash Browser app
  * http://www.vsubhash.com/article.asp?id=126&info=Subhash_Browser_ultimate_browser_and_RSS_feed_reader_app_for_Android
  * https://play.google.com/store/apps/details?id=com.vsubhash.droid.subhashbrowser

As this app was my first Android/Java project, I was surprised to find that I had to 
write several lines of code for even the simplest of tasks, say display a message. 
Searching for simpler code always lead me down the proverbial banana-monkey-forest 
route.

Hence, the objective of this library is to minimize the number of lines required to 
accomplish common and repetitive jobs.

The first three letters of all classes are uniquely named. And, so are most methods 
in this library. This will help greatly with the autocompletion feature of IDEs.

Many of the methods are static routines and do not require the developer to create an 
instance to call them. Others require an instance initialized with the current activity
passed as constructor parameter.

This software is released without copyright and is public-domain software.

The JAR file along with Java source, class and doc files are available at
  * http://www.vsubhash.com/article.asp?id=131&info=AndroidWithoutStupid_Java_Library

Here are a few introductory articles on CodeProject:
  * http://www.codeproject.com/Articles/801927/Using-AndroidWithoutStupid-Java-library-to-ease-th
  * http://www.codeproject.com/Tips/1037920/Tip-Accessing-Newer-Android-SDK-Version-APIs-Using
  

History
-------
  * 2019.01.19
  	* Fixed MvFile.getFileAsText() to return Unicode text instead of ASCII

