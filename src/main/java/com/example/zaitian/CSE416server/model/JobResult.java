package com.example.zaitian.CSE416server.model;
import lombok.Getter;
import lombok.Setter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.common.math.Quantiles;

import javax.json.*;

@Getter
@Setter
public class JobResult {
    private List<DistrictingPlan> districtingPlans;

    private Boxplot hBoxplot;
    private Boxplot wBoxplot;
    private Boxplot bBoxplot;
    private Boxplot aminBoxplot;
    private Boxplot asianBoxplot;
    private Boxplot nhpiBoxplot;

    private DistrictingPlan averageDistricting;
    private DistrictingPlan extremeDistricting;
    private DistrictingPlan randomDistricting;

    private int[] enactedHVAP;
    private int[] enactedWVAP;
    private int[] enactedBVAP;
    private int[] enactedAminVAP;
    private int[] enactedAsianVAP;
    private int[] enactedNHPIVAP;


    public JobResult(List<DistrictingPlan> plans,String state){
        //TODO: get enacted data based on state
//        if (state.equals("GA")) {
            enactedHVAP = Configuration.GA_enacted_HVAP;
            enactedWVAP = Configuration.GA_enacted_WVAP;
            enactedBVAP = Configuration.GA_enacted_BVAP;
            enactedAminVAP = Configuration.GA_enacted_AminVAP;
            enactedAsianVAP = Configuration.GA_enacted_AsianVAP;
            enactedNHPIVAP = Configuration.GA_enacted_NHPIVAP;
//        }
//        else if (state.equals("")){
//
//        }
//        else if (state.equals("")){
//
//        }
//
        districtingPlans=plans;
    }
    public void processResult() throws FileNotFoundException {
        setBoxPlot(Minority.H);
        setBoxPlot(Minority.W);
        setBoxPlot(Minority.B);
        setBoxPlot(Minority.AMIN);
        setBoxPlot(Minority.ASIAN);
        setBoxPlot(Minority.NHPI);

        generateBoxplotJson();
    }

    public String generateBoxplotJson() throws FileNotFoundException {

        JsonObjectBuilder outputObject=Json.createObjectBuilder();
        outputObject.add("AA",getMinorityBoxplot(Minority.B));
        outputObject.add("ASIAN",getMinorityBoxplot(Minority.ASIAN));
        outputObject.add("HIS",getMinorityBoxplot(Minority.H));
        outputObject.add("AMIN",getMinorityBoxplot(Minority.AMIN));
        outputObject.add("NHPI",getMinorityBoxplot(Minority.NHPI));
        outputObject.add("WHITE",getMinorityBoxplot(Minority.W));

        // then write it into the local file directory
        OutputStream os = new FileOutputStream("src/main/resources/boxplot/boxplot.json");
        JsonWriter jsonWriter = Json.createWriter(os);
        jsonWriter.writeObject(outputObject.build());
        jsonWriter.close();
        return "";
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
                case H:
                    temp=new double[]{this.hBoxplot.getMins().get(i),this.hBoxplot.getQ1s().get(i),
                            this.hBoxplot.getMedians().get(i),this.hBoxplot.getQ3s().get(i),this.hBoxplot.getMaxs().get(i)};
                    break;
                case W:
                    temp=new double[]{this.wBoxplot.getMins().get(i),this.wBoxplot.getQ1s().get(i),
                            this.wBoxplot.getMedians().get(i),this.wBoxplot.getQ3s().get(i),this.wBoxplot.getMaxs().get(i)};
                    break;
                case B:
                    temp=new double[]{this.bBoxplot.getMins().get(i),this.bBoxplot.getQ1s().get(i),
                            this.bBoxplot.getMedians().get(i),this.bBoxplot.getQ3s().get(i),this.bBoxplot.getMaxs().get(i)};
                    break;
                case AMIN:
                    temp=new double[]{this.aminBoxplot.getMins().get(i),this.aminBoxplot.getQ1s().get(i),
                            this.aminBoxplot.getMedians().get(i),this.aminBoxplot.getQ3s().get(i),this.aminBoxplot.getMaxs().get(i)};
                    break;
                case ASIAN:
                    temp=new double[]{this.asianBoxplot.getMins().get(i),this.asianBoxplot.getQ1s().get(i),
                            this.asianBoxplot.getMedians().get(i),this.asianBoxplot.getQ3s().get(i),this.asianBoxplot.getMaxs().get(i)};
                    break;
                case NHPI:
                    temp=new double[]{this.nhpiBoxplot.getMins().get(i),this.nhpiBoxplot.getQ1s().get(i),
                            this.nhpiBoxplot.getMedians().get(i),this.nhpiBoxplot.getQ3s().get(i),this.nhpiBoxplot.getMaxs().get(i)};
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + minority);
            }
            nums.add(temp[0]);
            nums.add(temp[1]);
            nums.add(temp[2]);
            nums.add(temp[3]);
            nums.add(temp[4]);
            dp.add("y",nums);
            datapointsBuilder.add(dp);
        }
        BWBuilder.add("dataPoints",datapointsBuilder);
        bBoxplotBuilder.add("BW",BWBuilder);
        //TODO: fill in with enacted data
        int[] minorityEnact;
        switch (minority){
            case H:
                minorityEnact=this.enactedHVAP; break;
            case W:
                minorityEnact=this.enactedWVAP;break;
            case B:
                minorityEnact=this.enactedBVAP;break;
            case AMIN:
                minorityEnact=this.enactedAminVAP;break;
            case ASIAN:
                minorityEnact=this.enactedAsianVAP;break;
            case NHPI:
                minorityEnact=this.enactedNHPIVAP;break;
            default:
                throw new IllegalStateException("Unexpected value: " + minority);
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
        int[][] temp = new int[districtingPlans.size()][districtingPlans.get(0).getDistricts().size()];
        System.out.println("After sort each districting: ");
        for (int i = 0; i < districtingPlans.size(); i++) {
            for (int j = 0; j < districtingPlans.get(i).getDistricts().size(); j++) {
                switch (minority) {
                    case H:
                        temp[i][j] = districtingPlans.get(i).getDistricts().get(j).getHvap();
                        break;
                    case W:
                        temp[i][j] = districtingPlans.get(i).getDistricts().get(j).getWvap();
                        break;
                    case B:
                        temp[i][j] = districtingPlans.get(i).getDistricts().get(j).getBvap();
                        break;
                    case AMIN:
                        temp[i][j] = districtingPlans.get(i).getDistricts().get(j).getAminvap();
                        break;
                    case ASIAN:
                        temp[i][j] = districtingPlans.get(i).getDistricts().get(j).getAsianvap();
                        break;
                    case NHPI:
                        temp[i][j] = districtingPlans.get(i).getDistricts().get(j).getNhpivap();
                        break;
                }
            }
            Arrays.sort(temp[i]);
        }

        List<Double> mins=new ArrayList<>();
        List<Double> q1s=new ArrayList<>();
        List<Double> medians=new ArrayList<>();
        List<Double> q3s=new ArrayList<>();
        List<Double> maxs=new ArrayList<>();


        for (int i = 0; i < districtingPlans.get(0).getDistricts().size(); i++) {
            int[] col = new int[districtingPlans.size()];
            for (int j = 0; j < districtingPlans.size(); j++) {
                col[j] = temp[j][i];
            }

            mins.add(Quantiles.percentiles().index(0).compute(col));
            q1s.add(Quantiles.percentiles().index(25).compute(col));
            medians.add(Quantiles.percentiles().index(50).compute(col));
            q3s.add( Quantiles.percentiles().index(70).compute(col));
            maxs.add(Quantiles.percentiles().index(100).compute(col));
        }

        switch (minority) {
            case H:
                setHBoxplot(new Boxplot(mins,q1s,medians,q3s,maxs));
                break;
            case W:
                setWBoxplot(new Boxplot(mins,q1s,medians,q3s,maxs));
                break;
            case B:
                setBBoxplot(new Boxplot(mins,q1s,medians,q3s,maxs));
                break;
            case AMIN:
                setAminBoxplot(new Boxplot(mins,q1s,medians,q3s,maxs));
                break;
            case ASIAN:
                setAsianBoxplot(new Boxplot(mins,q1s,medians,q3s,maxs));
                break;
            case NHPI:
                setNhpiBoxplot(new Boxplot(mins,q1s,medians,q3s,maxs));
                break;
        }
    }

}
