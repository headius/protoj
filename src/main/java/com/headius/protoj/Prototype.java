package com.headius.protoj;

/**
 * The base prototype has no properties.
 */
public class Prototype {
    private static final String[] EMPTY = new String[0];

    public Prototype(){}
    public Prototype(Prototype base){}

    public String[] properties() {
        return EMPTY;
    }
}
