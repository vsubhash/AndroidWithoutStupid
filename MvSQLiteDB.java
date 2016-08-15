/*
 * AndroidWithoutStupid Java Library
 * Created by V. Subhash 
 * http://www.VSubhash.com
 * Released as Public Domain Software in 2014
 */
package com.vsubhash.droid.androidwithoutstupid;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;

/**
 * This class provides methods for easy SQLite database operations.
 *
 */
public class MvSQLiteDB {
	String msLocation;
	/**
	 * Identifies whether the SQLite database file exists.
	 */
	public boolean mbIsAvailable = false;
	SQLiteDatabase moDB;
	 	
	/**
	 * Creates a new database file with specified tables. The file will be
	 * created in the application's data directory. 
	 * 
	 * @param aoCallingContext
	 *          application context of the activity or service
	 * @param asDatabase
	 *          name of the database
	 * @param aoTableCreationSqlList
	 *          list of table-definition SQL ("CREATE") statements
	 */
	public MvSQLiteDB(Context aoCallingContext, String asDatabase, ArrayList<String> aoTableCreationSqlList) {
		this(aoCallingContext.getApplicationInfo().dataDir + File.separator + asDatabase, aoTableCreationSqlList);
	}
	
	/**
	 * Initializes a new database instance with an already existing file.
	 * 
	 * @param asDatabase
	 *          pathname of the file
	 */
	public MvSQLiteDB(String asDatabase) {
		msLocation = asDatabase;
		this.mbIsAvailable = false;			
		
		if (MvFileIO.isPathExists(msLocation)) {
			try {
				moDB = SQLiteDatabase.openDatabase(msLocation, null,  SQLiteDatabase.OPEN_READONLY);
				moDB.close();
				mbIsAvailable = true;
			} catch (SQLiteException se) {
				mbIsAvailable = false;		
				MvMessages.logMessage("SQLite exception");
			}
		} else {
			MvMessages.logMessage("No database file found at " + asDatabase);
		}
	}
	
	/**
	 * Creates a new database with specified path and tables.
	 * 
	 * @param asDatabase
	 *          pathname of the database file
	 * @param aoTableCreationSqlList
	 *          list of table-definition SQL ("CREATE") statements
	 */
	public MvSQLiteDB(String asDatabase, ArrayList<String> aoTableCreationSqlList) {
		MvException oCreateTables;
		
		msLocation = asDatabase;
		if (MvFileIO.isPathExists(msLocation)) {
			mbIsAvailable = true;
			MvMessages.logMessage("Private database already exits at " + msLocation);
			
			for (String sSQL : aoTableCreationSqlList) {
				if (!isOpen()) {
					if (!openDB(true).mbSuccess) {
						MvMessages.logMessage("Database at " + msLocation + " cannot be opened.");
						break;
					}
				}
				oCreateTables = executeSQL(sSQL);
				if (!oCreateTables.mbSuccess) {
				  MvMessages.logMessage("Unable to create table with SQL [" + sSQL + "]");
				}									
			}
			this.closeDB();
		}	else {
			try {
				moDB = SQLiteDatabase.openOrCreateDatabase(msLocation, null);
				mbIsAvailable = true;
				for (String sSQL : aoTableCreationSqlList) {
					oCreateTables = executeSQL(sSQL);
					if (!oCreateTables.mbSuccess) {
						MvMessages.logMessage("Unable to create table with SQL [" + sSQL + "]");
					}
				}
				closeDB();
			} catch (SQLiteException se) {
				MvMessages.logMessage("Error: Database at " + asDatabase + " could not be found or created");
				mbIsAvailable = false;
			}		
		}		
	}
	
	
	/**
	 * Creates a database database based on a copy of a database in the
	 * application's assets directory.
	 * 
	 * @param aoCallingContext
	 *          context of the activity or service
	 * @param asDatabase
	 *          full path of the database file that needs to be created
	 * @param asFromAssetsDatabase
	 *          name of the database (from the application's assets directory)
	 *          that needs to be copied
	 */
	public MvSQLiteDB(Context aoCallingContext, String asDatabase, String asFromAssetsDatabase) {
		msLocation = asDatabase;
		 
		if (MvFileIO.isPathExists(msLocation)) {
			mbIsAvailable = true;
		}	else {
			MvException oCopyDatabase;
			try {
				InputStream oAssetsDbStream = aoCallingContext.getAssets().open(asFromAssetsDatabase);
				oCopyDatabase = MvFileIO.copyFile(oAssetsDbStream, msLocation);
				if (oCopyDatabase.mbSuccess) {
					MvMessages.logMessage("Private database copied");
					mbIsAvailable = true;
				} else {
					MvMessages.logMessage("Private database could not be copied");
					mbIsAvailable = false;
				}
			} catch (IOException e) {
				MvMessages.logMessage("Private database could not be found or copied");
				mbIsAvailable = false;
			}
		}		
	}
	
	/**
	 * Opens the database for reading and returns a cursor. IMPORTANT: Remember to
	 * close the cursor and database after use.
	 * 
	 * @param asSelectSQL
	 *          SQL query for which the cursor needs to be created
	 * @return the cursor in .moResult if .mbSuccess is true
	 */
	public MvException getCursorForSelectSQL(String asSelectSQL) {
		Cursor oCursor;
		MvException oResult = new MvException();
		
		if (openDB(false).mbSuccess) {
			oCursor = moDB.rawQuery(asSelectSQL, null);
			//MvMessages.logMessage("Executed SQL for cursor: " + asSelectSQL);
		  oResult.moResult = oCursor;
		  oResult.mbSuccess = true;
		  // this.closeDB(); // no need for this as cursor will get closed as well
		} else {
			oResult.mbSuccess = false;
		}
	  
		return(oResult);
	}
	
	/**
	 * Returns text result of a 1-row-by-1-column SQL query.
	 * 
	 * @param asSelectSQL
	 *          SQL query that returns a single row with a single column.
	 * @return text result of the query
	 */
	public MvException getSingleResultForSelectSQL(String asSelectSQL) {
		return(getSingleResultForSelectSQL(asSelectSQL, false));
	}
	
	/**
	 * Returns result of a 1-row-by-1-column SQL query.
	 * 
	 * @param asSelectSQL
	 *          SQL query that returns a single row with a single column.
	 * @return text or number result of the query in this.moResult
	 * @see android.database.sqlite.SQLiteStatement#simpleQueryForString()
	 * @see android.database.sqlite.SQLiteStatement#simpleQueryForLong()
	 */
	public MvException getSingleResultForSelectSQL(String asSelectSQL, boolean abIsNumberResult) {
		SQLiteStatement oSQL;
		MvException oResult = new MvException();
		
		if (openDB(false).mbSuccess) {
			try {
				oSQL = moDB.compileStatement(asSelectSQL);
			  if (abIsNumberResult) {
			  	oResult.moResult = oSQL.simpleQueryForLong();
			  } else {
			  	oResult.moResult = oSQL.simpleQueryForString();
			  }
			  oResult.mbSuccess = true;
			} catch (SQLiteDoneException sde) {
				MvMessages.logMessage("SQL did not return rows with : " + asSelectSQL);
				oResult.mException = sde;
			} catch (SQLException se) {
				MvMessages.logMessage("SQL error with : " + asSelectSQL);
				oResult.mException = se;
			}
			closeDB();
		} else {
			oResult.mbSuccess = false;
		}
		
		return(oResult);
	}	
	
	/**
	 * Executes the specified SQL statement on the database and automatically
	 * closes it. Use this method for statements that modify the database.
	 * 
	 * @param asSQL
	 *          SQL statement that needs to be executed
	 * @return success or failure of the execution
	 */
	public MvException executeSQL(String asSQL) {
		return(executeSQL(asSQL, true));
	}
	
	/**
	 * Executes the specified SQL statement on the database. Use this method for
	 * statements that modify the database.
	 * 
	 * @param asSQL
	 *          SQL statement that needs to be executed
	 * @param abAutoClose
	 *          whether the database needs to be closed after executing the SQL
	 *          statement
	 * @return success or failure of the execution
	 */
	public MvException executeSQL(String asSQL, boolean abAutoClose) {
		SQLiteStatement oSQL;
		MvException oResult = new MvException();
		
		if (openDB(true).mbSuccess) {
			try {
			  oSQL = moDB.compileStatement(asSQL); 
			  oSQL.execute();
				oResult.mbSuccess = true;
			} catch (SQLException se) {
				oResult.mException = se;
				MvMessages.logMessage("SQL error with " + asSQL);
			}
			if (abAutoClose) {
				closeDB();
			}
		}
	  
		return(oResult);
	}
	


	/**
	 * Opens the database either in readonly or writable state.
	 * 
	 * @param abWritable
	 *          true if the database needs to be writable; false if otherwise
	 * @return result of the operation
	 */
	public MvException openDB(boolean abWritable) {
		MvException oResult = new MvException();
		if (mbIsAvailable) {
			try {
				if (abWritable) {
					moDB = SQLiteDatabase.openDatabase(msLocation, null,  SQLiteDatabase.OPEN_READWRITE);	
				} else {
					moDB = SQLiteDatabase.openDatabase(msLocation, null, SQLiteDatabase.OPEN_READONLY|SQLiteDatabase.NO_LOCALIZED_COLLATORS);
				}
				oResult.mbSuccess = true;
			} catch (SQLiteException se) {
				oResult.mbSuccess = false;
				oResult.mException = se;
				oResult.msProblem = "Unable to open DB at " + this.msLocation;
			}
		} else {
			oResult.mbSuccess = false;
			oResult.msProblem = "DB is not available at location " + this.msLocation;
		}
		return(oResult);
	}	
	
	/**
	 * Returns whether the DB is open.
	 * 
	 * @return whether the DB is open
	 */
	public boolean isOpen() {
		if (mbIsAvailable) {
			if (moDB != null) {
				if (moDB.isOpen()) {
					return(true);
				}
			}
		}
		return(false);
	}

	/**
	 * Closes the database.
	 */
	public void closeDB() {
		if (mbIsAvailable) {
			if (isOpen()) {
				MvMessages.logMessage("DB closed");
				moDB.close();
			}
		}
	}		
	
}

