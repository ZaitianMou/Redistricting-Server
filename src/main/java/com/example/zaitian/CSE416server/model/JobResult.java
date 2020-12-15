package com.example.zaitian.CSE416server.model;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.google.common.math.Quantiles;
import javax.json.*;

@Getter
@Setter
public class JobResult {
    private List<DistrictingPlan> districtingPlans;
    private Boxplot hBoxplot, wBoxplot, bBoxplot, aminBoxplot, asianBoxplot, nhpiBoxplot;
    private int indexOfAverageDistricting, indexOfExtremeDistricting, indexOfRandomDistricting,indexOfSecondRandomDistricting;
    private double[] enactedHVAP, enactedWVAP, enactedBVAP, enactedAminVAP, enactedAsianVAP, enactedNHPIVAP;

    private String state;
    private double compactnessLimit;
    private double populationDiffLimit;
    private long id;

    public JobResult(List<DistrictingPlan> plans,String state, double compactnessLimit, double populationDiffLimit,long id){
        if (state.equals("GA")) {
            enactedHVAP = Configuration.GA_enacted_HVAP;
            enactedWVAP = Configuration.GA_enacted_WVAP;
            enactedBVAP = Configuration.GA_enacted_BVAP;
            enactedAminVAP = Configuration.GA_enacted_AminVAP;
            enactedAsianVAP = Configuration.GA_enacted_AsianVAP;
            enactedNHPIVAP = Configuration.GA_enacted_NHPIVAP;
        }
        else if (state.equals("TX")){
            enactedHVAP = Configuration.TX_enacted_HVAP;
            enactedWVAP = Configuration.TX_enacted_WVAP;
            enactedBVAP = Configuration.TX_enacted_BVAP;
            enactedAminVAP = Configuration.TX_enacted_AminVAP;
            enactedAsianVAP = Configuration.TX_enacted_AsianVAP;
            enactedNHPIVAP = Configuration.TX_enacted_NHPIVAP;
        }
        else if (state.equals("VA")){
            enactedHVAP = Configuration.VA_enacted_HVAP;
            enactedWVAP = Configuration.VA_enacted_WVAP;
            enactedBVAP = Configuration.VA_enacted_BVAP;
            enactedAminVAP = Configuration.VA_enacted_AminVAP;
            enactedAsianVAP = Configuration.VA_enacted_AsianVAP;
            enactedNHPIVAP = Configuration.VA_enacted_NHPIVAP;
        }
        else
            System.out.println("!!!!!!!BUG");
        districtingPlans=plans;
        this.state=state;
        this.compactnessLimit=compactnessLimit;
        this.populationDiffLimit=populationDiffLimit;
        this.id=id;

    }
    public void processResult() throws IOException, InterruptedException {
        System.out.println("\n===Set Boxplots===");
        setBoxPlot(Minority.H);
        setBoxPlot(Minority.W);
        setBoxPlot(Minority.B);
        setBoxPlot(Minority.AMIN);
        setBoxPlot(Minority.ASIAN);
        setBoxPlot(Minority.NHPI);

        exportBoxplotJson();

        System.out.println("\n===Determine DistrictingPlan===");
        determineDistricting();
        //TODO export districting

        System.out.println("\n===Calculate number of counties in each district===");
        calculateNumberOfCounties();

        System.out.println("\n===Generating districting jsons===");
        getDistrictsGeojson();

        System.out.println("\n===Generating job summary json===");
//        generateJobSummaryJson();

    }
    public void generateJobSummaryJson(){
        JsonObjectBuilder summary= Json.createObjectBuilder();
        summary.add("state",this.state);
        JsonObjectBuilder precinctsWrapper=Json.createObjectBuilder();

        //precincts: manully copy and paste

        summary.add("precinctsGeoJson",precinctsWrapper);
        summary.add("averageDistricting",indexOfAverageDistricting);
        summary.add("extremeDistricting",indexOfExtremeDistricting);
        summary.add("randomDistricting",indexOfRandomDistricting);
        summary.add("secondRandomDistricting",indexOfSecondRandomDistricting);
        JsonArrayBuilder districtingArray=Json.createArrayBuilder();
        districtingArray.add(getDistrictingObjectForSummary(indexOfAverageDistricting));
        districtingArray.add(getDistrictingObjectForSummary(indexOfExtremeDistricting));
        districtingArray.add(getDistrictingObjectForSummary(indexOfRandomDistricting));
        districtingArray.add(getDistrictingObjectForSummary(indexOfSecondRandomDistricting));

        summary.add("districtings",districtingArray);
        System.out.println(summary.build());
    }
    public JsonObjectBuilder getDistrictingObjectForSummary(int index){
        //add averageDistricting
        JsonObjectBuilder districtingJson=Json.createObjectBuilder();
        DistrictingPlan average=districtingPlans.get(index);
        districtingJson.add("districtingID",index);
        districtingJson.add("compactnessLimit", this.compactnessLimit);
        districtingJson.add("populationDifferenceLimit", this.populationDiffLimit);

        JsonObjectBuilder congressionDistrictsGeoJSON=Json.createObjectBuilder();
        congressionDistrictsGeoJSON.add("type","FeatureCollection");
        congressionDistrictsGeoJSON.add("description","Congressional Districts");
        JsonArrayBuilder districts=Json.createArrayBuilder();
        for (int i=0;i<average.getDistricts().size();i++){
            JsonObjectBuilder district=Json.createObjectBuilder();
            district.add("districtID",i);
            district.add("differentConuties",average.getDistricts().get(i).getCountyNum());
            district.add("AminVAP",average.getDistricts().get(i).getAminvap());
            district.add("AsianVAP",average.getDistricts().get(i).getAsianvap());
            district.add("BVAP",average.getDistricts().get(i).getBvap());
            district.add("HVAP",average.getDistricts().get(i).getHvap());
            district.add("NhpiVAP",average.getDistricts().get(i).getNhpivap());
            district.add("WVAP",average.getDistricts().get(i).getWvap());
            district.add("VAP",average.getDistricts().get(i).getTotalVap());

            JsonArrayBuilder precincts=Json.createArrayBuilder();
            for (int j=0;j<average.getDistricts().get(i).getPrecincts().size();j++){
                precincts.add((int)average.getDistricts().get(i).getPrecincts().get(j));
            }
            district.add("precincts:",precincts);
            districts.add(district);
        }
        congressionDistrictsGeoJSON.add("features",districts);
        congressionDistrictsGeoJSON.add("geometry","Displayed in the presentation.");
        districtingJson.add("congressionalDistrictsGeoJSON",congressionDistrictsGeoJSON);

        return districtingJson;
    }

    public void getDistrictsGeojson() throws IOException, InterruptedException {
        //get calculate districting info
        JsonObjectBuilder f= Json.createObjectBuilder();
        f.add("state",state);
        JsonArrayBuilder plans=Json.createArrayBuilder();
        plans.add(getPlanJsonObject(indexOfAverageDistricting));
        plans.add(getPlanJsonObject(indexOfExtremeDistricting));
        plans.add(getPlanJsonObject(indexOfRandomDistricting));
        plans.add(getPlanJsonObject(indexOfSecondRandomDistricting));

        f.add("districtingPlans",plans);

        Path path = Paths.get("src/main/resources/result/"+id+"districting_input.json");
        Files.write(path, f.build().toString().getBytes());
        String data_path = null;
        switch(state) {
            case "GA":
                data_path = "src/main/resources/data/GA/GA_refined.json";
                break;
            case "TX":
                data_path = "src/main/resources/data/TX/TX_refined.json";
                break;
            case "VA":
                data_path = "src/main/resources/data/VA/VA_refined.json";
                break;
            default:
                System.out.println("post processing error");
        }

        ProcessBuilder pb = new ProcessBuilder("python3" ,"src/main/resources/script/postprocess.py", data_path, "src/main/resources/result/"+id+"districting_input.json",String.valueOf(id));
        Process process = pb.start();
        process.waitFor();
        System.out.println("Districtings generated.");

    }
    public  JsonObjectBuilder getPlanJsonObject(int index){
        JsonObjectBuilder plan=Json.createObjectBuilder();
        JsonArrayBuilder districts=Json.createArrayBuilder();

        for (int i=0;i<districtingPlans.get(index).getDistricts().size();i++){
            JsonArrayBuilder precinctsObject=Json.createArrayBuilder();
            for (int p: districtingPlans.get(index).getDistricts().get(i).getPrecincts()){
                precinctsObject.add(p);
            }
            districts.add(precinctsObject);
        }
        plan.add("districts",districts);
        return plan;
    }
    public void calculateNumberOfCounties() throws IOException {
        HashMap<Integer,String> map=Configuration.generatePrecinctCountyMapping(state);
        System.out.println("Total counties: "+(new HashSet<>(map.values())).toString());
        for(int i=0;i<districtingPlans.size();i++){
            for (int j=0;j<districtingPlans.get(i).getDistricts().size();j++){
                Set<String> set= new HashSet<String>();
                for (int precinct:districtingPlans.get(i).getDistricts().get(j).getPrecincts()){
                    set.add(map.get(precinct));
                }
                this.getDistrictingPlans().get(i).getDistricts().get(j).setCountyNum(set.size());
                System.out.println("Districting "+(i+1)+" District "+(j+1)+" has "+set.size()+" counties: "+set);
            }
        }
    }

    public void determineDistricting(){
        long[] SDs=new long[districtingPlans.size()];
        //Calculate the average vap of minorities， it should be the same for all the districtings
        int sumHvap=0,sumWvap=0,sumBvap=0,sumAminvap=0,sumAsianvap=0,sumNhpivap=0;
        for (District district:districtingPlans.get(0).getDistricts()){
            sumHvap+=district.getHvap();
            sumWvap+=district.getWvap();
            sumBvap+=district.getBvap();
            sumAminvap+=district.getAminvap();
            sumAsianvap+=district.getAsianvap();
            sumNhpivap+=district.getNhpivap();
        }
        int numDistricts=districtingPlans.get(0).getDistricts().size();
        double avarageHvap= (double) sumHvap/numDistricts;
        double avarageWvap= (double) sumWvap/numDistricts;
        double avarageBvap= (double) sumBvap/numDistricts;
        double avarageAminvap= (double) sumAminvap/numDistricts;
        double avarageAsianvap= (double) sumAsianvap/numDistricts;
        double avarageNhpivap= (double) sumNhpivap/numDistricts;
        //Calculate Standard Deviation
        for (int i=0;i<districtingPlans.size();i++){
            long sd=0;
            for (District district:districtingPlans.get(i).getDistricts()){
                sd+=Math.pow(district.getHvap()-avarageHvap,2);
                sd+=Math.pow(district.getWvap()-avarageWvap,2);
                sd+=Math.pow(district.getBvap()-avarageBvap,2);
                sd+=Math.pow(district.getAminvap()-avarageAminvap,2);
                sd+=Math.pow(district.getAsianvap()-avarageAsianvap,2);
                sd+=Math.pow(district.getNhpivap()-avarageNhpivap,2);
            }
            SDs[i]=(long)Math.sqrt(sd/numDistricts);
        }
        int indexOfSmallest=getIndexOfSmallest(SDs);
        int indexOfLargest=getIndexOfLargest(SDs);
        int indexOfRandom=(int)Math.floor(Math.random()*districtingPlans.size());
        int indexOfSecondRandomDistricting=(int)Math.floor(Math.random()*districtingPlans.size());

        setIndexOfAverageDistricting(indexOfSmallest);
        setIndexOfExtremeDistricting(indexOfLargest);
        setIndexOfRandomDistricting(indexOfRandom);
        setIndexOfSecondRandomDistricting(indexOfSecondRandomDistricting);

        System.out.println("Average DistrictingPlan: "+districtingPlans.get(indexOfSmallest));
        System.out.println("   With Standard deviation: "+SDs[indexOfSmallest]);
        System.out.println("Extreme DistrictingPlan: "+districtingPlans.get(indexOfLargest));
        System.out.println("   With Standard deviation: "+SDs[indexOfLargest]);
        System.out.println("Random DistrictingPlan: "+districtingPlans.get(indexOfRandom));
        System.out.println("   With Standard deviation: "+SDs[indexOfRandom]);
        System.out.println("Second Random DistrictingPlan: "+districtingPlans.get(indexOfSecondRandomDistricting));
        System.out.println("   With Standard deviation: "+SDs[indexOfRandom]);
    }

    public void exportBoxplotJson() throws FileNotFoundException {

        JsonObjectBuilder outputObject=Json.createObjectBuilder();
        outputObject.add("AA",getMinorityBoxplot(Minority.B));
        outputObject.add("ASIAN",getMinorityBoxplot(Minority.ASIAN));
        outputObject.add("HIS",getMinorityBoxplot(Minority.H));
        outputObject.add("AMIN",getMinorityBoxplot(Minority.AMIN));
        outputObject.add("NHPI",getMinorityBoxplot(Minority.NHPI));
        outputObject.add("WHITE",getMinorityBoxplot(Minority.W));

        // then write it into the local file directory
        OutputStream os = new FileOutputStream("src/main/resources/boxplot/"+id+"_boxplot.json");
        JsonWriter jsonWriter = Json.createWriter(os);
        jsonWriter.writeObject(outputObject.build());
        jsonWriter.close();
    }
    public JsonObjectBuilder getMinorityBoxplot(Minority minority){
        JsonObjectBuilder bBoxplotBuilder=Json.createObjectBuilder();
        JsonObjectBuilder BWBuilder=Json.createObjectBuilder();
        JsonArrayBuilder datapointsBuilder=Json.createArrayBuilder();
        for (int i=0;i<this.bBoxplot.getMins().size();i++){
            JsonObjectBuilder dp=Json.createObjectBuilder();
            dp.add("label","District "+(i+1));
            JsonArrayBuilder nums=Json.createArrayBuilder();
            double[] temp;
            switch (minority) {
                case H: temp=new double[]{this.hBoxplot.getMins().get(i),this.hBoxplot.getQ1s().get(i),this.hBoxplot.getQ3s().get(i),
                        this.hBoxplot.getMaxs().get(i),this.hBoxplot.getMedians().get(i)}; break;
                case W: temp=new double[]{this.wBoxplot.getMins().get(i),this.wBoxplot.getQ1s().get(i),this.wBoxplot.getQ3s().get(i),
                        this.wBoxplot.getMaxs().get(i),this.wBoxplot.getMedians().get(i)}; break;
                case B: temp=new double[]{this.bBoxplot.getMins().get(i),this.bBoxplot.getQ1s().get(i),this.bBoxplot.getQ3s().get(i),
                        this.bBoxplot.getMaxs().get(i),this.bBoxplot.getMedians().get(i)}; break;
                case AMIN: temp=new double[]{this.aminBoxplot.getMins().get(i),this.aminBoxplot.getQ1s().get(i),this.aminBoxplot.getQ3s().get(i),
                        this.aminBoxplot.getMaxs().get(i),this.aminBoxplot.getMedians().get(i)}; break;
                case ASIAN: temp=new double[]{this.asianBoxplot.getMins().get(i),this.asianBoxplot.getQ1s().get(i),this.asianBoxplot.getQ3s().get(i),
                        this.asianBoxplot.getMaxs().get(i),this.asianBoxplot.getMedians().get(i)}; break;
                case NHPI: temp=new double[]{this.nhpiBoxplot.getMins().get(i),this.nhpiBoxplot.getQ1s().get(i),this.nhpiBoxplot.getQ3s().get(i),
                        this.nhpiBoxplot.getMaxs().get(i),this.nhpiBoxplot.getMedians().get(i)}; break;
                default: throw new IllegalStateException("Unexpected value: " + minority);
            }
            nums.add(new BigDecimal(temp[0]).setScale(8,BigDecimal.ROUND_HALF_DOWN).doubleValue());
            nums.add(new BigDecimal(temp[1]).setScale(8,BigDecimal.ROUND_HALF_DOWN).doubleValue());
            nums.add(new BigDecimal(temp[2]).setScale(8,BigDecimal.ROUND_HALF_DOWN).doubleValue());
            nums.add(new BigDecimal(temp[3]).setScale(8,BigDecimal.ROUND_HALF_DOWN).doubleValue());
            nums.add(new BigDecimal(temp[4]).setScale(8,BigDecimal.ROUND_HALF_DOWN).doubleValue());
            dp.add("y",nums);
            datapointsBuilder.add(dp);
        }
        BWBuilder.add("dataPoints",datapointsBuilder);
        bBoxplotBuilder.add("BW",BWBuilder);
        double[] minorityEnact;
        switch (minority){
            case H: minorityEnact=this.enactedHVAP; break;
            case W: minorityEnact=this.enactedWVAP;break;
            case B: minorityEnact=this.enactedBVAP;break;
            case AMIN: minorityEnact=this.enactedAminVAP;break;
            case ASIAN: minorityEnact=this.enactedAsianVAP;break;
            case NHPI: minorityEnact=this.enactedNHPIVAP;break;
            default: throw new IllegalStateException("Unexpected value: " + minority);
        }

        JsonObjectBuilder enactedJson=Json.createObjectBuilder();
        JsonArrayBuilder enactedDataPoints=Json.createArrayBuilder();
        for (int i=0;i<this.enactedAsianVAP.length;i++){
            JsonObjectBuilder dp=Json.createObjectBuilder();
            dp.add("x",i);
            dp.add("y",minorityEnact[i]);
            enactedDataPoints.add(dp);
        }
        enactedJson.add("dataPoints",enactedDataPoints);
        bBoxplotBuilder.add("enacted",enactedJson);
        return bBoxplotBuilder;
    }

    public void setBoxPlot(Minority minority){
        for (int i = 0; i < districtingPlans.size(); i++) {
            for (int j = 0; j < districtingPlans.get(i).getDistricts().size(); j++) {
                switch (minority) {
                    case H: districtingPlans.get(i).getDistricts().sort(Comparator.comparing(District::getHvap));break;
                    case W: districtingPlans.get(i).getDistricts().sort(Comparator.comparing(District::getWvap));break;
                    case B: districtingPlans.get(i).getDistricts().sort(Comparator.comparing(District::getBvap));break;
                    case AMIN:districtingPlans.get(i).getDistricts().sort(Comparator.comparing(District::getAminvap));break;
                    case ASIAN:districtingPlans.get(i).getDistricts().sort(Comparator.comparing(District::getAsianvap));break;
                    case NHPI:districtingPlans.get(i).getDistricts().sort(Comparator.comparing(District::getNhpivap));break;
                }
            }
        }
        System.out.println("DistrictingPlan 1 after sorted based on minority VAP: "+districtingPlans.get(0).getDistricts());

        List<Double> mins=new ArrayList<>(), q1s=new ArrayList<>(), medians=new ArrayList<>(),
                q3s=new ArrayList<>(),maxs=new ArrayList<>();

        for (int i = 0; i < districtingPlans.get(0).getDistricts().size(); i++) {
            double[] col = new double[districtingPlans.size()];
            for (int j = 0; j < districtingPlans.size(); j++) {
                switch (minority) {
                    case H: col[j] = districtingPlans.get(j).getDistricts().get(i).getHvap();       break;
                    case W: col[j] = districtingPlans.get(j).getDistricts().get(i).getWvap();       break;
                    case B: col[j] = districtingPlans.get(j).getDistricts().get(i).getBvap();       break;
                    case AMIN: col[j] = districtingPlans.get(j).getDistricts().get(i).getAminvap();       break;
                    case ASIAN: col[j] = districtingPlans.get(j).getDistricts().get(i).getAsianvap();       break;
                    case NHPI: col[j] = districtingPlans.get(j).getDistricts().get(i).getNhpivap();       break;
                }

            }
            mins.add(Quantiles.percentiles().index(0).compute(col));
            q1s.add(Quantiles.percentiles().index(25).compute(col));
            medians.add(Quantiles.percentiles().index(50).compute(col));
            q3s.add( Quantiles.percentiles().index(70).compute(col));
            maxs.add(Quantiles.percentiles().index(100).compute(col));
        }
        Boxplot newBoxplot=new Boxplot(mins,q1s,medians,q3s,maxs);
        switch (minority) {
            case H: setHBoxplot(newBoxplot);
                System.out.println("HBoxplot: "+newBoxplot);break;
            case W: setWBoxplot(newBoxplot);
                System.out.println("WBoxplot: "+newBoxplot);break;
            case B: setBBoxplot(newBoxplot);
                System.out.println("BBoxplot: "+newBoxplot);break;
            case AMIN: setAminBoxplot(newBoxplot);
                System.out.println("AminBoxplot: "+newBoxplot);break;
            case ASIAN: setAsianBoxplot(newBoxplot);
                System.out.println("AsianBoxplot: "+newBoxplot);break;
            case NHPI: setNhpiBoxplot(newBoxplot);
                System.out.println("NHPIBoxplot: "+newBoxplot);break;
        }

    }
    public static int getIndexOfSmallest(long[] array){
        if (array.length == 0)
            return -1;// array contains no elements
        int index = 0;
        long min = array[index];

        for (int i = 1; i < array.length; i++){
            if (array[i] <= min){
                min = array[i];
                index = i;
            }
        }
        return index;
    }
    public static int getIndexOfLargest(long[] array) {
        if (array.length == 0)
            return -1; // array contains no elements
        int index = 0;
        long max = array[0];

        for(int i=1; i<array.length; i++) {
            if (max < array[i]) {
                max = array[i];
                index = i;
            }
        }
        return index;
    }
}
