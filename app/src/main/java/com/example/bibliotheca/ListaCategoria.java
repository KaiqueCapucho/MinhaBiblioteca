package com.example.bibliotheca;

public class ListaCategoria extends ListaBase {
    @Override
    protected int getSpinnerText() {
        return R.string.select_category;
    }

    @Override
    protected String getTabToDialog() {
        return "Categorias";
    }

    @Override
    protected String getColToDialog() {
        return "categoria";
    }

    @Override
    protected String getSqlToListView() {
        return "SELECT Livros._id, titulo_original, titulo_pt_br, obtido, " +
                "REPLACE(GROUP_CONCAT(DISTINCT Autores.nome), ',', ', ') AS autores, " +
                "REPLACE(GROUP_CONCAT(DISTINCT Categorias.categoria), ',', ', ') AS categorias " +
                "FROM Livros " +
                "LEFT JOIN Livros_Autores ON Livros._id = Livros_Autores.livro_id " +
                "LEFT JOIN Autores ON Autores._id = Livros_Autores.autor_id " +
                "LEFT JOIN Livros_Categorias ON Livros._id = Livros_Categorias.livro_id " +
                "LEFT JOIN Categorias ON Categorias._id = Livros_Categorias.categoria_id " +
                "WHERE Livros._id IN" +
                "   (SELECT Livros._id " +
                "   FROM Livros" +
                "   LEFT JOIN Livros_Categorias ON Livros._id = Livros_Categorias.livro_id" +
                "   LEFT JOIN Categorias ON Categorias._id = Livros_Categorias.categoria_id" +
                "   WHERE Categorias.categoria LIKE ? )" +
                "GROUP BY Livros._id";
    }

}

