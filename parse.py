import sys
sys.path.append('./target/generated-sources/python/gen-py')
from collections import namedtuple
import logging

from sample import ClearNLP
from sample.ttypes import *
from sample.constants import *

from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol



def dsplit(x,sentence):
    """
    Decode the semantic role relations.
    ; separates multiple relations
    : separates index from relation name
    output is a set of index,relname pairs
    """
    if ";" in x:
        r = []
        for y in x.split(";"):
            r += dsplit(y,sentence)
        return r
    try:
        (i,r) = x.split(":")
        targets = [x for x in sentence if x.id == i]
        if len(targets) != 1:
            logging.info((targets,node.headId,[x.id for x in sentence]))
            sys.exit(0)
        
        return [(r,targets[0])]
    except:
        return []





def link_ids(sentence):
    for node in sentence:
        if node.headId is '_':
            node.headId = None
        else:
            targets = [x for x in sentence if x.id == node.headId]
            if len(targets) != 1:
                logging.info((targets,node.headId,[x.id for x in sentence]))
                sys.exit(0)
            node.headId = targets[0]
        node.sheads = dsplit(node.sheads,sentence) 



Dependency = namedtuple("Dependency",("deptype","head","relation","dependent"))
class Token(namedtuple("Token",("word","lemma","pos"))):
    __slots__ = ()
    def __str__(self):
        return "%s/%s" % (self.word,self.pos)



def generate_triples(sentence):
    for x in sentence:
        if x.headId:
            yield Dependency(deptype="syn",
                             head=Token(word= x.headId.word,
                                        lemma=x.headId.lemma,
                                        pos=x.headId.pos),
                             relation=x.deprel,
                             dependent=Token(word=x.word,lemma=x.lemma,pos=x.pos))
        for l in x.sheads:
            yield Dependency(deptype="sem",
                             head=Token(word=l[1].word,
                                        lemma=l[1].lemma,
                                        pos=l[1].pos),
                                        relation=l[0],
                             dependent=Token(word=x.word,lemma=x.lemma,pos=x.pos))
                             


def dep_string(x):
    return "%s:%s-%s-%s" % x


def featureize_sentence(sentence):
    link_ids(sentence)
    return "\n".join(dep_string(x) for x in generate_triples(sentence))


def package_sentences(result):
    return [featureize_sentence(sentence) for sentence in result]
        





def featureize(text):
    """
    Yield the features associated with each 
    sentence.
    """
    global client
    text = str(text)
    if text[-1] not in (".","!","?"):
        text += '.'
    logging.info(text)
    result = client.labelString(text)


    return "\n\n".join(featureize_sentence(x) for x in result)
        


def oninit():
    global client
    global transport
    try:
        # Make socket

        transport = TSocket.TSocket('localhost', 9090)
        # Buffering is critical. Raw sockets are very slow
        transport = TTransport.TBufferedTransport(transport)

        # Wrap in a protocol
        protocol = TBinaryProtocol.TBinaryProtocol(transport)

        # Create a client to use the protocol encoder
        client = ClearNLP.Client(protocol)
        # Connect!
        transport.open()
    except:
        pass



def onexit():
    global transport
    try:
        transport.close()
    except Thrift.TException, tx:
        print "%s" % (tx.message)



if __name__ == "__main__":
    oninit()
    print featureize('The dog might be too cool to do that. Which would annoy the owner')
    onexit()





