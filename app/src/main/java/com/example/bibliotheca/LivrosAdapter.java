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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class LivrosAdapter extends RecyclerView.Adapter<LivrosAdapter.LivroViewHolder> {
    private final Context context;
    private final ArrayList<String[]> dados = new ArrayList<>();

    public LivrosAdapter(Context context, Cursor cursor) {
        this.context = context;
        setDados(cursor);

    }

    @NonNull @Override
    public LivroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LivroViewHolder(LayoutInflater.from(context).inflate(R.layout.livros_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LivrosAdapter.LivroViewHolder holder, int position) {
            String[] livro = dados.get(position);
            holder.txtTitulo.setText(livro[1]);
            holder.txtAutores.setText(livro[2]);
            holder.txtCategorias.setText(livro[3]);

            if(Objects.equals(livro[4], "1")) {
                holder.itemView.setBackgroundColor(Color.parseColor(context.getString(R.string.obtido)));
            } else {
                holder.itemView.setBackgroundColor(Color.parseColor(context.getString(R.string.lvColor)));
            }
            //Abre a activity_livro
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, Livro.class);
                intent.putExtra(Livro.EXTRA_LIVRO_ID, livro[0]);
                context.startActivity(intent);
            });

    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    public void setDados(Cursor cur){
        if (cur.moveToFirst()) {
            do {
                int livroID = cur.getInt(cur.getColumnIndexOrThrow("_id"));
                String titulo = cur.getString(cur.getColumnIndexOrThrow("titulo_original"));
                if (titulo.isEmpty()) {
                    titulo = cur.getString(cur.getColumnIndexOrThrow("titulo_pt_br"));
                }
                String autores = cur.getString(cur.getColumnIndexOrThrow("autores"));
                String categorias = cur.getString(cur.getColumnIndexOrThrow("categorias"));
                int obtido = cur.getInt(cur.getColumnIndexOrThrow("obtido"));

                dados.add(new String[]{String.valueOf(livroID), titulo, autores, categorias, String.valueOf(obtido)});
            } while (cur.moveToNext());
        }
        //Ordena a lista pelo titulo do livro
        dados.sort(Comparator.comparing(s -> s[1]));
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

