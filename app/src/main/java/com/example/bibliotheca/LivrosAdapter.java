package com.example.bibliotheca;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class LivrosAdapter extends CursorAdapter {
    private ArrayList<Integer> listaID = new ArrayList<>();

    public LivrosAdapter(Context context, Cursor c) {
        super(context, c, false); //Descobrir o que é esse false e o que são as flags
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return ((Activity)context).getLayoutInflater().inflate(R.layout.livros_list, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView txt = view.findViewById(R.id.txtNome);
        txt.setText(cursor.getString(cursor.getColumnIndexOrThrow("titulo_pt_br")));
        txt = view.findViewById(R.id.txtAutor);
        txt.setText(cursor.getString(cursor.getColumnIndexOrThrow("autores")));
        txt = view.findViewById(R.id.txtCategoria);
        txt.setText(cursor.getString(cursor.getColumnIndexOrThrow("categorias")));
        listaID.add(cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
    }
    public int getLivroID(int p){
        return listaID.get(p);
    }
}
