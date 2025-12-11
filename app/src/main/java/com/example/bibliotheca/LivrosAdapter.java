package com.example.bibliotheca;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class LivrosAdapter extends CursorAdapter {


    public LivrosAdapter(Context context, Cursor c) {
        super(context, c, false); //Descobrir o que é esse false e o que são as flags
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) { //Inflar a view livros_list e retorná-la
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) { //preenche a view livros_list

    }
}
