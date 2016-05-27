package com.example.ivan.konverzijavaluta.entitet;

import java.math.BigDecimal;

/**
 * Created by ivan on 5/25/2016.
 */
public class TecajnaListaPredicted {

    private Long       id;
    private Drzava     drzava;
    private Dan        dan;
    private BigDecimal kupovniTecaj;
    private BigDecimal srednjiTecaj;
    private BigDecimal prodajniTecaj;
    private boolean    soloPredicted;

    public Dan getDan() {
        return dan;
    }

    public void setDan(Dan p_dan) {
        dan = p_dan;
    }

    public Drzava getDrzava() {
        return drzava;
    }

    public void setDrzava(Drzava p_drzava) {
        drzava = p_drzava;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long p_id) {
        id = p_id;
    }

    public BigDecimal getKupovniTecaj() {
        return kupovniTecaj;
    }

    public void setKupovniTecaj(BigDecimal p_kupovniTecaj) {
        kupovniTecaj = p_kupovniTecaj;
    }

    public BigDecimal getProdajniTecaj() {
        return prodajniTecaj;
    }

    public void setProdajniTecaj(BigDecimal p_prodajniTecaj) {
        prodajniTecaj = p_prodajniTecaj;
    }

    public BigDecimal getSrednjiTecaj() {
        return srednjiTecaj;
    }

    public void setSrednjiTecaj(BigDecimal p_srednjiTecaj) {
        srednjiTecaj = p_srednjiTecaj;
    }

    public boolean isSoloPredicted() {
        return soloPredicted;
    }

    public void setSoloPredicted(boolean p_soloPredicted) {
        soloPredicted = p_soloPredicted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TecajnaListaPredicted that = (TecajnaListaPredicted) o;

        if (soloPredicted != that.soloPredicted) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (!drzava.equals(that.drzava)) return false;
        if (!dan.equals(that.dan)) return false;
        if (!kupovniTecaj.equals(that.kupovniTecaj)) return false;
        if (!srednjiTecaj.equals(that.srednjiTecaj)) return false;
        return prodajniTecaj.equals(that.prodajniTecaj);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + drzava.hashCode();
        result = 31 * result + dan.hashCode();
        result = 31 * result + kupovniTecaj.hashCode();
        result = 31 * result + srednjiTecaj.hashCode();
        result = 31 * result + prodajniTecaj.hashCode();
        result = 31 * result + (soloPredicted ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TecajnaListaPredicted{" +
                "id=" + id +
                ", drzava=" + drzava +
                ", dan=" + dan +
                ", kupovniTecaj=" + kupovniTecaj +
                ", srednjiTecaj=" + srednjiTecaj +
                ", prodajniTecaj=" + prodajniTecaj +
                ", soloPredicted=" + soloPredicted +
                '}';
    }
}
