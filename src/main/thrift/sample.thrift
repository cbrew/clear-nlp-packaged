namespace java org.ets.nlp

/**
 * the thrift representation of a single node
 */
typedef list<string> TDepNode
typedef list<TDepNode> TDepTree 

service ClearNLP {

TDepTree labelString(1: string inString)
list<TDepTree> labelFile(1: string inFile) 


}