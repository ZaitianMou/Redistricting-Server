package com.example.zaitian.CSE416server.model;
import lombok.Getter;
import lombok.Setter;
import javax.json.*;
import javax.persistence.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name="job")
@Getter
@Setter
public class Job {
    @Id
    private long id;

    @Column(name="state")
    private String state;

    @Column(name ="number_of_districtings")
    private int numberOfDistrictings;

    @Column(name= "compactness_limit")
    private double compactnessLimit;

    @Column(name="population_diff_Limit")
    private double populationDiffLimit;

    @Column(name="status")
    private String status; // "running","finished"

    @Column(name="result_location")
    private String resultLocation;

    @Transient
    private JobResult jobResult;

    @Transient
    private int fucker;

    public Job() {
    }

    @Override
    public String toString(){
        return "Job id "+this.getId();
    }

    public void cancelRunning(){
        this.setStatus("aborted");
        //TODO: call the server or seawulf to cancel

    }
    public void startRunning() throws IOException {
        this.setStatus("running");
        //TODO: call the server or seawulf to start the job
        //hint: using process builder
        if (getNumberOfDistrictings()> Configuration.runningLocationThreshold){

         }

        else{
            List<String> commandList=new ArrayList<>();
            commandList.add("python3 ./../../../script.py");

            ProcessBuilder process= new ProcessBuilder("python","./../../..src/script.py");
            //process.start()
        }

    }
    public boolean checkResult(){
        //TODO: ask whether job is done
        return true;
    }
    //precondition: already finished.
    public String getResult(long id) throws FileNotFoundException {
        //Read result
        JsonReader reader = Json.createReader(new FileInputStream("src/main/resources/result/results.json"));
        JsonObject file = reader.readObject();
        JsonArray districtingPlansJson=file.getJsonArray("districtingPlans");

        List<DistrictingPlan> plans = new ArrayList<>();
        for (int i=0;i<districtingPlansJson.size();i++) {
            JsonObject districtingJson=districtingPlansJson.getJsonObject(i);
            JsonArray districtsJson = districtingJson.getJsonArray("districts");
            JsonNumber districtingPlanID = districtingJson.getJsonNumber("districtingPlanID");
            List<District> districts = new ArrayList<>();
            for (int j = 0; j < districtsJson.size(); j++) {
                JsonObject districtJson = districtsJson.getJsonObject(j);
                int hvap = districtJson.getJsonNumber("HVAP").intValue();
                int wvap = districtJson.getJsonNumber("WVAP").intValue();
                int bvap = districtJson.getJsonNumber("BVAP").intValue();
                int aminvap = districtJson.getJsonNumber("AMINVAP").intValue();
                int asianvap = districtJson.getJsonNumber("ASIANVAP").intValue();
                int nhpivap = districtJson.getJsonNumber("NHPIVAP").intValue();
                JsonArray precincts = districtJson.getJsonArray("precincts");
                List<Integer> l = new ArrayList<>();
                for (JsonNumber p : precincts.getValuesAs(JsonNumber.class)) {
                    l.add(p.intValue());
                }
                districts.add(new District(hvap, wvap, bvap, aminvap, asianvap, nhpivap, l));
            }
            DistrictingPlan plan = new DistrictingPlan(districtingPlanID.intValue(), districts);
            System.out.println(plan.toString());
            plans.add(plan);
        }
        JobResult result=new JobResult(plans,this.getState());
        this.jobResult=result;
        System.out.println(">>"+this.jobResult);
        jobResult.processResult();

        this.setFucker(1);
        return "";
    }

    //precondition: already finished.
    public String getBoxplot(long id) throws FileNotFoundException {
        System.out.println(this.fucker);
        System.out.println(this.jobResult);
        return this.jobResult.generateBoxplotJson();
    }
}

enum Minority{
    H,
    W,
    B,
    AMIN,
    ASIAN,
    NHPI
}