package com.example.bibliotheca;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.Objects;


public class Livro extends AppCompatActivity {

    public static final String extraLivroID = "";
    private String livroID;
    private SQLiteDatabase bibliotecaBD;
    private BDHelper bdHelper;
    private EditText titulo;
    private EditText ptbr;
    private EditText editora;
    private EditText anoPub;
    private TextView autor;
    private TextView categorias;
    private TextView temas;
    private EditText descricao;
    private EditText idioma;
    private CheckBox chkBox;
    private Button   btnSalva;
    private Button   btnCancela;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livro);
        bdHelper = new BDHelper(this);

        //recebe livroID parâmetro
        Intent intent = getIntent();
        livroID = intent.getStringExtra(Livro.extraLivroID);

        //Config toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //FindViews
        ImageButton imgBtnEdit = findViewById(R.id.imgBtnEditar);
        titulo = findViewById(R.id.edtTxtTitulo);
        ptbr = findViewById(R.id.edtTxtPtBr);
        autor = findViewById(R.id.edtTxtAutor);
        editora = findViewById(R.id.edtTxtEditora);
        anoPub = findViewById(R.id.edtTxtAnoPub);
        categorias = findViewById(R.id.edtTxtCategorias);
        temas = findViewById(R.id.edtTxtTemas);
        descricao = findViewById(R.id.edtTxtDescricao);
        idioma = findViewById(R.id.edtTxtIdioma);
        chkBox = findViewById(R.id.chkObtido);
        btnSalva = findViewById(R.id.btnSalvar);
        btnCancela = findViewById(R.id.btnCancelar);

        //Config toolbar principal (seta voltar)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }

        //Inicialização do banco de dados
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
            anoPub.setText(cursor.getString(cursor.getColumnIndexOrThrow("ano_pub")));
            categorias.setText(cursor.getString(cursor.getColumnIndexOrThrow("categorias")));
            temas.setText(cursor.getString(cursor.getColumnIndexOrThrow("temas")));
            descricao.setText(cursor.getString(cursor.getColumnIndexOrThrow("descricao")));
            chkBox.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow("obtido")) >= 1);
        }

        //Config do botão para ativar edição
        imgBtnEdit.setOnClickListener(view -> toggleEdicao(titulo, ptbr, autor, editora, anoPub, categorias, temas, descricao, idioma, chkBox));

        //Config do botão de cancelamento (toggleEdição)
        btnCancela.setOnClickListener(view -> {
            Toast.makeText(getApplicationContext(), "Edição cancelada!", Toast.LENGTH_SHORT).show();
            toggleEdicao(titulo, ptbr, autor, editora, anoPub, categorias, temas, descricao, idioma, chkBox);
        });

        //Config do botão de salvar alterações (toggleEdição/updateBD)
        btnSalva.setOnClickListener(view -> {
            toggleEdicao(titulo, ptbr, autor, editora, anoPub, categorias, temas, descricao, idioma, chkBox);
            updateBD(bibliotecaBD);
            Toast.makeText(getApplicationContext(), "Dados salvos no bd!", Toast.LENGTH_SHORT).show();
        });

        autor.setOnClickListener(view -> configRegister(autor, "Autores", "nome"));
        categorias.setOnClickListener(view -> configRegister(categorias,"Categorias", "categoria"));
        temas.setOnClickListener(view -> configRegister(temas, "Temas", "tema"));
        cursor.close();
    }

    public void toggleEdicao(View... views){
        for (View view: views){
            view.setEnabled((!view.isEnabled()));
        }
        btnSalva.setVisibility(btnSalva.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        btnCancela.setVisibility(btnCancela.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    public void updateBD(SQLiteDatabase bd) {
        //Atualiza a tabela Livros
        ContentValues cvLivros = new ContentValues();
        cvLivros.put("titulo_original", titulo.getText().toString().trim());
        cvLivros.put("titulo_pt_br", ptbr.getText().toString().trim());
        cvLivros.put("idioma", idioma.getText().toString().trim());
        cvLivros.put("ano_pub", anoPub.getText().toString().trim());
        cvLivros.put("editora", editora.getText().toString().trim());
        cvLivros.put("descricao", descricao.getText().toString().trim());
        cvLivros.put("obtido", chkBox.isChecked());
        bd.update("Livros", cvLivros, "Livros._id = ?", new String[]{livroID});

        // Delete das associações
        bd.execSQL("DELETE FROM Livros_Autores WHERE Livro_id = ?", new Object[]{livroID});
        bd.execSQL("DELETE FROM Livros_Categorias WHERE Livro_id = ?", new Object[]{livroID});
        bd.execSQL("DELETE FROM Livros_Temas WHERE Livro_id = ?", new Object[]{livroID});

        associaTabelas(autor, "Autores", "nome", "Livros_Autores", "autor_id");
        associaTabelas(categorias, "Categorias", "categoria", "Livros_Categorias", "categoria_id");
        associaTabelas(temas, "Temas", "tema", "Livros_Temas", "tema_id");
    }

    // (UpdadeBD)
    public void associaTabelas(TextView txt, String tab, String col, String tabA, String colA){
        if(txt.getText().toString().isEmpty()){
            return;
        }
        String[] array = txt.getText().toString().split(",");
        for (String s : array){
            //Tenta inserir o valor na tabela
            ContentValues edtValue = new ContentValues();
            edtValue.put(col, s.trim());
            long valueID = bibliotecaBD.insertWithOnConflict(tab, null, edtValue, SQLiteDatabase.CONFLICT_IGNORE);
           //Pega o id do valor que já existe (se inserção falhar)
            if (valueID == -1){
                Cursor cursor = bibliotecaBD.rawQuery("SELECT _id FROM "+ tab +" WHERE "+col+" = ?", new String[]{s.trim()});
                if (cursor.moveToFirst()) {
                    valueID = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                } cursor.close();
            }
            //Associa as tabelas
            ContentValues values = new ContentValues();
            values.put("livro_id", livroID);
            values.put(colA, valueID);
            bibliotecaBD.insert(tabA, null, values);
        }
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

    // Volta para a activity anterior (configRegister)
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bibliotecaBD != null){
            bibliotecaBD.close();
        }
    }
}
