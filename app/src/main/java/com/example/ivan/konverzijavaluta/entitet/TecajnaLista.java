package com.example.ivan.konverzijavaluta.entitet;

/**
 * Created by Ivan on 12.4.2016..
 */
public class TecajnaLista {

    private int    id;
    private Drzava drzava;
    private Dan    dan;
    private String kupovniTecaj;
    private String srednjiTecaj;
    private String proidajniTecaj;

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

    public int getId() {
        return id;
    }

    public void setId(int p_id) {
        id = p_id;
    }

    public String getKupovniTecaj() {
        return kupovniTecaj;
    }

    public void setKupovniTecaj(String p_kupovniTecaj) {
        kupovniTecaj = p_kupovniTecaj;
    }

    public String getProidajniTecaj() {
        return proidajniTecaj;
    }

    public void setProidajniTecaj(String p_proidajniTecaj) {
        proidajniTecaj = p_proidajniTecaj;
    }

    public String getSrednjiTecaj() {
        return srednjiTecaj;
    }

    public void setSrednjiTecaj(String p_srednjiTecaj) {
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
        int result = id;
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
