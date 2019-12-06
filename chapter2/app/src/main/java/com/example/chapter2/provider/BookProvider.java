package com.example.chapter2.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.chapter2.DbOpenHelper;

public class BookProvider extends ContentProvider {
    private static final String TAG="BookProvider";

    public static final String AUTHORITY="com.example.chapter2.book.provider";

    public static final Uri BOOK_CONTENT_URI=Uri.parse("content://"+AUTHORITY+"/book");
    public static final Uri USER_CONTENT_URI=Uri.parse("content://"+AUTHORITY+"/user");

    public static final int BOOK_URI_CODE=0;
    public static final int USER_URI_CODE=1;

    private static final UriMatcher sUriMatcher=new UriMatcher(UriMatcher.NO_MATCH);

    static{
        sUriMatcher.addURI(AUTHORITY,"book",BOOK_URI_CODE);
        sUriMatcher.addURI(AUTHORITY,"user",USER_URI_CODE);
    }

    private Context mContext;
    private SQLiteDatabase mDb;

    public BookProvider() {
    }

    //使用UriMatcher，根据Uri获取tableName
    private String getTableName(Uri uri){
        String tableName=null;
        switch(sUriMatcher.match(uri)){
            case BOOK_URI_CODE:
                tableName= DbOpenHelper.BOOK_TABLE_NAME;
                break;
            case USER_URI_CODE:
                tableName=DbOpenHelper.USER_TABLE_NAME;
                break;
                default:
                    break;
        }
        return tableName;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
//        throw new UnsupportedOperationException("Not yet implemented");
        Log.d(TAG,"delete,current thread:"+Thread.currentThread().getName());
        String table=getTableName(uri);
        if(table==null){
            throw new IllegalArgumentException("Unsupported URI:"+uri);
        }
        int count=mDb.delete(table,selection,selectionArgs);
        if(count>0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return count;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
//        throw new UnsupportedOperationException("Not yet implemented");
        Log.d(TAG,"getType,current thread:"+Thread.currentThread().getName());
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
//        throw new UnsupportedOperationException("Not yet implemented");
        Log.d(TAG,"insert,current thread:"+Thread.currentThread().getName());
        String table=getTableName(uri);
        if(table==null){
            throw new IllegalArgumentException("Unsupported URI:"+uri);
        }

        mDb.insert(table,null,values);
        //插入和删除会引起数据源的变化，因而要notify
        mContext.getContentResolver().notifyChange(uri,null);
        return uri;
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        Log.d(TAG,"onCreate,current thread:"+Thread.currentThread().getName());
        mContext=getContext();
        initProviderData();
        return true;
    }

    private void initProviderData(){
        mDb=new DbOpenHelper(mContext).getWritableDatabase();
        mDb.execSQL("delete from "+DbOpenHelper.BOOK_TABLE_NAME);
        mDb.execSQL("delete from "+DbOpenHelper.USER_TABLE_NAME);
        mDb.execSQL("insert or ignore into book values(3,'Android');");
        mDb.execSQL("insert or ignore into book values(4,'Ios');");
        mDb.execSQL("insert or ignore into book values(5,'Html5')");
        mDb.execSQL("insert or ignore into user values(1,'jake',1);");
        mDb.execSQL("insert or ignore into user values(2,'jasmine',0)");
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
//        throw new UnsupportedOperationException("Not yet implemented");
        Log.d(TAG,"query,current thread:"+Thread.currentThread().getName());
        String table=getTableName(uri);
        if(table==null){
            throw new IllegalArgumentException("Unsupported URI:"+uri);
        }

        return mDb.query(table,projection,selection,selectionArgs,null,sortOrder,null);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
//        throw new UnsupportedOperationException("Not yet implemented");
        Log.d(TAG,"update,current thread:"+Thread.currentThread().getName());
        String table=getTableName(uri);
        if(table==null){
            throw new IllegalArgumentException("Unsupported URI:"+uri);
        }
        int row=mDb.update(table,values,selection,selectionArgs);
        if(row>0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return row;
    }
}
