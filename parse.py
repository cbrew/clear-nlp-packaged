import sys
sys.path.append('./target/generated-sources/python/gen-py')

from sample import ClearNLP
from sample.ttypes import *
from sample.constants import *

from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

def featureize(text):
    global client
    text = str(text)
    result = client.labelString(text + ' . ')
    return result

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









