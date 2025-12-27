package com.example.bibliotheca;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Objects;

public class ListaCategoria extends AppCompatActivity {

    private SQLiteDatabase bibliotecaBD;
    private ListView lstView;
    private TextView txtCont;
    private LivrosAdapter livroAdap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        DrawerLayout drawerLayout = findViewById(R.id.main);
        NavigationView menuLat = findViewById(R.id.menuLat);

        BDHelper bdHelper = new BDHelper(this);
        bibliotecaBD = bdHelper.getReadableDatabase();

        lstView = findViewById(R.id.listView);
        Spinner spnCategoria = findViewById(R.id.spinner);
        txtCont = findViewById(R.id.txtCont);

        //Configura a toolbar e seus elementos (aka. Drawer do menu lateral)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(""); //remove o título da toolbar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.Open, R.string.Close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) { }
            @Override
            public void onDrawerOpened(@NonNull View drawerView) { }
            @Override
            public void onDrawerClosed(@NonNull View drawerView) { }
            @Override
            public void onDrawerStateChanged(int newState) { }
        });

        //Configuração dos elementos do Menu Lateral
        menuLat.setNavigationItemSelectedListener(item -> {
            if(item.getItemId() == R.id.selpAutor){
                startActivity(new Intent(ListaCategoria.this, ListaAutor.class));
            }
            if(item.getItemId() == R.id.selpCategoria){
                startActivity(new Intent(ListaCategoria.this, ListaCategoria.class));
            }
            if(item.getItemId() == R.id.selpTema){
                Toast.makeText(ListaCategoria.this, "Não Implementado.", Toast.LENGTH_SHORT).show();
            }
            if(item.getItemId() == R.id.addLivro){
                Toast.makeText(ListaCategoria.this, "Não Implementado.", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        //adiciona os elementos(categorias) do bd no spinner
        configSpnCategoria(spnCategoria, bibliotecaBD);

        //Adiciona os livros no listView
        spnCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String sql = "SELECT Livros._id, titulo_original, titulo_pt_br, GROUP_CONCAT(Autores.nome) AS autores, GROUP_CONCAT(categoria) AS categorias " +
                        "FROM Livros JOIN Livros_Autores ON Livros._id = Livros_Autores.livro_id " +
                        "JOIN Autores ON Autores._id = Livros_Autores.autor_id " +
                        "JOIN Livros_Categorias ON Livros._id = Livros_Categorias.livro_id " +
                        "JOIN Categorias ON Categorias._id = Livros_Categorias.categoria_id " +
                        "WHERE Categorias.categoria = ? GROUP BY Livros._id ";
                Cursor cur = bibliotecaBD.rawQuery(sql, new String[]{spnCategoria.getSelectedItem().toString()});
                livroAdap = new LivrosAdapter(ListaCategoria.this, cur);
                lstView.setAdapter(livroAdap);
                txtCont.setText("Total: " +cur.getCount()); //Adiciona o total de livros no contador
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { } //Não faz nada
        });
        lstView.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent = new Intent(ListaCategoria.this, Livro.class);
            intent.putExtra(Livro.extraLivroID, String.valueOf(livroAdap.getLivroID(i)));
            startActivity(intent);
        });
    }

    private void configSpnCategoria(Spinner spnCategoria, SQLiteDatabase bd){
        ArrayList<String> arratCat = new ArrayList<>();
        try (Cursor cur = bd.rawQuery("SELECT categoria FROM Categorias ORDER BY categoria ASC", null)) {
            if (cur.moveToFirst()) {
                do {
                    int index = cur.getColumnIndex("categoria");
                    if (index >= 0) {
                        arratCat.add(cur.getString(index));
                    }
                } while (cur.moveToNext());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arratCat);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategoria.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bibliotecaBD != null){
            bibliotecaBD.close();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(bibliotecaBD != null){
            bibliotecaBD.close();
        }
    }
}
