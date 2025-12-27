package com.example.bibliotheca;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

public class Livro extends AppCompatActivity {

    public static final String extraLivroID = "";
    private String livroID;
    private SQLiteDatabase bibliotecaBD;
    private EditText titulo;
    private EditText ptbr;
    private EditText autor;
    private EditText editora;
    private EditText anoPub;
    private EditText categorias;
    private EditText temas;
    private EditText descricao;
    private CheckBox chkBox;
    private Button   btnSalva;
    private Button   btnCancela;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livro);

        Intent intent = getIntent();
        livroID = intent.getStringExtra(Livro.extraLivroID);

        DrawerLayout drawerLayout = findViewById(R.id.main);
        NavigationView menuLat = findViewById(R.id.menuLat);

        ImageButton imgBtnEdit = findViewById(R.id.imgBtnEditar);
        Toolbar  toolbar = findViewById(R.id.toolbar);
        titulo = findViewById(R.id.edtTxtTitulo);
        ptbr = findViewById(R.id.edtTxtPtBr);
        autor = findViewById(R.id.edtTxtAutor);
        editora = findViewById(R.id.edtTxtEditora);
        anoPub = findViewById(R.id.edtTxtAnoPub);
        categorias = findViewById(R.id.edtTxtCategorias);
        temas = findViewById(R.id.edtTxtTemas);
        descricao = findViewById(R.id.edtTxtDescricao);
        chkBox = findViewById(R.id.chkObtido);
        btnSalva = findViewById(R.id.btnSalvar);
        btnCancela = findViewById(R.id.btnCancelar);

        //Configura a toolbar e seus elementos (aka. Drawer do menu lateral)
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
                startActivity(new Intent(Livro.this, ListaAutor.class));
            }
            if(item.getItemId() == R.id.selpCategoria){
                startActivity(new Intent(Livro.this, ListaCategoria.class));
            }
            if(item.getItemId() == R.id.selpTema){
                Toast.makeText(Livro.this, "Não Implementado.", Toast.LENGTH_SHORT).show();
            }
            if(item.getItemId() == R.id.addLivro){
                Toast.makeText(Livro.this, "Não Implementado.", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        //Inicialização do banco de dados
        BDHelper bdHelper = new BDHelper(this);
        bibliotecaBD = bdHelper.getReadableDatabase();

        //Inserção de dados nos EditTexts (e checkBox)
        Cursor cursor = bibliotecaBD.rawQuery("SELECT titulo_original, titulo_pt_br, idioma, ano_pub, editora, descricao, obtido,\n" +
                "REPLACE(GROUP_CONCAT(DISTINCT nome), ',', ', ') AS autores, \n" +
                "REPLACE(GROUP_CONCAT(DISTINCT categoria), ',', ', ') AS categorias, \n" +
                "REPLACE(GROUP_CONCAT(DISTINCT tema), ',', ', ')AS temas\n" +
                "FROM Livros LEFT JOIN Livros_Autores ON Livros._id = Livros_Autores.livro_id\n" +
                "LEFT JOIN Autores ON Autores._id = Livros_Autores.autor_id\n" +
                "LEFT JOIN Livros_Categorias ON Livros._id = Livros_Categorias.livro_id\n" +
                "LEFT JOIN Categorias ON Categorias._id = Livros_Categorias.categoria_id\n" +
                "LEFT JOIN Livros_Temas ON Livros._id = Livros_Temas.livro_id\n" +
                "LEFT JOIN Temas ON Temas._id = Livros_Temas.tema_id\n" +
                "WHERE Livros._id = ?", new String[]{String.valueOf(livroID)});
        if (cursor.moveToFirst()) {
            titulo.setText(cursor.getString(cursor.getColumnIndexOrThrow("titulo_original")));
            ptbr.setText(cursor.getString(cursor.getColumnIndexOrThrow("titulo_pt_br")));
            autor.setText(cursor.getString(cursor.getColumnIndexOrThrow("autores")));
            editora.setText(cursor.getString(cursor.getColumnIndexOrThrow("editora")));
            anoPub.setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("ano_pub"))));
            categorias.setText(cursor.getString(cursor.getColumnIndexOrThrow("categorias")));
            temas.setText(cursor.getString(cursor.getColumnIndexOrThrow("temas")));
            descricao.setText(cursor.getString(cursor.getColumnIndexOrThrow("descricao")));
            chkBox.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow("obtido")) >= 1);
        }

        //Botão para ativar edição
        imgBtnEdit.setOnClickListener(view -> toggleEdicao());

        //Config do botão de cancelamento
        btnCancela.setOnClickListener(view -> {
            Toast.makeText(getApplicationContext(), "Edição cancelada!", Toast.LENGTH_SHORT).show();
            toggleEdicao();
        });

        //Config do botão de salvar alterações
        btnSalva.setOnClickListener(view -> {
            toggleEdicao();
            updateBD(bibliotecaBD);
            Toast.makeText(getApplicationContext(), "Dados salvos no bd!", Toast.LENGTH_SHORT).show();
        });

        cursor.close();
    }
    public void toggleEdicao(){
        titulo.setEnabled(!titulo.isEnabled());
        ptbr.setEnabled(!ptbr.isEnabled());
        autor.setEnabled(!autor.isEnabled());
        editora.setEnabled(!editora.isEnabled());
        anoPub.setEnabled(!anoPub.isEnabled());
        categorias.setEnabled(!categorias.isEnabled());
        temas.setEnabled(!temas.isEnabled());
        descricao.setEnabled(!descricao.isEnabled());
        chkBox.setEnabled(!chkBox.isEnabled());
        btnSalva.setVisibility(btnSalva.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        btnCancela.setVisibility(btnCancela.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    public void updateBD(SQLiteDatabase bd) {
        ContentValues cvLivros = new ContentValues();
        cvLivros.put("titulo_original", titulo.getText().toString().trim());
        cvLivros.put("titulo_pt_br", ptbr.getText().toString().trim());
        //cvLivros.put("idioma", );
        cvLivros.put("ano_pub", anoPub.getText().toString().trim());
        cvLivros.put("editora", editora.getText().toString().trim());
        cvLivros.put("descricao", descricao.getText().toString().trim());
        //cvLivros.put("obtido", chkBox);

        //Atualiza a tabela Livros
        bd.update("Livros", cvLivros, "Livros._id = ?", new String[]{livroID});

        // Delete das conexões
        bd.execSQL("DELETE FROM Livros_Autores WHERE Livro_id = ?", new Object[]{livroID});
        bd.execSQL("DELETE FROM Livros_Categorias WHERE Livro_id = ?", new Object[]{livroID});

        ContentValues autorValues = new ContentValues();
        autorValues.put("nome", autor.getText().toString().trim());
        long autorID = bd.insertWithOnConflict("Autores", null, autorValues, SQLiteDatabase.CONFLICT_IGNORE);

        if (autorID < 0) {
            Cursor cursor = bd.rawQuery("SELECT _id FROM Autores WHERE nome = ?", new String[]{autor.getText().toString().trim()});
            if (cursor.moveToFirst()) {
                autorID = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            }
            cursor.close();
        }

        // Associar livro ao autor
        ContentValues livroAutorValues = new ContentValues();
        livroAutorValues.put("livro_id", livroID);
        livroAutorValues.put("autor_id", autorID);
        bd.insertWithOnConflict("Livros_Autores", null, livroAutorValues, SQLiteDatabase.CONFLICT_IGNORE);

        // Inserir ou obter IDs das Categorias
        String[] categoriasArray = categorias.getText().toString().split(",");
        for (String categoria : categoriasArray) {
            ContentValues categoriaValues = new ContentValues();
            categoriaValues.put("categoria", categoria.trim());
            long categoriaID = bd.insertWithOnConflict("Categorias", null, categoriaValues, SQLiteDatabase.CONFLICT_IGNORE);

            if (categoriaID == -1) {
                Cursor cursor = bd.rawQuery("SELECT _id FROM Categorias WHERE categoria = ?", new String[]{categoria.trim()});
                if (cursor.moveToFirst()) {
                    categoriaID = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                }
                cursor.close();
            }

            // Associar livro à categoria
            ContentValues livroCategoriaValues = new ContentValues();
            livroCategoriaValues.put("livro_id", livroID);
            livroCategoriaValues.put("categoria_id", categoriaID);
            bd.insertWithOnConflict("Livros_Categorias", null, livroCategoriaValues, SQLiteDatabase.CONFLICT_IGNORE);
        }

        // Inserir ou obter IDs dos Temas
        String[] temasArray = temas.getText().toString().split(",");
        for (String tema : temasArray) {
            ContentValues temaValues = new ContentValues();
            temaValues.put("tema", tema.trim());
            long temaID = bd.insertWithOnConflict("Temas", null, temaValues, SQLiteDatabase.CONFLICT_IGNORE);

            if (temaID == -1) {
                Cursor cursor = bd.rawQuery("SELECT _id FROM Temas WHERE tema = ?", new String[]{tema.trim()});
                if (cursor.moveToFirst()) {
                    temaID = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                }
                cursor.close();
            }
            // Associar livro ao tema
            ContentValues livroTemaValues = new ContentValues();
            livroTemaValues.put("livro_id", livroID);
            livroTemaValues.put("tema_id", temaID);
            bd.insertWithOnConflict("Livros_Temas", null, livroTemaValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bibliotecaBD != null){
            bibliotecaBD.close();
        }
    }
}
