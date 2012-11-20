#!/usr/bin/env python
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
    sentno = 0
    for arg in sys.argv[1:]:
        print arg
        for i, sentence in enumerate(client.labelFile(arg)):
            print i
            for node in sentence:
                print node
        sentno += i + 1

    transport.close()

except Thrift.TException, tx:
    print "%s" % (tx.message)
print sentno
