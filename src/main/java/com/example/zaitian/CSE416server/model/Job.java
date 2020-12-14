package com.example.zaitian.CSE416server.model;
import lombok.Getter;
import lombok.Setter;
import javax.json.*;
import javax.persistence.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    @Column(name="slurm_ID")
    private int slurmID;

    public Job() {
    }

    @Override
    public String toString(){
        return "Job id "+this.getId();
    }

    public void cancelRunning() throws IOException {
        //TODO: call the server or seawulf to cancel
        if (getNumberOfDistrictings() < Configuration.runningLocationThreshold){

        }

        else{
            ProcessBuilder process= new ProcessBuilder("src/main/resources/script/trigger_cancel.sh", String.valueOf(this.slurmID));
            Process ps = process.start();
            try {
                ps.waitFor();
            }
            catch(Exception e){}
            System.out.println("Job " + this.id + " slurm id " + this.slurmID + " canceled with code " + ps.exitValue());
        }
    }
    public void startRunning() throws IOException {
        this.setStatus("running");
        //TODO: call the server or seawulf to start the job
        if (getNumberOfDistrictings() < Configuration.runningLocationThreshold){

        }

        else{
            StringBuilder state_file = new StringBuilder();
            state_file.append("_refined.json");
            switch(this.state) {
                case "GA":
                    state_file.insert(0, "GA");
                    break;
                case "TX":
                    state_file.insert(0, "TX");
                    break;
                case "VA":
                    state_file.insert(0, "VA");
                    break;
                default:
                    break;
            }
            String plans = "mpirun -np " + String.valueOf(this.numberOfDistrictings) + " python algorithm.py" + " " + state_file.toString()
                    + " " + String.valueOf(this.populationDiffLimit) + " " + String.valueOf(this.compactnessLimit);
            ProcessBuilder process= new ProcessBuilder("src/main/resources/script/trigger_submit.sh", plans);
            Process ps = process.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            String retval = reader.readLine();
            this.slurmID=Integer.parseInt(retval.split(" ")[3]);
            System.out.println(retval);
        }

    }
    public boolean checkResult(){
        if (getNumberOfDistrictings() < Configuration.runningLocationThreshold){

        }
        else{
            ProcessBuilder process= new ProcessBuilder("src/main/resources/script/trigger_getresult.sh");

            try {
                Process ps = process.start();
                ps.waitFor();
                System.out.println("Get result with exit code " + ps.exitValue());
                if(ps.exitValue() == 0) {

                    String str = new String(Files.readAllBytes(Paths.get("src/main/resources/result/redistrict.log")), StandardCharsets.UTF_8);
                    str = "[" + str.substring(str.indexOf("{"));
                    str= str.substring(0,str.length()-2)+"]";
                    PrintWriter out = new PrintWriter("src/main/resources/result/test.json");
                    out.print(str);
//                    System.out.println("File: " + str);
                }
                else {
                    System.out.println("Job "+this.id + " is still running.");
                    return false;
                }

            }
            catch(Exception e){
                System.out.println("check result failed "+e);
            }
        }
        return true;
    }
    //precondition: already finished.
    public String getResult(long id) throws IOException, InterruptedException {
        //Read result
        JsonReader reader = Json.createReader(new FileInputStream("src/main/resources/result/redistrict-lastest.json"));
//        JsonObject file = reader.readObject();
        JsonArray file =reader.readArray();
//        JsonArray districtingPlansJson=file.getJsonArray("districtingPlans");

        List<DistrictingPlan> plans = new ArrayList<>();
        for (int i=0;i<file.size();i++) {
            JsonObject districtingJson=file.getJsonObject(i);
            JsonArray districtsJson = districtingJson.getJsonArray("districts");
//            JsonNumber districtingPlanID = districtingJson.getJsonNumber("districtingPlanID");
            List<District> districts = new ArrayList<>();
            for (int j = 0; j < districtsJson.size(); j++) {
                JsonObject districtJson = districtsJson.getJsonObject(j);
                int hvap = districtJson.getJsonNumber("HVAP").intValue();
                int wvap = districtJson.getJsonNumber("WVAP").intValue();
                int bvap = districtJson.getJsonNumber("BVAP").intValue();
                int aminvap = districtJson.getJsonNumber("AMINVAP").intValue();
                int asianvap = districtJson.getJsonNumber("ASIANVAP").intValue();
                int nhpivap = districtJson.getJsonNumber("NHPIVAP").intValue();
                int totalvap=hvap+wvap+bvap+aminvap+asianvap+nhpivap;
                JsonArray precincts = districtJson.getJsonArray("precincts");
                List<Integer> l = new ArrayList<>();
                for (JsonNumber p : precincts.getValuesAs(JsonNumber.class)) {
                    l.add(p.intValue());
                }
                districts.add(new District(hvap, wvap, bvap, aminvap, asianvap, nhpivap,totalvap, l));
            }
            DistrictingPlan plan = new DistrictingPlan( (i+1),districts);
            System.out.println(plan.toString());
            plans.add(plan);
        }
        JobResult result=new JobResult(plans,this.getState());
        this.jobResult=result;
        jobResult.processResult(state);

        return "";
    }

    //precondition: already finished.
//    public String getBoxplot(long id) throws FileNotFoundException {
//
//        System.out.println(this.jobResult);
//        return this.jobResult.generateBoxplotJson();
//    }
}

enum Minority{
    H,
    W,
    B,
    AMIN,
    ASIAN,
    NHPI
}