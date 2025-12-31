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
    private Context context;


    public BDHelper(Context context){
        super(context, NOMEBD, null, VERBD);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        copiarBD();
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) { }

    private void copiarBD() {
        File bd = new File(context.getDatabasePath(NOMEBD).getAbsolutePath());
        if (!bd.exists()) {
            try (InputStream inp = context.getAssets().open(NOMEBD);
                 OutputStream out = new FileOutputStream(bd)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inp.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("DBCopyError", "Erro ao copiar o banco de dados", e);
            }
        }
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }


    public ArrayList<String> getCategorias(SQLiteDatabase bd){
        ArrayList<String> arrayCat = new ArrayList<>();
        try (Cursor cur = bd.rawQuery("SELECT categoria FROM Categorias ORDER BY categoria ASC", null)) {
            if (cur.moveToFirst()) {
                do {
                    int index = cur.getColumnIndex("categoria");
                    if (index >= 0) {
                        arrayCat.add(cur.getString(index));
                    }
                } while (cur.moveToNext());
            }
        }
        return arrayCat;
    }
    public ArrayList<String> getAutores(SQLiteDatabase bd){
        ArrayList<String> arrayAutor = new ArrayList<>();
        try (Cursor cur = bd.rawQuery("SELECT nome FROM Autores ORDER BY nome ASC", null)) {
            if (cur.moveToFirst()) {
                do {
                    int index = cur.getColumnIndex("nome");
                    if (index >= 0) {
                        String nomeCat = cur.getString(index);
                        arrayAutor.add(nomeCat);
                    }
                } while (cur.moveToNext());
            }
        }
        return arrayAutor;
    }

    public ArrayList<String> getTemas(SQLiteDatabase bd) {
        ArrayList<String> arrayTemas = new ArrayList<>();
        try (Cursor cur = bd.rawQuery("SELECT tema FROM Temas ORDER BY tema ASC", null)) {
            if (cur.moveToFirst()) {
                do {
                    int index = cur.getColumnIndex("tema");
                    if (index >= 0) {
                        String nomeCat = cur.getString(index);
                        arrayTemas.add(nomeCat);
                    }
                } while (cur.moveToNext());
            }
        }
        return arrayTemas;
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
}
