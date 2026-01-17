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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Objects;

public abstract class ListaBase extends AppCompatActivity {
    protected SQLiteDatabase bibliotecaBD;
    protected RecyclerView lstLivros;
    protected TextView txtCont;
    protected TextView txtSpinner;
    protected EditText edtSearch;
    protected LivrosAdapter livroAdap;
    protected Dialog dialog;
    protected BDHelper bdHelper;
    protected View headerView;
    protected AutoCompleteTextView acTxtView;
    protected ImageButton btnConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView menuLat = findViewById(R.id.menuLat);
        headerView = menuLat.getHeaderView(0);

        bdHelper = new BDHelper(this);
        bibliotecaBD = bdHelper.getReadableDatabase();

        txtSpinner = findViewById(R.id.txtSpinner);
        txtSpinner.setText(getSpinnerText());
        lstLivros = findViewById(R.id.listView);
        txtCont = findViewById(R.id.txtCont);

        btnConfig = findViewById(R.id.btnConfig);

        configToolbar(drawerLayout);
        configMenuLat(menuLat, drawerLayout);

        txtSpinner.setOnClickListener(view -> configDialog());
    }

    //(OnCreate)
    private void configToolbar(DrawerLayout drawerLayout){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(""); //remove o título da toolbar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.Open, R.string.Close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Guarda se txtCont estava visível antes de abrir o drawer
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                btnConfig.setAlpha(slideOffset);
                btnConfig.setVisibility(View.VISIBLE);

                txtSpinner.setAlpha(Math.max(0f, 1f - slideOffset * 2));
                txtCont.setAlpha(Math.max(0f, 1f - slideOffset * 2));

                if (slideOffset > 0f) txtCont.setVisibility(View.GONE);

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                txtSpinner.setVisibility(View.GONE);
                txtCont.setVisibility(View.GONE);
                btnConfig.setOnClickListener(view -> confBtnConfig() );
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                btnConfig.setVisibility(View.GONE);
                txtSpinner.setVisibility(View.VISIBLE);
                txtCont.setVisibility(txtCont.getText() != ""  ?View.VISIBLE : View.INVISIBLE);

            }

            @Override
            public void onDrawerStateChanged(int newState) {}
        });

    }

    //(OnCreate)
    private void configMenuLat(NavigationView menuLateral, DrawerLayout drawerLayout){
        menuLateral.setNavigationItemSelectedListener(item -> {
            if(item.getItemId() == R.id.selpAutor){
                startActivity(new Intent(this, ListaAutor.class));
                finish();
            }
            if(item.getItemId() == R.id.selpCategoria){
                startActivity(new Intent(this, ListaCategoria.class));
                finish();            }
            if(item.getItemId() == R.id.selpTema){
                startActivity(new Intent(this, ListaTema.class));
                finish();
            }
            if(item.getItemId() == R.id.addLivro){
                startActivity(new Intent(this, Livro.class));
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
    private void confBtnConfig(){
        Toast.makeText(getApplicationContext(), "Ainda não Implementado!", Toast.LENGTH_SHORT).show();
    }


    //Abre a activity_livro ao clicar num item do ListView (configMenuLat/acTctViewOnClick --
    private void openLivroActivity(int i) {
        Intent intent = new Intent(this, Livro.class);
        intent.putExtra(Livro.EXTRA_LIVRO_ID, String.valueOf(i));
        startActivity(intent);
    }

    private void configDialog(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_spinner);
        dialog.show();
        edtSearch = dialog.findViewById(R.id.edtTxtSearch);
        ListView lstView = dialog.findViewById(R.id.lstType);

        //Adiciona categorias, autores ou temas no listView do dialog
            ArrayList<String> arratCat = bdHelper.getColuna(bibliotecaBD, getTabToDialog(), getColToDialog());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arratCat);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        lstView.setAdapter(adapter);
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
        addLivrosOnListView(lstView);
    }
    private void addLivrosOnListView(ListView spnListView){
        spnListView.setOnItemClickListener((adapterView, view, i, l) -> {
            Cursor cur = bibliotecaBD.rawQuery(getSqlToListView(), new String[]{adapterView.getItemAtPosition(i).toString()});
            livroAdap = new LivrosAdapter(this, cur);

            lstLivros.setLayoutManager(new LinearLayoutManager(this));
            lstLivros.setAdapter(livroAdap);
            lstLivros.setVisibility(View.VISIBLE);
            dialog.dismiss();
            txtCont.setText(String.format(getString(R.string.total), cur.getCount())); //Adiciona o total de livros no contador
            txtCont.setVisibility(View.VISIBLE);
            txtSpinner.setText(adapterView.getItemAtPosition(i).toString());
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bibliotecaBD != null){
            bibliotecaBD.close();
        }
    }

    protected abstract int getSpinnerText();
    protected abstract String getTabToDialog();
    protected abstract String getColToDialog();
    protected abstract String getSqlToListView();
}
