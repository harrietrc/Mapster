package com.mapster.map.models;

/**
 * Created by tommyngo on 11/06/15.
 */
public class Instruction {
    private String _instruction;

    public Instruction (String instruction){
        _instruction = instruction;
    }

    public String getInstruction(){
        return _instruction;
    }

    public void setInstruction(String instruction){
        _instruction = instruction;
    }
}
