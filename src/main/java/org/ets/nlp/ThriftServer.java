package org.ets.nlp;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
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



    private List<List<String>> wrap(List<DEPNode> tokens){

    ArrayList<List<String>> result = new ArrayList<List<String>> ();
    for(DEPNode token: tokens){
      result.add(Arrays.asList(token.toStringSRL().split("\t")));
    }
    return result;

  }

/*
	public List tokenize(String inputFile) {

    try {
    BufferedReader in = UTInput.createBufferedFileReader(inputFile);

    String line = null;
    List<List<List<String>>> result = new ArrayList<List<List<String>>>();
    
    AbstractTokenizer tokenizer = EngineGetter.getTokenizer(language, 
			ThriftServer.class.getResourceAsStream("/dictionary-1.1.0.zip"));

    AbstractSegmenter segmenter = EngineGetter.getSegmenter(language, tokenizer);
    
    for (List<String> tokens : segmenter.getSentences(in)){    
    
      result.add(wrap(tokens));

    }





    in.close();
      return result;
		} catch (Exception e) {
      return null;
    }
	}
*/

  public List labelString(String inputString)
  {

    DEPTree tree = EngineProcess.getDEPTree(taggers, analyzer, parser, identifier, labeler, 
      Arrays.asList(inputString.split("[\t\n ]+"))); 
    return wrap(tree);
  }

  public List labelFile(String inputFile) {

    try {
    BufferedReader in = UTInput.createBufferedFileReader(inputFile);

    String line = null;
    List<List<List<String>>> result = new ArrayList<List<List<String>>>();
    
    AbstractTokenizer tokenizer = EngineGetter.getTokenizer(language, ThriftServer.class.getResourceAsStream("/dictionary-1.1.0.zip"));

    AbstractSegmenter segmenter = EngineGetter.getSegmenter(language, tokenizer);
    
    for (List<String> tokens : segmenter.getSentences(in)){    
      DEPTree tree = EngineProcess.getDEPTree(taggers, analyzer, parser, identifier, labeler, tokens);
      /* convert pos node into list likes 
          id,form,lemma and pos
      */
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

