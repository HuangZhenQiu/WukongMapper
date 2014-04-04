package edu.uci.eecs.wukong.util;

import org.jgrapht.generate.RandomGraphGenerator;
import org.jgrapht.generate.LinearGraphGenerator;
import org.jgrapht.generate.StarGraphGenerator;
import org.jgrapht.generate.ScaleFreeGraphGenerator;


import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ClassBasedVertexFactory;
import org.jgrapht.VertexFactory;


public class GraphGenerator {
	
	public GraphGenerator() {
		
	}
	
	public SimpleDirectedGraph<Object, DefaultEdge> generateRandomGraph(int vSize, int eSize) {
        //Create the graph object; it is null at this point
		SimpleDirectedGraph<Object, DefaultEdge> randomGraph = new SimpleDirectedGraph<Object, DefaultEdge>(DefaultEdge.class);

        //Create the RandmomGraphGenerator object
		RandomGraphGenerator<Object, DefaultEdge> randomGenerator =
            new RandomGraphGenerator<Object, DefaultEdge>(vSize, eSize);

        //Create the VertexFactory so the generator can create vertices
        VertexFactory<Object> vFactory =
            new ClassBasedVertexFactory<Object>(Object.class);

        //Use the RandmomGraphGenerator object to make completeGraph a
        //complete graph with [size] number of vertices
        randomGenerator.generateGraph(randomGraph, vFactory, null);
        
        return randomGraph;
	}
	
	public SimpleDirectedGraph<Object, DefaultEdge> generateLinearGraph(int vSize) {
		 //Create the graph object; it is null at this point
		SimpleDirectedGraph<Object, DefaultEdge> randomGraph = new SimpleDirectedGraph<Object, DefaultEdge>(DefaultEdge.class);

        //Create the RandmomGraphGenerator object
		LinearGraphGenerator<Object, DefaultEdge> linearRandomGenerator =
            new LinearGraphGenerator<Object, DefaultEdge>(vSize);

        //Create the VertexFactory so the generator can create vertices
        VertexFactory<Object> vFactory =
            new ClassBasedVertexFactory<Object>(Object.class);

        //Use the RandmomGraphGenerator object to make completeGraph a
        //complete graph with [size] number of vertices
        linearRandomGenerator.generateGraph(randomGraph, vFactory, null);
        
        return randomGraph;
	}
	
	public SimpleDirectedGraph<Object, DefaultEdge> generateStarGraph(int vSize) {
		 //Create the graph object; it is null at this point
		SimpleDirectedGraph<Object, DefaultEdge> randomGraph = new SimpleDirectedGraph<Object, DefaultEdge>(DefaultEdge.class);

       //Create the RandmomGraphGenerator object
		StarGraphGenerator<Object, DefaultEdge> starGraphGenerator =
           new StarGraphGenerator<Object, DefaultEdge>(vSize);

       //Create the VertexFactory so the generator can create vertices
       VertexFactory<Object> vFactory =
           new ClassBasedVertexFactory<Object>(Object.class);

       //Use the RandmomGraphGenerator object to make completeGraph a
       //complete graph with [size] number of vertices
       starGraphGenerator.generateGraph(randomGraph, vFactory, null);
       
       return randomGraph;
	}
	
	public SimpleDirectedGraph<Object, DefaultEdge> generateScaleFreeGraph(int vSize) {

		 //Create the graph object; it is null at this point
		SimpleDirectedGraph<Object, DefaultEdge> randomGraph = new SimpleDirectedGraph<Object, DefaultEdge>(DefaultEdge.class);

      //Create the RandmomGraphGenerator object
		ScaleFreeGraphGenerator<Object, DefaultEdge> scaleFreeGraphGenerator =
          new ScaleFreeGraphGenerator<Object, DefaultEdge>(vSize);

      //Create the VertexFactory so the generator can create vertices
      VertexFactory<Object> vFactory =
          new ClassBasedVertexFactory<Object>(Object.class);

      //Use the RandmomGraphGenerator object to make completeGraph a
      //complete graph with [size] number of vertices
      scaleFreeGraphGenerator.generateGraph(randomGraph, vFactory, null);
      
      return randomGraph;
	}
	
	
}
