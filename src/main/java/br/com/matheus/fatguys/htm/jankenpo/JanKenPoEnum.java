/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.matheus.fatguys.htm.jankenpo;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author y2gh
 */
public enum JanKenPoEnum {

    JAN(0, "JAN"),
    KEN(1, "KEN"),
    PO(2, "PO");

    public static List<String> valuesAsStringList() {
        List<String> ret = new ArrayList();
        for (JanKenPoEnum value : JanKenPoEnum.values()) {
            ret.add(value.toString());
        }
        return ret;
    }

    private int id;
    private String pattern;

    JanKenPoEnum(int id, String pattern) {
        this.id = id;
        this.pattern = pattern;
    }

    public static JanKenPoEnum getFromId(int id) {
        for (JanKenPoEnum value : JanKenPoEnum.values()) {
            if (value.id==id) {
                return value;
            }
        }
        return null;
    }
    public static JanKenPoEnum getFromPattern(String pattern) {
        for (JanKenPoEnum value : JanKenPoEnum.values()) {
            if (value.pattern.equals(pattern)) {
                return value;
            }
        }
        return null;
    }

    public boolean wins(JanKenPoEnum other) {
        return this.equals(winner(other));
    }

    public JanKenPoEnum winner(JanKenPoEnum other) {
        if(other==null){
            return null;
        }
        switch (other) {
            case JAN:
                return KEN;
            case KEN:
                return PO;
            case PO:
                return JAN;
            default:
                return null;
        }
    }
    
    public JanKenPoEnum winner() {
        return winner(this);
    }
}
