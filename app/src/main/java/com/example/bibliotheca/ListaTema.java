package com.example.bibliotheca;

public class ListaTema extends ListaBase {

    @Override
    protected int getSpinnerText() {
        return R.string.select_theme;
    }

    @Override
    protected String getTabToDialog() {
        return "Temas";
    }

    @Override
    protected String getColToDialog() {
        return "tema";
    }

    @Override
    protected String getSqlToListView() {
        return "SELECT Livros._id, titulo_original, titulo_pt_br, obtido, " +
                "REPLACE(GROUP_CONCAT(DISTINCT Autores.nome), ',', ', ' ) AS autores, " +
                "REPLACE(GROUP_CONCAT(DISTINCT Categorias.categoria), ',', ', ' ) AS categorias, " +
                "REPLACE(GROUP_CONCAT(DISTINCT Temas.tema), ',', ', ' ) AS temas " +
                "FROM Livros " +
                "JOIN Livros_Autores ON Livros._id = Livros_Autores.livro_id " +
                "LEFT JOIN Autores ON Autores._id = Livros_Autores.autor_id " +
                "LEFT JOIN Livros_Categorias ON Livros._id = Livros_Categorias.livro_id " +
                "LEFT JOIN Categorias ON Categorias._id = Livros_Categorias.categoria_id " +
                "LEFT JOIN Livros_Temas ON Livros._id = Livros_Temas.livro_id " +
                "LEFT JOIN Temas ON Temas._id = Livros_Temas.tema_id " +
                "GROUP BY Livros._id HAVING GROUP_CONCAT(Temas.tema) LIKE ?";
    }
}