package com.example.projdroid.models;


public class LocalTime {
    private int hour;
    private int minute;
    private int nano;
    private int second;

    // Construtor
    public LocalTime(int hour, int minute, int second, int nano) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.nano = nano;
    }

    // Getters e Setters
    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getNano() {
        return nano;
    }

    public void setNano(int nano) {
        this.nano = nano;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    // Método toString para exibição
    @Override
    public String toString() {
        return String.format("LocalTime{hour=%d, minute=%d, second=%d, nano=%d}", hour, minute, second, nano);
    }
}
