namespace java org.ets.nlp

/**
 * the thrift representation of a single node
 */

struct TDepNode {
       1: string id;
       2: string word;
       3: string lemma;
       4: string pos;
       5: string feats;
       6: string headId;
       7: string deprel;
       8: string sheads;
}



typedef list<TDepNode> TDepTree 





service ClearNLP {

list<string> labelString(1: string inString)
list<string> labelFile(1: string inFile) 


}