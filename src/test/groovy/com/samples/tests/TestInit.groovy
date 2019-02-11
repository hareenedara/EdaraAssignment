package com.samples.tests

import com.samples.TracingHelper
import spock.lang.Specification
//@CompileStatic
class TestInit extends Specification {

    def 'Test initialization stuff: Reading Input correctly'(){

        when:
        TracingHelper helper = new TracingHelper(rawString)

        then:
            Map<String,Integer> resultMap = helper.getConnLatMap()
            println resultMap
            resultMap.get(conn1).intValue() == conn1_latency
            resultMap.get(conn2).intValue() == conn2_latency
            resultMap.get(conn3).intValue() == conn3_latency

        where:
            conn1   | conn1_latency | conn2 | conn2_latency | conn3 | conn3_latency |rawString

             "AB"   | 5            |"BC"    |  4        | "CD"      | 8           | "AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7"
    }

    def 'Test initializing breadth first links of each vertex between given source&destnation'(){
        when:
        TracingHelper helper = new TracingHelper(rawString)

        then:
            List<String> links = helper.getLinks().get(givenVertex)
            println links
            String linksStr = "";
            for(String s:links){ linksStr = linksStr+s }
            linksStr.equals(linksToVertex)

        where:
            source | destination | givenVertex | linksToVertex   | rawString
            "A"   | "C"         | "A"       | "BDE"    | "AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7"
            "A"   | "C"         | "C"       | "DE"     | "AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7"
    }


    def 'Test initializing find all unique connected verticies'(){

        when:
            TracingHelper helper = new TracingHelper(rawString)
        then:
            println helper.getUniqueConnectedVertices()
            helper.getUniqueConnectedVertices().length() == count
        where:
            rawString| count
            "AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7" | 5
    }

}
