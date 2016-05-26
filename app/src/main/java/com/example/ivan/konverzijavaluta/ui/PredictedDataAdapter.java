package com.example.ivan.konverzijavaluta.ui;

/**
 * Created by ivan on 5/26/2016.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ivan.konverzijavaluta.R;
import com.example.ivan.konverzijavaluta.entitet.TecajnaLista;
import com.example.ivan.konverzijavaluta.entitet.TecajnaListaPredicted;
import com.example.ivan.konverzijavaluta.entitet.TecajnaListaWrapper;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * Created by ivan on 5/26/2016.
 */
public class PredictedDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Listener {
//        void onItemClicked(AudioExercise p_item);
    }

    private Context m_context;
    private TecajnaListaWrapper m_items = new TecajnaListaWrapper();

    private Listener m_listener;

    public PredictedDataAdapter(Context p_context) {
        m_context = p_context;
    }

    public void registerListener(Listener p_listener) {
        m_listener = p_listener;
    }

    public void setItems(TecajnaListaWrapper p_tecajnListaWrapper) {
        m_items = p_tecajnListaWrapper;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup p_parent, int p_viewType) {
        View v;
        v = LayoutInflater.from(m_context).inflate(R.layout.data_list_item, p_parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TecajnaListaPredicted tecajnaListaPredicted = m_items.getTecajnaListaPredicted().get(position);
        TecajnaLista tecajnaLista = null;
        if (!m_items.getTecajnaLista().isEmpty()) {
            tecajnaLista = m_items.getTecajnaLista().get(position);
        }

        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.m_firstRow.setText(tecajnaListaPredicted.getDrzava().getValuta());
        viewHolder.m_secondRow.setText(tecajnaListaPredicted.getSrednjiTecaj().toString());

        if (tecajnaLista == null) {
            viewHolder.m_thirdRow.setText(R.string.no_real_data);
        } else {
            viewHolder.m_thirdRow.setText(tecajnaLista.getSrednjiTecaj().toString());
        }
    }

    @Override
    public int getItemCount() {
        return m_items.getTecajnaListaPredicted().size();
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    public void clear() {
        m_items.getTecajnaLista().clear();
        m_items.getTecajnaListaPredicted().clear();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.first_row)  TextView m_firstRow;
        @InjectView(R.id.second_row) TextView m_secondRow;
        @InjectView(R.id.third_row)  TextView m_thirdRow;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

}
