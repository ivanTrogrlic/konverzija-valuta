package com.example.ivan.konverzijavaluta.entitet;

import java.math.BigDecimal;

/**
 * Created by Ivan on 12.4.2016..
 */
public class TecajnaLista {

    private long       id;
    private Drzava     drzava;
    private Dan        dan;
    private BigDecimal kupovniTecaj;
    private BigDecimal srednjiTecaj;
    private BigDecimal proidajniTecaj;

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

    public long getId() {
        return id;
    }

    public void setId(long p_id) {
        id = p_id;
    }

    public BigDecimal getKupovniTecaj() {
        return kupovniTecaj;
    }

    public void setKupovniTecaj(BigDecimal p_kupovniTecaj) {
        kupovniTecaj = p_kupovniTecaj;
    }

    public BigDecimal getProidajniTecaj() {
        return proidajniTecaj;
    }

    public void setProidajniTecaj(BigDecimal p_proidajniTecaj) {
        proidajniTecaj = p_proidajniTecaj;
    }

    public BigDecimal getSrednjiTecaj() {
        return srednjiTecaj;
    }

    public void setSrednjiTecaj(BigDecimal p_srednjiTecaj) {
        srednjiTecaj = p_srednjiTecaj;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TecajnaLista)) return false;

        TecajnaLista that = (TecajnaLista) o;

        if (id != that.id) return false;
        if (!drzava.equals(that.drzava)) return false;
        if (!dan.equals(that.dan)) return false;
        if (!kupovniTecaj.equals(that.kupovniTecaj)) return false;
        if (!srednjiTecaj.equals(that.srednjiTecaj)) return false;
        return proidajniTecaj.equals(that.proidajniTecaj);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + drzava.hashCode();
        result = 31 * result + dan.hashCode();
        result = 31 * result + kupovniTecaj.hashCode();
        result = 31 * result + srednjiTecaj.hashCode();
        result = 31 * result + proidajniTecaj.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TecajnaLista{" +
                "dan=" + dan +
                ", id=" + id +
                ", drzava=" + drzava +
                ", kupovniTecaj='" + kupovniTecaj + '\'' +
                ", srednjiTecaj='" + srednjiTecaj + '\'' +
                ", proidajniTecaj='" + proidajniTecaj + '\'' +
                '}';
    }
}
