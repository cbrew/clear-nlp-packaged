/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ets.nlp;

import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

public class ClearNLPClient {
	public static void main(String [] args) {


		try {
			TTransport transport;
			int PORT = 9090;
			if( args.length>0 )  PORT = Integer.parseInt( args[0] );            
			transport = new TSocket("localhost", PORT);
			transport.open();
			TProtocol protocol = new  TBinaryProtocol(transport);
			ClearNLP.Client client = new ClearNLP.Client(protocol);

			perform(client);

			transport.close();
		} catch (TException x) {
			x.printStackTrace();
            System.out.println("Don't forget to supply port as a parameter if different from 9090:\n    java -cp uber-clearserver-1.0.jar ClearNLPClient 9091");
		} 
	}



	private static void perform(ClearNLP.Client client) throws TException
	{

		for (String sentence : client.labelStringRaw("The man bit the dog , really ! And then jumped.")) {
			System.out.println(sentence);
			System.out.println();  

		}
	}  
}
