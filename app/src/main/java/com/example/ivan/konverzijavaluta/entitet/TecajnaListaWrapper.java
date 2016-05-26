package com.example.ivan.konverzijavaluta.entitet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivan on 5/26/2016.
 */
public class TecajnaListaWrapper {

    private List<TecajnaLista>          m_tecajnaLista;
    private List<TecajnaListaPredicted> m_tecajnaListaPredicted;

    public TecajnaListaWrapper() {
        m_tecajnaLista = new ArrayList<>();
        m_tecajnaListaPredicted = new ArrayList<>();
    }

    public List<TecajnaLista> getTecajnaLista() {
        return m_tecajnaLista;
    }

    public void setTecajnaLista(List<TecajnaLista> p_tecajnaLista) {
        m_tecajnaLista = p_tecajnaLista;
    }

    public List<TecajnaListaPredicted> getTecajnaListaPredicted() {
        return m_tecajnaListaPredicted;
    }

    public void setTecajnaListaPredicted(
            List<TecajnaListaPredicted> p_tecajnaListaPredicted) {
        m_tecajnaListaPredicted = p_tecajnaListaPredicted;
    }
}
