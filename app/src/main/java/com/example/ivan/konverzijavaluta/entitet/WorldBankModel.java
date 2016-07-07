package com.example.ivan.konverzijavaluta.entitet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Created by ivan on 7/7/2016.
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorldBankModel {

    private double m_value;
    private String m_date;
    private int    m_decimal;

    public double getValue() {
        return m_value;
    }

    public void setValue(double p_value) {
        m_value = p_value;
    }

    public String getDate() {
        return m_date;
    }

    public void setDate(String p_date) {
        m_date = p_date;
    }

    public int getDecimal() {
        return m_decimal;
    }

    public void setDecimal(int p_decimal) {
        m_decimal = p_decimal;
    }
}
