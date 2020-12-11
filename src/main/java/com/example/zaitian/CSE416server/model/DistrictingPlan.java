package com.example.zaitian.CSE416server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
public class DistrictingPlan {

    private int districtingPlanID;
    private List<District> districts;

    @Override
    public String toString() {
        String s="";
        for (District d: districts){
            s=s+d.toString();
        }
        return "DistrictingPlan{" +
                " districtingPlanID=" + districtingPlanID +
                s +
                '}';
    }
}
