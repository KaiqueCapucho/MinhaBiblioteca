package com.example.bibliotheca;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    BDHelper bdHelper;
    SQLiteDatabase bibliotecaBD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.listaAtv), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        bdHelper = new BDHelper(this);
        bibliotecaBD = bdHelper.getReadableDatabase();

        limpaBD();

        Intent lista = new Intent(this, ListaCategoria.class);
        startActivity(lista);
        finish();
    }
    //Remove autores, categorias e/ou temas sem livros associados.
    public void limpaBD(){

        delLinha("Autores", "autor_id");
        delLinha("Categorias", "categoria_id");
        delLinha("Temas", "tema_id");

    }
    public void delLinha(String tab1, String col){
        ArrayList<String> tabID = bdHelper.getColuna(bibliotecaBD, "Livros_"+tab1, col);
        for(String i: bdHelper.getColuna(bibliotecaBD, tab1, "_id")){
            if(!tabID.contains(i)){
                bibliotecaBD.execSQL("DELETE FROM " + tab1 + " WHERE _id = ?", new Object[]{i});
            }

        }
    }
}