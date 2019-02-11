package com.samples.tests

import com.samples.TracingHelper
import groovy.transform.CompileStatic
import spock.lang.Specification

class TestTracing extends Specification {

def 'Test Tracing in exact number of hops'(){
    given:
        TracingHelper helper = new TracingHelper(rawString)
        List<String> results = null;
    when:
        results = helper.identifyTracesUptoRequired(source,destination,hops,"", TracingHelper.MatchCase.EQ,helper.&traceDepthCompare)
    then:
        for(String s : expectedResult) {
            assert results.contains(s)
            //println s
        }
        println results
        assert results.size() == expectedResult.size()
    where:
        source | destination | hops | expectedResult    | rawString

        "A"    | "C"         | 4      | ["ABCDC","ADCDC","ADEBC"] | "AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7"
        "C"    | "C"         | 2      | ["CDC"]  |  "AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7"
        "C"    | "C"         | 3      | ["CEBC"]  |  "AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7"
}

def 'Test Tracing upto number of hops'(){
    given:
    TracingHelper helper = new TracingHelper(rawString)
    List<String> results = null;
    when:
    results = helper.identifyTracesUptoRequired(source,destination,hops,"", TracingHelper.MatchCase.LTE,helper.&traceDepthCompare)
    then:
    for(String s : expectedResult) {
        assert results.contains(s)
        //println s
    }
    println results
    assert results.size() == expectedResult.size()
    where:
    source | destination | hops | expectedResult    | rawString

    "A"    | "C"         | 4      | ["ABC","ADC","AEBC","ABCDC","ADCDC","ADEBC"] | "AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7"
    "C"    | "C"         | 2      | ["CDC"]  |  "AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7"
    "C"    | "C"         | 3      | ["CDC","CEBC"]  |  "AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7"
}

def 'Test Trace with shortest latency'() {
    given:
        TracingHelper helper = new TracingHelper(rawString)
        Set<String> results = null;
    when:
        String result = helper.identifyTracewithShortestLatency(source,destination)
    then:
        println result
        println helper.calculateTotalLatency(result)
        helper.calculateTotalLatency(result) == expectedLatency
        result.equals(expectedResult)
    where:
    source | destination || expectedLatency | expectedResult | rawString

    "A"    | "C"         || 9            | "ABC"  | "AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7"
    "C"    | "C"         || 9              | "CEBC"  | "AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7"
    "B"    | "B"         || 9            | "BCEB"   | "AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7"
    "B"    | "F"         || 0           | ""    | "AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7"
}

def 'Test Trace latency less than given'(){

    when:
        TracingHelper helper = new TracingHelper(rawString)
        List<String> results = helper.identifyTraceUptoLatency(source,destination,givenLatency)
    then:
        println results
        for(String s : expectedTraces) {
            assert results.contains(s)
        }
        results.size()== expectedNumOfTraces

    where:
    source    | destination   | givenLatency | expectedTraces | expectedNumOfTraces | rawString
    "C"       | "C"        | 30        | ["CDC","CEBC", "CDEBC", "CDCEBC","CEBCDC","CEBCEBC","CEBCEBCEBC"]    | 7  | "AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7"

}


def 'Test Total Average latency between two points'(){

    when:
    TracingHelper helper = new TracingHelper(rawString)
    then:
        def result = helper.calculateTotalLatency(trace)
        result == expectedLatency
    where:
        trace   | expectedLatency | rawString
        "ABC"        | 9             | "AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7"
        "AD"        | 5            | "AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7"

}

def 'Test number of traces with latency less than given'(){
    when:
    TracingHelper helper = new TracingHelper(rawString)
    then:
        List<String> temp = helper.identifyTraceUptoLatency(source,destination,maxLatency)
        println temp
        println temp.size()
    where:
        source | destination |maxLatency | noOfTraces | rawString
        "C"   | "C"         |  100  |  1073 |"AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7"

}

}