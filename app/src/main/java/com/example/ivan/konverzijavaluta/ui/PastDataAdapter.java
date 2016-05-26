package com.example.ivan.konverzijavaluta.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ivan.konverzijavaluta.R;
import com.example.ivan.konverzijavaluta.entitet.TecajnaLista;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by ivan on 5/26/2016.
 */
public class PastDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Listener {
//        void onItemClicked(AudioExercise p_item);
    }

    private Context m_context;
    private List<Object> m_items = new ArrayList<>();

    private Listener m_listener;

    public PastDataAdapter(Context p_context) {
        m_context = p_context;
    }

    public void registerListener(Listener p_listener) {
        m_listener = p_listener;
    }

    public void setItems(List<TecajnaLista> p_sections) {
        m_items.addAll(p_sections);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup p_parent, int p_viewType) {
        View v;
        v = LayoutInflater.from(m_context).inflate(R.layout.past_data_list_item, p_parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TecajnaLista tecajnaLista = (TecajnaLista) m_items.get(position);

        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.m_dan.setText(tecajnaLista.getDan().getDan().toString());
        viewHolder.m_drzava.setText(tecajnaLista.getDrzava().getValuta());
        viewHolder.m_valuta.setText(tecajnaLista.getSrednjiTecaj().toString());
    }

    @Override
    public int getItemCount() {
        return m_items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    public void clear() {
        m_items.clear();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.dan)    TextView m_dan;
        @InjectView(R.id.drzava) TextView m_drzava;
        @InjectView(R.id.valuta) TextView m_valuta;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

}
