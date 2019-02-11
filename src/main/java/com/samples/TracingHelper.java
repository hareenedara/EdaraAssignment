package com.samples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TracingHelper {

    private HashMap<String,Integer> connLatMap = new HashMap<>();
    private HashMap<String, ArrayList<String>> links = new HashMap<>();

    private String shortestTrace = "";
    private String uniqueConnectedVertices ="";
    public TracingHelper(String raw){
        init(raw);
    }


    public void init(String raw){
        String[] rawConns = raw.split(",");
        for(int i=0;i<rawConns.length;i++){
            String temp = rawConns[i].trim();
            String conn = temp.length()>=3 ? temp.substring(0,2).toUpperCase():"invalid";
            Integer latency = null;
            if(conn.equals("invalid")){
                System.out.printf("Invalid Connection info %s in line %s .",temp,raw);
                System.out.println();
                System.out.println("Skipping Connection.");
                continue;
            }else {
                String num = temp.substring(2);
                latency = num.matches("^[0-9]+$") ? Integer.parseInt(num) : null;
            }
            if(latency != null){
                getConnLatMap().put(conn,latency);
            }
        }
        Set<String> keys = getConnLatMap().keySet();
        StringBuffer sb = new StringBuffer();
        keys.stream().forEach(key -> {
            if(key.charAt(0) != key.charAt(1)) {
                String k = key.substring(0, 1);
                if (getLinks().get(k) == null) {
                    ArrayList<String> templinks = new ArrayList<>();
                    templinks.add(key.substring(1, 2));
                    getLinks().put(k, templinks);
                } else {
                    getLinks().get(k).add(key.substring(1, 2));
                }
            }
            sb.append(key);
        });
        uniqueConnectedVertices = findUnique(sb.toString());

    }

    String findUnique(String str) {
        String result = "";
        for(int i=0;i<str.length();i++){
            if(result.indexOf(str.charAt(i)) == -1){
                result = result+str.charAt(i);
            }
        }
        return result;
    }

    public void injestNewValues(String rawTrace) {
        getConnLatMap().clear();
        getLinks().clear();
        init(rawTrace);

    }

    public String getUniqueConnectedVertices() {
        return uniqueConnectedVertices;
    }

    public enum MatchCase {
        SHORT,LT,EQ,LTE,GT
    }

    public ArrayList<String> identifyTracesUptoRequired(String source, String dest,int maxLimit,String initialTrace,MatchCase matchCase, TraceFilter<String,Integer,MatchCase> fn){
        ArrayList<String> traces= new ArrayList<>();

        ArrayList<String> ltTraces = new ArrayList<>();
        ArrayList<String> eqTraces = new ArrayList<>();
        // review this debug

        if(!initialTrace.trim().equals("")) {
            findTraces(initialTrace,traces);
        }else {
            findTraces(source,traces);
        }

        int size = traces.size();
        String s="";
        for(int i=0;i<size;i++) {
            s = traces.get(i);
            if(s.substring(s.length()-1).equals(dest)){
                if(fn.filter(s,maxLimit)==MatchCase.LT) {
                    ltTraces.add(s);
                } else if(fn.filter(s,maxLimit)== MatchCase.EQ){
                    eqTraces.add(s);
                } else{
                    if(fn.filter(s,maxLimit) == MatchCase.SHORT){
                        eqTraces.add(s);
                    }
                    break;
                }
                }
            findTraces(s,traces);
            size = traces.size();
            // if source and destination are not reacheable, break.
            if(i > (uniqueConnectedVertices.length()*2) && ltTraces.size()<1 && eqTraces.size()<1){
                break;
            }
        }
        if(matchCase == MatchCase.LT){
            return ltTraces;
        }else if(matchCase == MatchCase.LTE){
            ltTraces.addAll(eqTraces);
            return ltTraces;
        }else {
            return eqTraces;
        }
    }

    public void findTraces(String parent, ArrayList<String> traces) {
        String source = parent.substring(parent.length()-1);
        for(String s : getLinks().get(source)) {
            traces.add(parent+s);
        }
    }

    void addTraces(String parent, int max) {

    }

    MatchCase shortDepthTrace(String str,int depth ) {
        return MatchCase.SHORT;
    }



    public MatchCase traceDepthCompare(String str, int depth){
        if(str.length()-1 < depth){
            return MatchCase.LT;
        }else if(str.length()-1 == depth){
            return MatchCase.EQ;
        }else {
            return MatchCase.GT;
        }
    }

    MatchCase traceLatencyCompare(String str, int maxLatency) {
        int latency = calculateTotalLatency(str);
        if(latency < maxLatency){
            return MatchCase.LT;
        }else if(latency == maxLatency){
            return MatchCase.EQ;
        }else {
            return MatchCase.GT;
        }

    }
    public String identifyTracewithShortestLatency(String source,String dest) {
        int count = 10;
        List<String> tempList= new ArrayList<>();
        String result = "";
        while (tempList.size()==0 && count < 20){
            tempList = identifyTracesUptoRequired(source,dest,count,"",MatchCase.LT,this::traceDepthCompare);
            int min = Integer.MAX_VALUE;
            int lat = 0;
            for(String temp: tempList){
                lat = calculateTotalLatency(temp);
                if(lat < min) {
                    min = lat;
                    result = temp;
                }
            }
            count = count + 10;
        }


        return result;
    }

    public List<String> identifyTraceUptoLatency(String source,String dest,int maxLatency) {
        List<String> tempList = new ArrayList<>();
        int count =20;
        String initialTrace ="";
        do {
            tempList.addAll(identifyTracesUptoRequired(source, dest, count,initialTrace, MatchCase.LT, this::traceDepthCompare));

            count = count+10;

            if(count >20) {
                initialTrace = tempList.get(tempList.size()-1);
            }
//            //debug
//            System.out.println(tempList.get(tempList.size()-1));
//            System.out.println("last latency: "+calculateTotalLatency(tempList.get(tempList.size()-1)));
        }while(calculateTotalLatency(tempList.get(tempList.size()-1)) <= maxLatency );

        return tempList.stream().filter(temp -> calculateTotalLatency(temp) < maxLatency).collect(Collectors.toList());

    }

    public int calculateTotalLatency(String traceStr){
        int total =0;
        for(int i=0;i<traceStr.length()-1;i++){
            String temp = traceStr.substring(i,(i+2<=traceStr.length())?i+2:traceStr.length());
            if(getConnLatMap().get(temp) == null) return -1;
            total += getConnLatMap().get(temp);

        }
        return total;
    }

    public HashMap<String, Integer> getConnLatMap() {
        return connLatMap;
    }

    public HashMap<String, ArrayList<String>> getLinks() {
        return links;
    }

    public String getShortestTrace() {
        return shortestTrace;
    }
}
