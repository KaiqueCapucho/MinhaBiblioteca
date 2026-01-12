package com.example.bibliotheca;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Objects;

public class ListaAutor extends AppCompatActivity {

    private SQLiteDatabase bibliotecaBD;
    private RecyclerView lstLivros;
    private TextView txtCont;
    private TextView txtSpinner;
    private EditText edtSearch;
    private LivrosAdapter livroAdap;
    private Dialog dialog;
    private BDHelper bdHelper;
    private View headerView;
    private AutoCompleteTextView acTxtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        DrawerLayout drawerLayout = findViewById(R.id.main);
        NavigationView menuLat = findViewById(R.id.menuLat);
        headerView = menuLat.getHeaderView(0);

        bdHelper = new BDHelper(this);
        bibliotecaBD = bdHelper.getReadableDatabase();

        txtSpinner = findViewById(R.id.txtSpinner);
        txtSpinner.setText(R.string.select_author);
        lstLivros = findViewById(R.id.listView);
        txtCont = findViewById(R.id.txtCont);

        configToolbar(drawerLayout);
        configMenuLat(menuLat, drawerLayout);

        txtSpinner.setOnClickListener(view -> configDialog());
    }

    private void configToolbar(DrawerLayout drawerLayout){
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
    }

    private void configMenuLat(NavigationView menuLateral, DrawerLayout drawerLayout){
        menuLateral.setNavigationItemSelectedListener(item -> {
            if(item.getItemId() == R.id.selpAutor){
                Toast.makeText(this, "Já Selecionado.", Toast.LENGTH_SHORT).show();
            }
            if(item.getItemId() == R.id.selpCategoria){
                startActivity(new Intent(this, ListaCategoria.class));
                finish();
            }
            if(item.getItemId() == R.id.selpTema){
                startActivity(new Intent(this, ListaTema.class));
                finish();
            }
            if(item.getItemId() == R.id.addLivro){
                startActivity(new Intent(this, AddLivro.class));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        acTxtView = headerView.findViewById(R.id.acTxtView);
        AutoComAdapter adapter = new AutoComAdapter(this,bdHelper.getLivros(bibliotecaBD));
        acTxtView.setAdapter(adapter);

        acTxtView.setOnItemClickListener((adapterView, view, i, l) -> {
            String t = adapterView.getItemAtPosition(i).toString();
            Cursor c = bibliotecaBD.rawQuery(
                    "SELECT _id FROM Livros WHERE titulo_original == ? OR titulo_pt_br == ?", new String[]{t,t});
            if (c.moveToFirst()) {
                openLivroActivity(c.getInt(c.getColumnIndexOrThrow("_id")));
                acTxtView.setText("");
            } c.close();
        });
    }

    private void configDialog(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_spinner);
        dialog.show();
        edtSearch = dialog.findViewById(R.id.edtTxtSearch);
        ListView lstAut = dialog.findViewById(R.id.lstType);
        addAuthorsOnDialog(lstAut);
    }

    private void addAuthorsOnDialog(ListView listView){
        ArrayList<String> arrayAut = bdHelper.getColuna(bibliotecaBD, "Autores", "nome");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayAut);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable e) { }
        });
        addLivrosOnListView(listView);
    }

    private void addLivrosOnListView(ListView spnListView){
        spnListView.setOnItemClickListener((adapterView, view, i, l) -> {
            String sql = "SELECT Livros._id, titulo_original, titulo_pt_br, obtido, " +
                    "REPLACE(GROUP_CONCAT(DISTINCT Autores.nome), ',', ', ' ) AS autores, " +
                    "REPLACE(GROUP_CONCAT(DISTINCT categoria), \",\", \", \") AS categorias " +
                    "FROM Livros \n" +
                    "JOIN Livros_Autores ON Livros._id = Livros_Autores.livro_id " +
                    "LEFT JOIN Autores ON Autores._id = Livros_Autores.autor_id " +
                    "LEFT JOIN Livros_Categorias ON Livros._id = Livros_Categorias.livro_id " +
                    "LEFT JOIN Categorias ON Categorias._id = Livros_Categorias.categoria_id " +
                    "GROUP BY Livros._id HAVING GROUP_CONCAT(Autores.nome) LIKE ?";
            Cursor cur = bibliotecaBD.rawQuery(sql, new String[]{"%"+adapterView.getItemAtPosition(i).toString()+"%"});
            livroAdap = new LivrosAdapter(this, cur);
            lstLivros.setLayoutManager(new LinearLayoutManager(this));
            lstLivros.setAdapter(livroAdap);
            lstLivros.setVisibility(View.VISIBLE);
            dialog.dismiss();
            txtCont.setText("Total: " +cur.getCount()); //Adiciona o total de livros no contador
            txtCont.setVisibility(View.VISIBLE);
            txtSpinner.setText(adapterView.getItemAtPosition(i).toString());
        });
    }

    //Abre a activity_livro ao clicar num item do ListView
    private void openLivroActivity(int i) {
        Intent intent = new Intent(this, Livro.class);
        intent.putExtra(Livro.extraLivroID, String.valueOf(i));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bibliotecaBD != null){
            bibliotecaBD.close();
        }
    }
}
