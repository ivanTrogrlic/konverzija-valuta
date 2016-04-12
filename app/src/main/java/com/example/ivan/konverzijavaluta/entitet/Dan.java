package com.example.ivan.konverzijavaluta.entitet;

import java.sql.Date;

/**
 * Created by Ivan on 12.4.2016..
 */
public class Dan {

    private Date dan;

    public Date getDan() {
        return dan;
    }

    public void setDan(Date p_dan) {
        dan = p_dan;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dan)) return false;

        Dan dan1 = (Dan) o;

        return dan.equals(dan1.dan);

    }

    @Override
    public int hashCode() {
        return dan.hashCode();
    }

    @Override
    public String toString() {
        return "Dan{" +
                "dan=" + dan +
                '}';
    }
}
