import pandas
import sys
sys.path.append('./target/generated-sources/python/gen-py')

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
        targets = [x for x in sentence if x.id is i]
        assert len(targets) == 1
        
        return [(r,targets[0])]
    except:
        return []





def link_ids(sentence):
    for node in sentence:
        if node.headId is '_':
            node.headId = None
        else:
            targets = [x for x in sentence if x.id is node.headId]
            assert len(targets) == 1
            node.headId = targets[0]
        node.sheads = dsplit(node.sheads,sentence) 




def generate_triples(sentence):
    for x in sentence:
        if x.headId:
            yield ("syn",x.headId.lemma,x.deprel,x.lemma)
        for l in x.sheads:
            yield ("sem",l[1].lemma,l[0],x.lemma)


def package_sentence(sentence):
    link_ids(sentence)
    
    return list(generate_triples(sentence))


def package_sentences(result):
    return [package_sentence(sentence) for sentence in result]
        





def featureize(text):
    """
    Yield the features associated with each 
    sentence.
    """
    global client
    text = str(text)
    if text[-1] not in (".","!","?"):
        text += '.'
    result = client.labelString(text)
    return [sentence for sentence in package_sentences(result)]


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
    print featureize('The dog might get too cool to do that')
    onexit()





