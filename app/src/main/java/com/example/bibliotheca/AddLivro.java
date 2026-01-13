package com.example.bibliotheca;

import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Objects;


public class AddLivro extends AppCompatActivity {
    private SQLiteDatabase bibliotecaBD;
    private BDHelper bdHelper;
    private EditText titulo;
    private EditText ptbr;
    private EditText editora;
    private EditText anoPub;
    private TextView autores;
    private TextView categorias;
    private TextView temas;
    private EditText descricao;
    private EditText idioma;
    private CheckBox chkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bdHelper = new BDHelper(this);
        bibliotecaBD = bdHelper.getReadableDatabase();

        //Config toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }

        setEdtText();
        configButtons();

        autores.setOnClickListener(view -> configRegister(autores, "Autores", "nome"));
        categorias.setOnClickListener(view -> configRegister(categorias,"Categorias", "categoria"));
        temas.setOnClickListener(view -> configRegister(temas, "Temas", "tema"));
    }

    // FindView by Ids (onCreate)
    private void setEdtText(){
        titulo = findViewById(R.id.edtTxtTitulo);
        ptbr = findViewById(R.id.edtTxtPtBr);
        autores = findViewById(R.id.edtTxtAutor);
        editora = findViewById(R.id.edtTxtEditora);
        anoPub = findViewById(R.id.edtTxtAnoPub);
        categorias = findViewById(R.id.edtTxtCategorias);
        temas = findViewById(R.id.edtTxtTemas);
        descricao = findViewById(R.id.edtTxtDescricao);
        idioma = findViewById(R.id.edtTxtIdioma);
        chkBox = findViewById(R.id.chkObtido);
        titulo.setEnabled(true);
        ptbr.setEnabled(true);
        autores.setEnabled(true);
        editora.setEnabled(true);
        anoPub.setEnabled(true);
        categorias.setEnabled(true);
        temas.setEnabled(true);
        descricao.setEnabled(true);
        idioma.setEnabled(true);
        chkBox.setEnabled(true);
    }

    // (onCreate)
    private void configButtons(){
        ImageButton imgBtn = findViewById(R.id.imgBtnEditar);
        Button btnCancela = findViewById(R.id.btnCancelar);
        Button btnSalva = findViewById(R.id.btnSalvar);

        imgBtn.setVisibility(View.INVISIBLE);
        btnCancela.setVisibility(View.VISIBLE);
        btnSalva.setVisibility(View.VISIBLE);

        //Cancelar Operação
        btnCancela.setOnClickListener(view -> {
                Toast.makeText(AddLivro.this, "Operação Cancelada", Toast.LENGTH_SHORT).show();
                finish();
        });

        //Salvar Livro no BD
        btnSalva.setOnClickListener(view -> {
            String tit = titulo.getText().toString();
            String ptb = ptbr.getText().toString();

            if (!tit.isEmpty() || !ptb.isEmpty()){
                String aut = validaText(autores);
                String cat = validaText(categorias);
                String edi = editora.getText().toString();
                String ano = anoPub.getText().toString();
                String tem = temas.getText().toString();
                String des = descricao.getText().toString();
                String idi = idioma.getText().toString();
                boolean chk = chkBox.isEnabled();

                if (aut.isEmpty() || cat.isEmpty()) {
                    Toast.makeText(this,"Campo(s) obrigatório(s)", Toast.LENGTH_SHORT).show();
                } else{
                    ContentValues tabLivro = new ContentValues();
                    tabLivro.put("titulo_original", tit);
                    tabLivro.put("titulo_pt_br", ptb);
                    tabLivro.put("editora", edi.trim());
                    tabLivro.put("ano_pub", ano.trim());
                    tabLivro.put("descricao", des.trim());
                    tabLivro.put("idioma", idi);
                    tabLivro.put("obtido", chk? 1: 0);

                    addLivroOnBD(bibliotecaBD, tabLivro, aut, cat, tem);
                }
            }
            else {
                Toast.makeText(AddLivro.this, "Operação não realizada!  Titulo Obrigatório!", Toast.LENGTH_SHORT).show();
            }

        });
    }

    //
    private String validaText(TextView view){
        String txt = view.getText().toString();
        if (txt.isEmpty()) {
            view.setBackgroundColor(Color.parseColor("#F44336"));
            new Handler().postDelayed(() -> {
                // Restaura a borda original
                view.setBackgroundResource(android.R.drawable.edit_text);
            }, 1200);
        }
        return txt;
    }

    //Config dialog_register (onCreate/onClickListener)
    public void configRegister(TextView txtView, String tab, String col){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_register);
        dialog.show();

        Toolbar toolbar = dialog.findViewById(R.id.dialogTb);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(tab);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(view -> dialog.dismiss());

        //Adiciona as categorias já existentes no chipGroup
        ChipGroup chipGroup = dialog.findViewById(R.id.dialogCp);
        String[] text = txtView.getText().toString().split(", ");
        for (String i: text){
            if(!i.isEmpty()){addChip(chipGroup, i.trim());}
        }


        //Configura AutoCompleteTextView
        AutoCompleteTextView dialogTxt = dialog.findViewById(R.id.dialogActv);
        AutoComAdapter adapter = new AutoComAdapter(this, bdHelper.getColuna(bibliotecaBD,tab, col));
        dialogTxt.setAdapter(adapter);
        dialogTxt.setOnItemClickListener((adapterView, view, i, l) -> {
            String s = adapterView.getItemAtPosition(i).toString();
            addChip(chipGroup, s.trim());
            dialogTxt.setText("");

        });

        //configurar botões
        Button dialogBtnAdd = dialog.findViewById(R.id.dialogBtnAdd);
        dialogBtnAdd.setOnClickListener(view -> {
            String txt = dialogTxt.getText().toString();
            if(!txt.isEmpty()){
                addChip(chipGroup, txt);
                dialogTxt.setText("");
            }
        });

        Button dialogBtnSalvar = dialog.findViewById(R.id.dialogBtnSalvar);
        dialogBtnSalvar.setOnClickListener(view -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                View child = chipGroup.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;
                    sb.append(chip.getText().toString());
                    if (i < chipGroup.getChildCount() - 1) {
                        sb.append(", ");
                    }
                }
            }
            txtView.setText(sb.toString());
            dialog.dismiss();
        });
    }

    //add chip no chipGroup (configRegister/dialog[AutoCom]TxtClickListener -- )
    public void addChip(ChipGroup cg, String s){
        Chip chip = new Chip(this);
        chip.setText(s);
        chip.setCloseIconVisible(true);
        chip.setOnClickListener(v -> cg.removeView(chip));
        cg.addView(chip);
    }

    private void addLivroOnBD(SQLiteDatabase bd, ContentValues livros, String autores, String categorias, String temas) {
        String[] autList = autores.split(",");
        String[] catList = categorias.split(",");
        String[] temList = temas.split(",");
        long livroID = bd.insertWithOnConflict("Livros", null, livros, SQLiteDatabase.CONFLICT_IGNORE);

        if (livroID<0){
            Toast.makeText(AddLivro.this, "Erro! livroID < 0", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!autores.isEmpty()){
            for (String autor : autList) {
                ContentValues autorValues = new ContentValues();
                autorValues.put("nome", autor.trim());
                long autorID = bd.insertWithOnConflict("Autores", null, autorValues, SQLiteDatabase.CONFLICT_IGNORE);
                if (autorID == -1) {
                    Cursor cursor = bd.rawQuery("SELECT _id FROM Autores WHERE nome = ?", new String[]{autor.trim()});
                    if (cursor.moveToFirst()) {
                        autorID = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                    }
                    cursor.close();
                }
                // Associar Livro ao autor
                ContentValues livroCategoriaValues = new ContentValues();
                livroCategoriaValues.put("livro_id", livroID);
                livroCategoriaValues.put("autor_id", autorID);
                bd.insert("Livros_Autores", null, livroCategoriaValues);
            }
        }
        if(!categorias.isEmpty()){
            for (String categoria : catList) {
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
                // Associar Livro à categoria
                ContentValues livroCategoriaValues = new ContentValues();
                livroCategoriaValues.put("livro_id", livroID);
                livroCategoriaValues.put("categoria_id", categoriaID);
                bd.insertWithOnConflict("Livros_Categorias", null, livroCategoriaValues, SQLiteDatabase.CONFLICT_IGNORE);
            }
        }
        if(!temas.isEmpty()) {
            for (String tema : temList) {
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
        Toast.makeText(AddLivro.this, "Livro Adicionado", Toast.LENGTH_SHORT).show();
        finish();
    }

    // Volta para a activity anterior
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}