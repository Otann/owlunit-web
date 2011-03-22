package com.manymonkeys.impex;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.io.*;

/**
 * Many Monkeys
 *
 * @author Ilya Pimenov
 */
public class Export {

    public static void main(String [] args){
        System.out.println("Export");

        File outfile = new File("c:/fuck.json");

        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(outfile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(outputStream);

        GraphDatabaseService graphDb = new EmbeddedGraphDatabase("D:/DEVELOPE/InformationItems/ii-stream/runtime/neo4j-db");
        for (Node node : graphDb.getAllNodes()){
            pw.print("[");
            pw.print(node.getId());
            for (String keys : node.getPropertyKeys()){
                pw.print(" ");
                pw.print(keys + " = " + node.getProperty(keys));
            }
            pw.println("]");
            for (Relationship relationship : node.getRelationships()){
                pw.print("{");
                for (Node rel : relationship.getNodes()){
                    pw.print("[ " + rel.getId() + " ] ");
                }
                pw.print("}");
            }
            pw.println();
        }

        graphDb.shutdown();
        pw.close();
        System.out.println("fucked");
    }
}