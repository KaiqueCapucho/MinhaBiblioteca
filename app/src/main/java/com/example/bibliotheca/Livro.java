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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Objects;

public class Livro extends AppCompatActivity {
    public static final String EXTRA_LIVRO_ID = "";
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
    private ImageButton imgBtn;
    private Button btnCancela;
    private Button btnSalva;
    protected String livroID;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Config BD
        bdHelper = new BDHelper(this);
        bibliotecaBD = bdHelper.getReadableDatabase();

        configToolbar();

        ArrayList<View> edtTextViews =  setEdtTexts();

        //Recebe parâmetro
        livroID = getIntent().getStringExtra(EXTRA_LIVRO_ID);

        if(livroID != null){
            insereDadosInEdtTxt(livroID);
            configImgButton(edtTextViews);
        } else{
            toggleText(edtTextViews);
            toggleButtons(btnSalva, btnCancela);
            imgBtn.setVisibility(View.GONE);
        }
        autores.setOnClickListener(view -> configRegister(autores, "Autores", "nome"));
        categorias.setOnClickListener(view -> configRegister(categorias,"Categorias", "categoria"));
        temas.setOnClickListener(view -> configRegister(temas, "Temas", "tema"));

        //Config do botão de cancelamento
        btnCancela.setOnClickListener(view -> {
            Toast.makeText(getApplicationContext(), "Edição cancelada!", Toast.LENGTH_SHORT).show();
            toggleText(edtTextViews);
            toggleButtons(btnCancela, btnSalva);
            if (livroID == null) finish();
        });

        //Config do botão de salvar alterações (toggleEdição/updateBD)
        btnSalva.setOnClickListener(view -> {
            if(updateBD(bibliotecaBD, livroID)){
                toggleText(edtTextViews);
                toggleButtons(btnCancela, btnSalva);
                if (livroID == null) finish();

            }
        });
    }

    protected void configToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    //set EdtTexts, Buttons and return EdtTexts
    protected ArrayList<View> setEdtTexts(){
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
        imgBtn = findViewById(R.id.imgBtnEditar);
        btnCancela = findViewById(R.id.btnCancelar);
        btnSalva = findViewById(R.id.btnSalvar);
        return new ArrayList<>() {{add(titulo); add(ptbr); add(autores); add(editora); add(anoPub);
            add(categorias); add(temas); add(descricao); add(idioma); add(chkBox);
        }};
    }

    protected void insereDadosInEdtTxt(String livroID){
        String sql = "SELECT titulo_original, titulo_pt_br, idioma, ano_pub, editora, descricao, obtido, " +
                "REPLACE(GROUP_CONCAT(DISTINCT nome), ',', ', ') AS autores, " +
                "REPLACE(GROUP_CONCAT(DISTINCT categoria), ',', ', ') AS categorias, " +
                "REPLACE(GROUP_CONCAT(DISTINCT tema), ',', ', ') AS temas " +
                "FROM Livros " +
                "LEFT JOIN Livros_Autores ON Livros._id = Livros_Autores.livro_id " +
                "LEFT JOIN Autores ON Autores._id = Livros_Autores.autor_id " +
                "LEFT JOIN Livros_Categorias ON Livros._id = Livros_Categorias.livro_id " +
                "LEFT JOIN Categorias ON Categorias._id = Livros_Categorias.categoria_id " +
                "LEFT JOIN Livros_Temas ON Livros._id = Livros_Temas.livro_id " +
                "LEFT JOIN Temas ON Temas._id = Livros_Temas.tema_id " +
                "WHERE Livros._id = ?";
        try (Cursor cursor = bibliotecaBD.rawQuery(sql, new String[]{livroID})) {
            if (cursor.moveToFirst()) {
                titulo.setText(cursor.getString(0));
                ptbr.setText(cursor.getString(1));
                idioma.setText(cursor.getString(2));
                anoPub.setText(cursor.getString(3));
                editora.setText(cursor.getString(4));
                descricao.setText(cursor.getString(5));
                chkBox.setChecked(cursor.getInt(6) >= 1);
                autores.setText(cursor.getString(7));
                categorias.setText(cursor.getString(8));
                temas.setText(cursor.getString(9));
            }
        }
    }

    protected void configImgButton(ArrayList<View> views){
        imgBtn.setOnClickListener(view -> {
            toggleText(views);
            toggleButtons(btnCancela, btnSalva);
        });


    }

    protected void toggleText(ArrayList<View> views){
        for (View view: views){
            view.setEnabled((!view.isEnabled()));
        }
    }

    protected void toggleButtons(Button... buttons){
        for(Button b: buttons){
            b.setVisibility(b.getVisibility()==View.VISIBLE ? View.GONE : View.VISIBLE);
        }
    }

    //Config dialog_register
    protected void configRegister(TextView txtView, String tab, String col){
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

    //add chip no chipGroup
    protected void addChip(ChipGroup cg, String s){
        Chip chip = new Chip(this);
        chip.setText(s);
        chip.setCloseIconVisible(true);
        chip.setOnClickListener(v -> cg.removeView(chip));
        cg.addView(chip);
    }

    public boolean updateBD(SQLiteDatabase bd, String livroID) {
        //Atualiza a tabela Livros
        try{
            validaText(autores);
            validaText(categorias);
        }catch (IllegalArgumentException e){
            Toast.makeText(getApplicationContext(), "Campo Obrigatório!", Toast.LENGTH_SHORT).show();
            return false;
        }

        String titulo_original = titulo.getText().toString().trim();
        String titulo_ptbr = ptbr.getText().toString().trim();
        if(titulo_original.isEmpty() && titulo_ptbr.isEmpty()){
            Toast.makeText(this,"Título Obrigatório", Toast.LENGTH_SHORT).show();
            return false;
        }
        ContentValues cvLivros = new ContentValues();
        cvLivros.put("titulo_original", titulo_original);
        cvLivros.put("titulo_pt_br", titulo_ptbr);
        cvLivros.put("idioma", idioma.getText().toString().trim());
        cvLivros.put("ano_pub", anoPub.getText().toString().trim());
        cvLivros.put("editora", editora.getText().toString().trim());
        cvLivros.put("descricao", descricao.getText().toString().trim());
        cvLivros.put("obtido", chkBox.isChecked());

        if(livroID != null){
            bd.update("Livros", cvLivros, "Livros._id = ?", new String[]{livroID});

            // Delete das associações
            bd.execSQL("DELETE FROM Livros_Autores WHERE Livro_id = ?", new Object[]{livroID});
            bd.execSQL("DELETE FROM Livros_Categorias WHERE Livro_id = ?", new Object[]{livroID});
            bd.execSQL("DELETE FROM Livros_Temas WHERE Livro_id = ?", new Object[]{livroID});
        } else{
            livroID = String.valueOf(bd.insertWithOnConflict("Livros", null, cvLivros, SQLiteDatabase.CONFLICT_IGNORE));
        }

        associaTabelas(autores, livroID, "Autores", "nome", "Livros_Autores", "autor_id");
        associaTabelas(categorias, livroID,"Categorias", "categoria", "Livros_Categorias", "categoria_id");
        associaTabelas(temas, livroID,"Temas", "tema", "Livros_Temas", "tema_id");
        Toast.makeText(getApplicationContext(), "Dados salvos no bd!", Toast.LENGTH_SHORT).show();
        return true;
    }

    private void validaText(TextView view) throws IllegalArgumentException{
        if (view.getText().toString().trim().isEmpty()) {
            view.setBackgroundColor(Color.parseColor("#F44336"));
            new Handler().postDelayed(() -> { // Restaura a borda original
                view.setBackgroundColor(getColor(R.color.backgroundColor));
            }, 1200);
            throw new IllegalArgumentException("Campo Obrigatório!");
        }
    }

    // (UpdadeBD)
    public void associaTabelas(TextView txt, String livroID,String tab, String col, String tabA, String colA){
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
    // Volta para a activity anterior
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
