package org.ets.nlp;

import java.lang.Integer;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.FileReader;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;


import com.googlecode.clearnlp.dependency.AbstractDEPParser;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.srl.AbstractSRLabeler;
import com.googlecode.clearnlp.engine.EngineGetter;
import com.googlecode.clearnlp.engine.EngineProcess;
import com.googlecode.clearnlp.morphology.AbstractMPAnalyzer;
import com.googlecode.clearnlp.pos.POSTagger;
import com.googlecode.clearnlp.predicate.AbstractPredIdentifier;
import com.googlecode.clearnlp.reader.AbstractReader;
import com.googlecode.clearnlp.segmentation.AbstractSegmenter;
import com.googlecode.clearnlp.tokenization.AbstractTokenizer;
import com.googlecode.clearnlp.util.UTInput;
import com.googlecode.clearnlp.util.UTOutput;
import com.googlecode.clearnlp.util.pair.Pair;


public class ThriftServer {
	
    public static class ClearNLPHandler implements ClearNLP.Iface {
		
	static private final String language = AbstractReader.LANG_EN;
	static private AbstractMPAnalyzer analyzer;
	static private Pair<POSTagger[],Double> taggers;
	static private AbstractDEPParser parser;
	static private AbstractPredIdentifier identifier;
	static private AbstractSRLabeler labeler;
        

	public ClearNLPHandler () {
	    try {
		analyzer = EngineGetter.getMPAnalyzer(language, 
						      ThriftServer.class.getResourceAsStream("/dictionary-1.1.0.zip"));
                taggers = EngineGetter.getPOSTaggers(
						     ThriftServer.class.getResourceAsStream("/ontonotes-en-pos-1.1.0g.jar"));
                parser = EngineGetter.getDEPParser(
						   ThriftServer.class.getResourceAsStream("/ontonotes-en-dep-1.1.0b3.jar"));
                identifier = EngineGetter.getPredIdentifier(
							    ThriftServer.class.getResourceAsStream("/ontonotes-en-pred-1.2.0.jar"));
                labeler = EngineGetter.getSRLabeler(
						    ThriftServer.class.getResourceAsStream("/ontonotes-en-srl-1.2.0b3.jar"));
	    } catch (Exception e) {
		
	    }
	}



	private List<TDepNode> wrap(List<DEPNode> tokens){

	    ArrayList<TDepNode> result = new ArrayList<TDepNode> ();
	    for(DEPNode token: tokens){
		String s =  token.toStringSRL();
		String [] fields = s.split("[\t\n ]+");


		TDepNode x = new TDepNode(fields[0],
					  fields[1],
					  fields[2],
					  fields[3],
					  fields[4],
					  fields[5],
					  fields[6],
					  fields[7]);
		result.add(x);
	    }
	    return result;

	}

	public List labelString(String inputString)
	{

	    try {
		InputStream is = new ByteArrayInputStream(inputString.getBytes());
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		List r = labelCommon(in);
		in.close();
		return r;
	    }
	    catch (Exception e) {
		System.out.println(e);
		return null;
	    }
	}

	public List labelFile(String inputFile) {

	    try {
		BufferedReader in = UTInput.createBufferedFileReader(inputFile);

		List r = labelCommon(in);
		in.close();
		return r;
	    } catch (Exception e) {
		return null;
	    }
	}



	private List<List<TDepNode>> labelCommon(BufferedReader in)
	{

	    try {
		List<List<TDepNode>> result = new ArrayList<List<TDepNode>>();
    
		AbstractTokenizer tokenizer = EngineGetter.getTokenizer(language, ThriftServer.class.getResourceAsStream("/dictionary-1.1.0.zip"));

		AbstractSegmenter segmenter = EngineGetter.getSegmenter(language, tokenizer);
    
		for (List<String> tokens : segmenter.getSentences(in)){    
		    DEPTree tree = EngineProcess.getDEPTree(taggers, analyzer, parser, identifier, labeler, tokens);
		    result.add(wrap(tree));

		}
		in.close();
		return result;
	    } catch (Exception e) {
		return null;
	    }
	}
    }

    public static ClearNLPHandler handler;


    public static ClearNLP.Processor<ClearNLP.Iface> processor;
	
    public static void main(String [] args) {
	try {
	    handler = new ClearNLPHandler();
	    processor = new ClearNLP.Processor<ClearNLP.Iface>(handler);
	    
	    Runnable simple = new Runnable() {
		    public void run() {
			simple(processor);
		    }
		};      
	    new Thread(simple).start();
	} catch (Exception x) {
	    x.printStackTrace();
	}
    }
	
    public static void simple(ClearNLP.Processor<ClearNLP.Iface> processor) {
	try {
	    TServerTransport serverTransport = new TServerSocket(9090);
	    TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));

	    // Use this for a multithreaded server
	    // TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
	    
	    System.out.println("Starting the simple server...");
	    server.serve();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

}

