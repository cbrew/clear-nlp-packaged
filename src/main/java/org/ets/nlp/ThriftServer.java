package org.ets.nlp;

import java.lang.StringBuilder;
import java.lang.Integer;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import com.clearnlp.component.AbstractComponent;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.nlp.NLPMode;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.segmentation.AbstractSegmenter;
import com.clearnlp.tokenization.AbstractTokenizer;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// New imports introduced by Diane :)
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerTransport;


public class ThriftServer {
	private static Logger logger = LoggerFactory.getLogger(ThriftServer.class);

	public static class ClearNLPHandler implements ClearNLP.Iface {

		static private final String language = AbstractReader.LANG_EN;
		static private final String modelType = "general-en";

		static private AbstractTokenizer tokenizer;
		static private AbstractComponent tagger;
		static private AbstractComponent analyzer;
		static private AbstractComponent parser;
		static private AbstractComponent identifier;
		static private AbstractComponent classifier;
		static private AbstractComponent labeler;
		static private AbstractComponent[] components;
		static private AbstractSegmenter segmenter = NLPGetter.getSegmenter(language, tokenizer);
		static private Logger logger = LoggerFactory.getLogger(ThriftServer.class);

		public ClearNLPHandler () {
			try {
				//AbstractSegmenter segmenter = NLPGetter.getSegmenter(s_language, NLPGetter.getTokenizer(s_language));
				tokenizer  = NLPGetter.getTokenizer(language);
				tagger     = NLPGetter.getComponent(modelType, language, NLPMode.MODE_POS);
				analyzer   = NLPGetter.getComponent(modelType, language, NLPMode.MODE_MORPH);
				parser     = NLPGetter.getComponent(modelType, language, NLPMode.MODE_DEP);
				identifier = NLPGetter.getComponent(modelType, language, NLPMode.MODE_PRED);
				classifier = NLPGetter.getComponent(modelType, language, NLPMode.MODE_ROLE);
				labeler    = NLPGetter.getComponent(modelType, language, NLPMode.MODE_SRL);

				AbstractComponent [] comps = {tagger, analyzer, parser, identifier, classifier, labeler};
				components = comps;
			} catch (Exception e) {
				//java.io.StringWriter sw = new java.io.StringWriter();
				//java.io.PrintWriter pw = new java.io.PrintWriter(sw);
				//e.printStackTrace(pw);
				//logger.warn(sw.getBuffer().toString());
				logger.warn("Exception from ClearNLP Server", e);
			}
		}

		private String wrap(List<DEPNode> tokens){

			StringBuilder sb = new StringBuilder();

			for(DEPNode token: tokens){
				String s =  token.toStringSRL();
				sb.append(s);
				sb.append("\n");
			}

			String s = sb.toString();
			return s;
		}


		public List<String> labelStringRaw(String inputString){

			try {
				InputStream is = new ByteArrayInputStream(inputString.getBytes());
				BufferedReader in = new BufferedReader(new InputStreamReader(is));
				List<String> r = labelCommon(in);
				in.close();
				return r;
			} catch (IOException e) {
				logger.warn("Exception from ClearNLP Server", e);
				return null;
			}
		}


		private List<TDepNode> wrap2(List<DEPNode> tokens) {
			ArrayList<TDepNode> result = new ArrayList<TDepNode> ();
			for(DEPNode token: tokens){
				String s =  token.toStringSRL();
				String [] fields = s.split("[\t\n ]+");
				assert fields.length == 8;
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

		public List<List<TDepNode>> labelString(String inputString){

			try {
				InputStream is = new ByteArrayInputStream(inputString.getBytes());
				BufferedReader in = new BufferedReader(new InputStreamReader(is));
				AbstractSegmenter segmenter = NLPGetter.getSegmenter(language, tokenizer);

				List<List<TDepNode> > result = new ArrayList< List<TDepNode> >();
				for (List<String> tokens : segmenter.getSentences(in)){
					DEPTree tree = NLPGetter.toDEPTree(tokens);
					for (AbstractComponent component : components)
						component.process(tree);
					result.add(wrap2(tree));
				}
				return result;

			} catch (Exception e) {
				logger.warn("Exception from ClearNLP Server", e);
				return null;
			}

		}

		public List<String> labelFile(String inputFile) {

			try {
				BufferedReader in = UTInput.createBufferedFileReader(inputFile);
				List<String> r = labelCommon(in);
				in.close();
				return r;
			} catch (Exception e) {
				logger.warn("Exception from ClearNLP Server", e);
				return null;
			}
		}



		private List<String> labelCommon(BufferedReader in){

			AbstractSegmenter segmenter = NLPGetter.getSegmenter(language, tokenizer);

			try {
				List<String> result = new ArrayList<String>();
				for (List<String> tokens : segmenter.getSentences(in)){
					DEPTree tree = NLPGetter.toDEPTree(tokens);
					for (AbstractComponent component : components) {
						component.process(tree);
					}
					result.add(wrap(tree));
				}
				return result;
			} catch (Exception e) {
				//e.printStackTrace();
				logger.warn("Exception from ClearNLP Server", e);
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

			Runnable start = new Runnable() {
				public void run() {
					//simple(processor);
					tThreadPoolServer(processor);
				}
			};
			new Thread(start).start();
		} catch (Exception e) {
			logger.warn("Exception from ClearNLP Server", e);
		}
	}

	public static void simple(ClearNLP.Processor<ClearNLP.Iface> processor) {
		try {
			TServerTransport serverTransport = new TServerSocket(9090);
			TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));
			logger.info("Starting the simple server...");
			server.serve();
		} catch (Exception e) {
			logger.warn("Exception from ClearNLP Server", e);
		}
	}

	public static void tThreadPoolServer(ClearNLP.Processor<ClearNLP.Iface> processor) {
		int THREAD_POOL_SIZE = 1;

		try {
			TServerTransport serverTransport = new TServerSocket(9090);
			TThreadPoolServer.Args args = new TThreadPoolServer.Args(serverTransport);
			args.maxWorkerThreads(THREAD_POOL_SIZE);
			args.processor(processor);
			args.executorService(new ScheduledThreadPoolExecutor(THREAD_POOL_SIZE));
			TServer server = new TThreadPoolServer(args);
			logger.info("Starting the TThreadPoolServer...");
			server.serve();
		} catch (Exception e) {
			logger.warn("Exception from ClearNLP Server", e);
		}
	}
}
