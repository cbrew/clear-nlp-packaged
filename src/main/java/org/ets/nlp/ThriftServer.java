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



import com.googlecode.clearnlp.component.AbstractComponent;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.engine.EngineGetter;
import com.googlecode.clearnlp.nlp.NLPDecode;
import com.googlecode.clearnlp.nlp.NLPLib;
import com.googlecode.clearnlp.reader.AbstractReader;
import com.googlecode.clearnlp.segmentation.AbstractSegmenter;
import com.googlecode.clearnlp.tokenization.AbstractTokenizer;
import com.googlecode.clearnlp.util.UTInput;
import com.googlecode.clearnlp.util.UTOutput;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;



public class ThriftServer {
	
    public static class ClearNLPHandler implements ClearNLP.Iface {
		
	static private final String language = AbstractReader.LANG_EN;



	static private AbstractTokenizer tokenizer;
    static private AbstractComponent tagger; 
    static private AbstractComponent analyzer;
    static private AbstractComponent parser;   
    static private AbstractComponent identifier;  
   	static private AbstractComponent classifier;  
    static private AbstractComponent labeler;  
    static private AbstractComponent[] components;
        

	public ClearNLPHandler () {
	    try {

	    	tokenizer  = EngineGetter.getTokenizer(language, ThriftServer.class.getResourceAsStream("/dictionary-1.2.0.zip"));
	    	tagger    = EngineGetter.getComponent(ThriftServer.class.getResourceAsStream("/ontonotes-en-pos-1.3.0.jar") , language, NLPLib.MODE_POS);
	    	analyzer =  EngineGetter.getComponent(ThriftServer.class.getResourceAsStream("/dictionary-1.2.0.zip") , language, NLPLib.MODE_MORPH);
            parser =   EngineGetter.getComponent(ThriftServer.class.getResourceAsStream("/ontonotes-en-dep-1.3.0.jar") , language, NLPLib.MODE_DEP);
            identifier =  EngineGetter.getComponent(ThriftServer.class.getResourceAsStream("/ontonotes-en-pred-1.3.0.jar") , language, NLPLib.MODE_PRED);
            classifier = EngineGetter.getComponent(ThriftServer.class.getResourceAsStream("/ontonotes-en-pred-1.3.0.jar") , language, NLPLib.MODE_ROLE);
            labeler = EngineGetter.getComponent(ThriftServer.class.getResourceAsStream("/ontonotes-en-srl-1.3.0.jar") , language, NLPLib.MODE_SRL);
            AbstractComponent [] comps = {tagger, analyzer, parser, identifier, classifier, labeler};
            components = comps;
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

		AbstractSegmenter segmenter = EngineGetter.getSegmenter(language, tokenizer);
        NLPDecode nlp = new NLPDecode();

	    try {


			List<List<TDepNode>> result = new ArrayList<List<TDepNode>>();



    
	
    
		for (List<String> tokens : segmenter.getSentences(in)){    

			DEPTree tree = nlp.toDEPTree(tokens);
                        
            // for (AbstractComponent component : components){
            //        component.process(tree);
		   // }
		    result.add(wrap(tree));


		}
		in.close();
		return result;
	    } catch (Exception e) {
	    	System.out.println(e);
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

