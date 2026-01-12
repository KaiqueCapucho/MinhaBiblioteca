package com.example.bibliotheca;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AutoComAdapter extends ArrayAdapter<String> {
    private final List<String> originalList;
    private final List<String> filteredList;


    public AutoComAdapter(Context context, List<String> data) {
        super(context,android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        this.originalList = data;
        this.filteredList = new ArrayList<>();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return filter;
    }

    //Configuração da Classe/Atributo Filter
    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            filteredList.clear();
            if (charSequence != null) {
                String query = charSequence.toString().toLowerCase(); // texto digitado

                // Faz a filtragem por substring
                for (String item : originalList) {
                    if (item.toLowerCase().contains(query)) {
                        filteredList.add(item);
                    }
                }
            }
            // Coloca os resultados no objeto FilterResults (p/exibição)
            FilterResults results = new FilterResults();
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            clear();
            if (filterResults != null && filterResults.count > 0) {
                addAll((List<String>) filterResults.values);
            }
            notifyDataSetChanged();
        }
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return (String) resultValue;
        }
    };

}


