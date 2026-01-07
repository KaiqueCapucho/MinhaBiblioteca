package com.example.bibliotheca;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

public class LivrosAdapter extends RecyclerView.Adapter<LivrosAdapter.LivroViewHolder> {
    private Context context;
    private Cursor cursor;

    public LivrosAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    @NonNull @Override
    public LivroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LivroViewHolder(LayoutInflater.from(context).inflate(R.layout.livros_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LivrosAdapter.LivroViewHolder holder, int position) {
        if (cursor.moveToPosition(position)) {
            int livroID = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo_original"));
            String ptbt = cursor.getString(cursor.getColumnIndexOrThrow("titulo_pt_br"));
            String autores = cursor.getString(cursor.getColumnIndexOrThrow("autores"));
            String categorias = cursor.getString(cursor.getColumnIndexOrThrow("categorias"));

            // Coloca os dados nos TextViews da ViewHolder
            holder.txtTitulo.setText(Objects.equals(titulo, "") ? ptbt: titulo );
            holder.txtAutores.setText(autores);
            holder.txtCategorias.setText(categorias);

            if(cursor.getInt(cursor.getColumnIndexOrThrow("obtido"))!=0) {
                holder.itemView.setBackgroundColor(Color.parseColor(context.getString(R.string.obtido)));
            } else {
                holder.itemView.setBackgroundColor(Color.parseColor(context.getString(R.string.lvColor)));
            }

            //Abre a activity_livro
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, Livro.class);
                intent.putExtra(Livro.extraLivroID, String.valueOf(livroID));
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    // ViewHolder para o RecyclerView
    public static class LivroViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtAutores, txtCategorias;

        public LivroViewHolder(View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtNome);
            txtAutores = itemView.findViewById(R.id.txtAutor);
            txtCategorias = itemView.findViewById(R.id.txtCategoria);

        }
    }
}

