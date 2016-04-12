package com.example.ivan.konverzijavaluta.entitet;

/**
 * Created by Ivan on 12.4.2016..
 */
public class Drzava {

    private int    id;
    private String sifra;
    private String valuta;
    private int    jedinica;

    public int getId() {
        return id;
    }

    public void setId(int p_id) {
        id = p_id;
    }

    public int getJedinica() {
        return jedinica;
    }

    public void setJedinica(int p_jedinica) {
        jedinica = p_jedinica;
    }

    public String getSifra() {
        return sifra;
    }

    public void setSifra(String p_sifra) {
        sifra = p_sifra;
    }

    public String getValuta() {
        return valuta;
    }

    public void setValuta(String p_valuta) {
        valuta = p_valuta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Drzava)) return false;

        Drzava drzava = (Drzava) o;

        if (id != drzava.id) return false;
        if (jedinica != drzava.jedinica) return false;
        if (!sifra.equals(drzava.sifra)) return false;
        return valuta.equals(drzava.valuta);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + sifra.hashCode();
        result = 31 * result + valuta.hashCode();
        result = 31 * result + jedinica;
        return result;
    }

    @Override
    public String toString() {
        return "Drzava{" +
                "id=" + id +
                ", sifra='" + sifra + '\'' +
                ", valuta='" + valuta + '\'' +
                ", jedinica=" + jedinica +
                '}';
    }
}
