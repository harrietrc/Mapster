package com.mapster.map.models;

import com.mapster.date.CustomDate;

/**
 * Created by tommyngo on 11/06/15.
 */
public class Path {
    private Distance _distance;
    private Instruction _instruction;
    private Duration _duration;
    private CustomDate _date;
    private String _mode;

    public Path (Distance distance, Instruction instruction, Duration duration, CustomDate date, String mode){
        _distance = distance;
        _instruction = instruction;
        _duration = duration;
        _date = date;
        _mode = mode;
    }

    public Path (Distance distance, Instruction instruction, Duration duration, String mode){
        _distance = distance;
        _instruction = instruction;
        _duration = duration;
        _mode = mode;
    }

    public Distance getDistance() {
        return _distance;
    }

    public void setDistance(Distance _distance) {
        this._distance = _distance;
    }

    public Instruction getInstruction() {
        return _instruction;
    }

    public void setInstruction(Instruction _instruction) {
        this._instruction = _instruction;
    }

    public Duration getDuration() {
        return _duration;
    }

    public void setDuration(Duration _duration) {
        this._duration = _duration;
    }

    public void setDate(CustomDate date){ _date = date; }

    public CustomDate getDate() { return _date; }

    public String getMode(){ return _mode; }

    public void setMode(String mode) { _mode = mode; }
}
