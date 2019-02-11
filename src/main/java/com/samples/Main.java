package com.samples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws IOException {
        Main main = new Main();
        if(args.length < 1){
            System.out.println("Missing input data file");
            System.exit(1);
        }

        String filename = args[0];
        FileReader fileReader = new FileReader(new File(filename));
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            line = line.toUpperCase().trim();
            // read file. skip lines with anything other than A-Z 0-9 \, space
            line = line.replaceAll("\"", "");
            if(line.matches("[0-9a-zA-Z \\, \\ ]+")){
                TracingHelper helper = new TracingHelper(line);
                //1-5
                main.averageLatency(1,"A-B-C", helper);
                main.averageLatency(2,"A-D",helper);
                main.averageLatency(3,"A-D-C",helper);
                main.averageLatency(4,"A-E-B-C-D",helper);
                main.averageLatency(5,"A-E-D",helper);
                //6
                main.printTraces(6,helper.identifyTracesUptoRequired("C","C",3,"",TracingHelper.MatchCase.LTE,helper::traceDepthCompare));
                //7
                main.printTraces(7,helper.identifyTracesUptoRequired("A","C",4, "",TracingHelper.MatchCase.EQ,helper::traceDepthCompare));
                // 8
                System.out.println("8 -> " +helper.calculateTotalLatency(helper.identifyTracewithShortestLatency("A","C")));
                //9
                System.out.println("9 -> " +helper.calculateTotalLatency(helper.identifyTracewithShortestLatency("B","B")));
                // 10 c-c < 30
                System.out.println("10 -> " +helper.identifyTraceUptoLatency("C","C",30).size());
            }

        }

    }

    void averageLatency(int question_num,String trace, TracingHelper helper){
        trace = trace.replaceAll("-","");
        int tracetime = helper.calculateTotalLatency(trace);
        if(tracetime == -1){
            System.out.println(question_num+"-> NO SUCH TRACE");
        }else {
            System.out.println(question_num+" -> "+tracetime);
        }
    }

    void printTraces(int q_num, List<String> traces) {
//        traces.stream().forEach(s->{
//            System.out.println(s);
//        });
        if(traces.size()==0){
            System.out.println(q_num+" -> "+ "NO SUCH TRACE");
        }else {
            System.out.println(q_num + " -> " + traces.size());
        }
    }


}
