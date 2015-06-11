package com.mapster.map.information;

/**
 * Created by tommyngo on 11/06/15.
 */
public class Path {
    private Distance _distance;
    private Instruction _instruction;
    private Duration _duration;

    public Path (Distance distance, Instruction instruction, Duration duration){
        _distance = distance;
        _instruction = instruction;
        _duration = duration;
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
}
