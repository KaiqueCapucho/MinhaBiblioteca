package com.example.bibliotheca;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class BDHelper extends SQLiteOpenHelper {
    private static final String NOMEBD = "Bibliotheca.db";
    private static final int VERBD = 1;
    private final Context context;

    public BDHelper(Context context){
        super(context, NOMEBD, null, VERBD);
        this.context = context;
        try {
            createDatabase();
        } catch (IOException e) {
            Log.e("DBCopyError", "Erro ao copiar o banco de dados", e);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) { }

    private void createDatabase() throws IOException {
        File dbFile = context.getDatabasePath(NOMEBD);
        if (!dbFile.exists()) {

            SQLiteDatabase db = getReadableDatabase();
            db.close();
            copyDatabase(dbFile.getPath());
        }
    }


    private void copyDatabase(String outputName) throws IOException {
        InputStream inputStream = context.getAssets().open(NOMEBD);
        OutputStream outputStream = new FileOutputStream(outputName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }


    @Override
    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }

    public ArrayList<String> getColuna(SQLiteDatabase bd, String tab, String col){
        ArrayList<String> array = new ArrayList<>();
        try (Cursor cur = bd.rawQuery("SELECT " + col +" FROM " + tab + " ORDER BY " +col+ " ASC", null)) {
            if (cur.moveToFirst()) {
                do {
                    int index = cur.getColumnIndex(col);
                    if (index >= 0) {
                        String nomeCat = cur.getString(index);
                        array.add(nomeCat);
                    }
                } while (cur.moveToNext());
            }
        }
        return array;
    }
    public ArrayList<String> getLivros(SQLiteDatabase bd){
        ArrayList<String> array = new ArrayList<>();
        try (Cursor cur = bd.rawQuery("SELECT _id, titulo_original, titulo_pt_br FROM Livros", null)) {
            if (cur.moveToFirst()) {
                do {
                    int index = cur.getColumnIndex("titulo_original");
                    if (index >= 0) {
                        String nomeCat = cur.getString(index);
                        if(nomeCat!=null){
                            array.add(nomeCat);
                        }
                    }
                    index = cur.getColumnIndex("titulo_pt_br");
                    if (index >= 0) {
                        String nomeCat = cur.getString(index);
                        if(nomeCat!=null){
                            array.add(nomeCat);
                        }
                    }
                } while (cur.moveToNext());
            }
        }
        return array;
    }

}
