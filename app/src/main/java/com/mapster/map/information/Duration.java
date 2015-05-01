package com.mapster.map.information;

/**
 * Created by tommyngo on 1/05/15.
 */
public class Duration {
    private String _name;
    private int _value;
    public Duration(String name, int value){
        this._name = name;
        this._value = value;
    }

    public Duration(){

    }

    public void setValue(int value){
        _value = value;
    }

    public void setName(String name){
        _name = name;
    }

    public int getValue(){
        return _value;
    }

    public String getName(){
        return _name;
    }
}
