package com.example.ivan.konverzijavaluta.entitet;

import org.joda.time.LocalDate;

/**
 * Created by Ivan on 12.4.2016..
 */
public class Dan {

    private Long      id;
    private LocalDate dan;

    public Long getId() {
        return id;
    }

    public void setId(Long p_id) {
        id = p_id;
    }

    public LocalDate getDan() {
        return dan;
    }

    public void setDan(LocalDate p_dan) {
        dan = p_dan;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dan)) return false;

        Dan dan1 = (Dan) o;

        if (id != null ? !id.equals(dan1.id) : dan1.id != null) return false;
        return dan.equals(dan1.dan);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + dan.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Dan{" +
                "dan=" + dan +
                ", id=" + id +
                '}';
    }
}
