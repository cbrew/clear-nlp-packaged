clear-nlp-packaged
==================

A packaged client and server for Clear NLP 1.3.0. This software defines an interface for a client/server system that takes as input raw text and produces analyses based on tokenization, lemmatization, part-of-speech tagging and dependency parsing. 

The software uses the Thrift framework to define and use a service that offers labeling for strings and files. The NLP functionality is from Jinho D. Choi's Clear NLP, which is Apache licensed and available on 

@url https://code.google.com/p/clearnlp/

The first step is to download and build the software. The software is kept, for now, at 

@url https://github.com/cbrew/clear-nlp-packaged

This version contains no ETS specific material, other than the use of org.ets.nlp as the Java package name.

To build and run the server, you download from github, then call

$ mvn package

which downloads the dependencies and builds a jar file 

target/uber-clearpak-0.0.2-SNAPSHOT.jar

This contains the code, and all the statistical models necessary to run the
server. That can be done easily by invoking:

java -Xmx3g -jar target/uber-clearpak-0.0.2-SNAPSHOT.jar

I do this in a @code screen session. It takes a while to load models, then sets
up a server waiting for calls from clients. Thrift handles the server client interaction nicely. 
Presumably it works across machines, since that is the point of Thrift, but I haven't explored that fully.


Once the server is running, the following call can be made.

```code
$ python demo.py
0       _R_     _R_     _R_     _       _       _       _
1       Please  please  UH      _       4       intj    4:AM-DIS
2       do      do      VB      _       4       aux     _
3       not     not     RB      _       4       neg     4:AM-NEG
4       lean    lean    VB      pb=lean.01      0       root    _
5       out     out     IN      _       4       prep    4:A2
6       of      of      IN      _       5       prep    _
7       the     the     DT      _       8       det     _
8       window  window  NN      _       6       pobj    _
9       when    when    WRB     _       12      advmod  12:R-AM-TMP
10      the     the     DT      _       11      det     _
11      train   train   NN      _       12      nsubj   12:A1
12      is      be      VBZ     pb=be.01        4       advcl   4:AM-TMP
13      in      in      IN      _       12      prep    12:A2
14      motion  motion  NN      _       13      pobj    _
15      .       .       .       _       4       punct   _
```

There is also a client in the Java part of the code, and Thrift supports many other languages.
