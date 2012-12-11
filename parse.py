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



def dsplit(x):
    """
    Decode the semantic role relations.
    ; separates multiple relations
    : separates index from relation name
    
    output is a set of index,relname pairs
    """
    if ";" in x:
        return {dsplit(y).pop() for y in x.split(";")}
    try:
        (i,r) = x.split(":")
        return {(int(i),r)}
    except:
        return frozenset()

def package_sentence(sentence):
    srel=[dsplit(x.sheads) for x in sentence]
    df = pandas.DataFrame(dict(
            word=[x.word for x in sentence],
            id =[x.id for x in sentence],
            lemma=[x.lemma for x in sentence],
            pos=[x.pos for x in sentence],
            feats = [x.feats for x in sentence],
            dephead=[x.headId for x in sentence],
            deprel=[x.deprel for x in sentence],
            srel = srel),
            columns=['id','word','lemma','pos','feats','dephead','deprel','srel'])
    return df

def package_sentences(result):
    for sentence in result:
        yield package_sentence(sentence)





def featureize(text):
    """
    Yield the features associated with each 
    sentence.
    """
    global client
    text = str(text)
    result = client.labelString(text + ' . ')
    for sentence in package_sentences(result):
        yield sentence

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









