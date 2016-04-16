package com.example.ivan.konverzijavaluta.entitet;

import java.util.Date;

/**
 * Created by Ivan on 12.4.2016..
 */
public class Dan {

    private long id;
    private Date dan;

    public long getId() {
        return id;
    }

    public void setId(long p_id) {
        id = p_id;
    }

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

        if (id != dan1.id) return false;
        return dan.equals(dan1.dan);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
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
