package com.example.googlenewsapi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.googlenewsapi.Model.Article;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

//Database Handler for storing, fetching and deleting News articles
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "SQL ArticleManager";
    private static final String TABLE_NAME = "Articles1";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_DATE = "date";
    private static final String KEY_URL = "url";
    private static final String KEY_IMAGE_URL = "image_url";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String createArticlestableQuery = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_TITLE + " TEXT," + KEY_DESCRIPTION + " TEXT,"
                + KEY_URL + " TEXT," + KEY_IMAGE_URL + " TEXT," + KEY_DATE + " TEXT" + ")";

        sqLiteDatabase.execSQL(createArticlestableQuery);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    void addArticle(Article article) {

        List<Article> tempList = getAllArticles();

        for (int i = 0; i < tempList.size(); i++) {
            if (tempList.get(i).getTitle().equals(article.getTitle())) {
                return;
            }
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, article.getTitle()); // Article Phone
        values.put(KEY_DESCRIPTION, article.getDescription()); // Article Name
        values.put(KEY_URL, article.getUrl()); // Article Phone
        values.put(KEY_IMAGE_URL, article.getUrlToImage()); // Article Phone
        values.put(KEY_DATE, article.getPublishedAt()); // Article Phone

        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    // code to get the single article
    Article getArticle(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[]{KEY_TITLE,
                        KEY_DESCRIPTION, KEY_DATE}, KEY_TITLE + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Article article = new Article((cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
        // return article
        return article;
    }

    // code to get all articles in a list view
    public List<Article> getAllArticles() {
        List<Article> articleList = new ArrayList<Article>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + KEY_DESCRIPTION;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Article article = new Article();
                article.setTitle((cursor.getString(0)));
                article.setDescription(cursor.getString(1));
                article.setUrl(cursor.getString(2));
                article.setUrlToImage(cursor.getString(3));
                article.setPublishedAt(cursor.getString(4));
                // Adding article to list
                articleList.add(article);
            } while (cursor.moveToNext());
        }

        // return article list
        return articleList;
    }

    // code to update the single article
//    public int updateArticle(Article article) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(KEY_DESCRIPTION, article.getArticleName());
//        values.put(KEY_DATE, article.getArticleNumber());
//
//        // updating row
//        return db.update(TABLE_NAME, values, KEY_TITLE + " = ?",
//                new String[] { String.valueOf(article.getArticleId()) });
//    }

    // Deleting single article
    public void deleteArticle(Article article) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_TITLE + " = ?",
                new String[]{String.valueOf(article.getTitle())});
        db.close();
    }

    // Getting articles Count
    public int getArticlesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

}