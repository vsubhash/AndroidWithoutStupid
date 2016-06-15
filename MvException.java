/*
 * AndroidWithoutStupid Java Library
 * Created by V. Subhash 
 * http://www.VSubhash.com
 * Released as Public Domain Software in 2014
 */

package com.vsubhash.droid.androidwithoutstupid;

/**
 * This class wraps an {@link Exception Java Exception} so that it
 * can be passed around, mostly as the return value of a function, as in
 * in traditional C programming. Why do you need this? Well, most exception
 * messages are cryptic and reveal no real or useful information about the 
 * problem. Use members of this class to add more usefulness to error details. 
 * 
 * @author V. Subhash (<a href="http://www.VSubhash.com/">www.VSubhash.com</a>)
 * @version 2016.02.03
 *
 */
public class MvException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Whether the operation was successful. Set it to true
	 * if no exception was raised or if the operation yielded
	 * a result.
	 */
	public boolean mbSuccess = false;
	
	/**
	 * Actual result of the operation. Set it to the object that
	 * you want to be returned. The calling routine should be 
	 * aware of the class name of this object and cast it accordingly.
	 */
	public Object moResult = null;
	/**
	 * Error information. Set it to the exception that caused an error.
	 */
	public Exception mException;	
	/**
	 * Specifies what caused the problem. Set it to a description of the
	 * error in human-friendly language.
	 */
	public String msProblem = "";
	/**
	 * Specifies what can be done to fix the problem. Set it to a possible
	 * solution in human-friendly language.
	 */
	public String msPossibleSolution = "";
	
	/**
	 * Constructs a new instance of the class.
	 */
	public MvException() {
		super();
		mException = new Exception("Exception not specified yet");
	}
	
	
	
}
