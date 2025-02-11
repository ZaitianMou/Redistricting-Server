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
        if (getNumberOfDistrictings() < Configuration.runningLocationThreshold){
            // remove job from db
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
            StringBuilder state_file = new StringBuilder();
            state_file.append("_refined.json");
            int numberOfDistricting=0;
            switch(this.state) {
                case "GA":
                    state_file.insert(0, "src/main/java/resources/data/GA/GA");
                    numberOfDistricting=Configuration.GA_number_of_district;
                    break;
                case "TX":
                    state_file.insert(0, "src/main/java/resources/data/TX/TX");
                    numberOfDistricting=Configuration.TX_number_of_district;
                    break;
                case "VA":
                    state_file.insert(0, "src/main/java/resources/data/VA/VA");
                    numberOfDistricting=Configuration.VA_number_of_district;
                    break;
                default:
                    break;
            }
            try {
                ProcessBuilder process = new ProcessBuilder("python", "src/main/resources/script/algorithm", state_file.toString(),
                        String.valueOf(this.populationDiffLimit), String.valueOf(this.compactnessLimit), String.valueOf(numberOfDistricting));

                File log = new File("src/main/resources/result/redistrict.log");
                process.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
                process.start();
                this.slurmID=0;
            }catch (Exception e){
                System.out.println("Error when startRunning!");
            }
        }
        else{
            StringBuilder state_file = new StringBuilder();
            state_file.append("_refined.json");
            int numberOfDistricting=0;
            switch(this.state) {
                case "GA":
                    state_file.insert(0, "GA");
                    numberOfDistricting=Configuration.GA_number_of_district;
                    break;
                case "TX":
                    state_file.insert(0, "TX");
                    numberOfDistricting=Configuration.TX_number_of_district;
                    break;
                case "VA":
                    state_file.insert(0, "VA");
                    numberOfDistricting=Configuration.VA_number_of_district;
                    break;
                default:
                    break;
            }
            try {
                String plans = "mpirun -np " + this.numberOfDistrictings + " python algorithm.py" + " " + state_file.toString()
                        + " " + this.populationDiffLimit + " " + this.compactnessLimit + " " + numberOfDistricting;
                ProcessBuilder process = new ProcessBuilder("src/main/resources/script/trigger_submit.sh", plans);
                Process ps = process.start();
                ps.waitFor();
                BufferedReader reader = new BufferedReader(new InputStreamReader(ps.getInputStream()));
                String s=reader.readLine();
                System.out.println(s);
                this.slurmID=Integer.parseInt(s.split(" ")[3]);
            }catch (Exception e){
                System.out.println("Error when startRunning!");
                System.out.println(e.getMessage());
            }

        }
    }
    public boolean checkResult(){
        if (getNumberOfDistrictings() < Configuration.runningLocationThreshold){
            try {
                String str = new String(Files.readAllBytes(Paths.get("src/main/resources/result/redistrict.log")), StandardCharsets.UTF_8);
                if(str.length() < 100) {
                    System.out.println("Job "+this.id + " is still running.");
                    return false;
                }
                else {
                    str = "[" + str.substring(str.indexOf("{"));
                    str= str.substring(0,str.length()-2)+"]";
                    PrintWriter out = new PrintWriter("src/main/resources/result/redistrict_revised.json");
                    out.print(str);
                    return true;
                }
            }
            catch (Exception e) {}
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
                    PrintWriter out = new PrintWriter("src/main/resources/result/redistrict_revised.json");
                    out.print(str);
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
        //decide if file already exist
        if (new File("src/main/resources/districting/"+id+"_districtings.json").isFile()){
            String str = new String(Files.readAllBytes(Paths.get("src/main/resources/districting/"+id+"_districtings.json")), StandardCharsets.UTF_8);
            return str;
        }
        else {
            //Read result
            JsonReader reader = Json.createReader(new FileInputStream("src/main/resources/result/redistrict_revised.json"));
            JsonArray file = reader.readArray();
            List<DistrictingPlan> plans = new ArrayList<>();
            for (int i = 0; i < file.size(); i++) {
                JsonObject districtingJson = file.getJsonObject(i);
                JsonArray districtsJson = districtingJson.getJsonArray("districts");
                List<District> districts = new ArrayList<>();
                for (int j = 0; j < districtsJson.size(); j++) {
                    JsonObject districtJson = districtsJson.getJsonObject(j);
                    int hvap = districtJson.getJsonNumber("HVAP").intValue();
                    int wvap = districtJson.getJsonNumber("WVAP").intValue();
                    int bvap = districtJson.getJsonNumber("BVAP").intValue();
                    int aminvap = districtJson.getJsonNumber("AMINVAP").intValue();
                    int asianvap = districtJson.getJsonNumber("ASIANVAP").intValue();
                    int nhpivap = districtJson.getJsonNumber("NHPIVAP").intValue();
                    int totalvap = hvap + wvap + bvap + aminvap + asianvap + nhpivap;
                    JsonArray precincts = districtJson.getJsonArray("precincts");
                    List<Integer> l = new ArrayList<>();
                    for (JsonNumber p : precincts.getValuesAs(JsonNumber.class)) {
                        l.add(p.intValue());
                    }
                    districts.add(new District(hvap, wvap, bvap, aminvap, asianvap, nhpivap, totalvap, l));
                }
                DistrictingPlan plan = new DistrictingPlan((i + 1), districts);
                System.out.println(plan.toString());
                plans.add(plan);
            }
            JobResult result = new JobResult(plans, this.state, this.compactnessLimit, this.populationDiffLimit,this.id);
            this.jobResult = result;
            jobResult.processResult();

            return new String(Files.readAllBytes(Paths.get("src/main/resources/districting/"+id+"_districtings.json")), StandardCharsets.UTF_8);
        }
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