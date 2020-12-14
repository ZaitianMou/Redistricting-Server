package com.example.zaitian.CSE416server.model;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class District {
    private int hvap;
    private int wvap;
    private int bvap;
    private int aminvap;
    private int asianvap;
    private int nhpivap;
    private int totalVap;
    private List<Integer> precincts;

    private int countyNum; //TODO: usecase 38

    public District(int hvap, int wvap, int bvap, int aminvap, int asianvap, int nhpivap, int totalvap,List<Integer> precincts) {
        this.hvap = hvap;
        this.wvap = wvap;
        this.bvap = bvap;
        this.aminvap = aminvap;
        this.asianvap = asianvap;
        this.nhpivap = nhpivap;
        this.totalVap=totalvap;
        this.precincts = precincts;
    }

    @Override
    public String toString() {
        return "District{" +
                "hvap=" + hvap +
                ", wvap=" + wvap +
                ", bvap=" + bvap +
                ", aminvap=" + aminvap +
                ", asianvap=" + asianvap +
                ", nhpivap=" + nhpivap +
                '}';
    }
}
