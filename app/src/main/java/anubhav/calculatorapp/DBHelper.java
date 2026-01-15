/**
 * Wire
 * Copyright (C) 2018 Wire Swiss GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package anubhav.calculatorapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Anubhav on 13-03-2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String database_Name="HISTORY.DB";
    private static final int database_Version=1;
    private static final String TAG="DATABASE OPERATIONS";
    private static final String table_Name="history";
    private static final String column1="calculator_name";
    private static final String column2="expression";
    private static final String create_Table="CREATE TABLE "+table_Name+"("+column1+" TEXT,"+column2+" TEXT);";

    SQLiteDatabase db;
    public DBHelper(Context context) {
        super(context,database_Name,null,database_Version);
        Log.i(TAG,"Database Created / Opened");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(create_Table);
        Log.i(TAG,"Table Created");
    }

    public void insert(String calcName,String expression)
    {
        db=getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(column1,calcName);
        contentValues.put(column2, expression);
        db.insert(table_Name, null, contentValues);
        db.close();
    }

    public ArrayList<String> showHistory(String calcName)
    {
        db=getReadableDatabase();
        Cursor cursor;
        ArrayList<String> list=new ArrayList<String>();
        String []selectionArgs={calcName};
        //cursor=db.query(table_Name,columns,column1+" LIKE ?",selectionArgs,null,null,null);
        cursor=db.rawQuery("select * from "+table_Name+" where "+column1+" = ?",selectionArgs);
        if(cursor.moveToFirst())
        {
            do
            {
                String expression=cursor.getString(1);
                list.add(expression);
            }while (cursor.moveToNext());
        }
        db.close();
        return list;
    }

    public void deleteRecords(String calcName)
    {
        db=getWritableDatabase();
        String value[]={calcName};
        int i=db.delete(table_Name, column1+"=?", value);
        db.close();
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
