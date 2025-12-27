package com.example.bibliotheca;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
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

public class ListaAutor extends AppCompatActivity {

    private SQLiteDatabase bibliotecaBD;
    private ListView lstView;
    private TextView txtCont;
    private NavigationView menuLat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        DrawerLayout drawerLayout = findViewById(R.id.main);
        menuLat = findViewById(R.id.menuLat);

        BDHelper bdHelper = new BDHelper(this);
        bibliotecaBD = bdHelper.getReadableDatabase();
        lstView = findViewById(R.id.listView);
        Spinner spnAutor = findViewById(R.id.spinner);
        txtCont = findViewById(R.id.txtCont);

        //Configura a toolbar e seus elementos (aka. Drawer do menu lateral)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
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
        menuLat.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.selpAutor){
                    startActivity(new Intent(ListaAutor.this, ListaAutor.class));
                }
                if(item.getItemId() == R.id.selpCategoria){
                    startActivity(new Intent(ListaAutor.this, ListaCategoria.class));
                }
                if(item.getItemId() == R.id.selpCategoria){
                    Toast.makeText(ListaAutor.this, "Não Implementado.", Toast.LENGTH_SHORT).show();
                }
                if(item.getItemId() == R.id.addLivro){
                    Toast.makeText(ListaAutor.this, "Não Implementado.", Toast.LENGTH_SHORT).show();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        configSpnCategoria(spnAutor, bibliotecaBD); //adiciona os elementos(categorias) do bd no spinner
        spnAutor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String sql = "SELECT Livros._id, titulo_original, titulo_pt_br, GROUP_CONCAT(Autores.nome) AS autores, GROUP_CONCAT(categoria) AS categorias " +
                        "FROM Livros JOIN Livros_Autores ON Livros._id = Livros_Autores.livro_id " +
                        "JOIN Autores ON Autores._id = Livros_Autores.autor_id " +
                        "JOIN Livros_Categorias ON Livros._id = Livros_Categorias.livro_id " +
                        "JOIN Categorias ON Categorias._id = Livros_Categorias.categoria_id " +
                        "WHERE Autores.nome = ? GROUP BY Livros._id ";
                Cursor cur = bibliotecaBD.rawQuery(sql, new String[]{spnAutor.getSelectedItem().toString()});
                LivrosAdapter livroAdap = new LivrosAdapter(ListaAutor.this, cur);
                lstView.setAdapter(livroAdap);
                txtCont.setText("Total: " +cur.getCount());
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { } //Não faz nada
        });
    }

    private void configSpnCategoria(Spinner spnCategoria, SQLiteDatabase bd){
        ArrayList<String> arratCat = new ArrayList<>();
        try (Cursor cur = bd.rawQuery("SELECT nome FROM Autores ORDER BY nome ASC", null)) {
            if (cur.moveToFirst()) {
                do {
                    int index = cur.getColumnIndex("nome");
                    if (index >= 0) {
                        String nomeCat = cur.getString(index);
                        arratCat.add(nomeCat);
                    }
                } while (cur.moveToNext());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arratCat);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategoria.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            if(item.getItemId() == R.id.selpAutor){
                startActivity(new Intent(this, ListaAutor.class));
                return true;
            }
            if(item.getItemId() == R.id.selpCategoria){
                startActivity(new Intent(this, ListaCategoria.class));
                return true;
            }
                Toast.makeText(ListaAutor.this, "Não Implementado.", Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bibliotecaBD != null){
            bibliotecaBD.close();
        }
    }
}
