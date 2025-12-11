package com.example.bibliotheca;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class ListaActivity extends AppCompatActivity {

    private SQLiteDatabase bibliotecaBD;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        DrawerLayout drawerLayout = findViewById(R.id.main);
        NavigationView navigationView = findViewById(R.id.navView);

        BDHelper bdHelper = new BDHelper(this);
        SQLiteDatabase bibliotecaBD = bdHelper.getReadableDatabase();

        Spinner spnCategoria = findViewById(R.id.spnCategoria);

        //Configura a toolbar e seus elementos (aka. Drawer do menu lateral)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.Open, R.string.Close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        configSpnCategoria(spnCategoria, bibliotecaBD); //adiciona os elementos(categorias) do bd no spinner
        spnCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String itemSel = adapterView.getSelectedItem().toString();
                configRecyclerView(itemSel, bibliotecaBD);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { } //Não faz nada
        });
    }

    private void configSpnCategoria(Spinner spn, SQLiteDatabase bd){
        ArrayList<String> arratCat = new ArrayList<>();
        Cursor cur = bd.rawQuery("SELECT categoria FROM Categorias ORDER BY categoria ASC", null);
        if(cur.moveToFirst()){
            do{
                int index = cur.getColumnIndex("categoria");
                if(index>=0){
                    String nomeCat = cur.getString(index);
                    arratCat.add(nomeCat);
                }
            }while (cur.moveToNext());
        }

        cur.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arratCat);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn.setAdapter(adapter);
    }
    private void configRecyclerView(String sqlParam, SQLiteDatabase bd){
        String sql = "S";         //criar a query
        Cursor cur = bd.rawQuery(sql, null);
        LivrosAdapter lvAdapter = new LivrosAdapter(ListaActivity.this, cur, null); //tratar esse null
        //listView.setAdapter(lvAdapter)
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bibliotecaBD != null){
            bibliotecaBD.close();
        }
    }
}
