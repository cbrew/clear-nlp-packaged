clear-nlp-packaged
==================

A packaged client and server for Clear NLP 1.3.1. This software defines an interface for a client/server system that takes as input raw text and produces analyses based on tokenization, lemmatization, part-of-speech tagging and dependency parsing. 

The software uses the Thrift framework to define and use a service that offers labeling for strings and files. The NLP functionality is from Jinho D. Choi's Clear NLP, which is Apache licensed and available on 

https://code.google.com/p/clearnlp/

The first step is to download and build the software. The software is kept, for now, at 

https://github.com/cbrew/clear-nlp-packaged

This version contains no ETS specific material, other than the use of org.ets.nlp as the Java package name.

To build and run the server, you download from github, carry out an extra step to download the models, then build.

The extra step, which is necessary because github now has size limits, is to invoke a script.

	./get-models

This script relies on wget, which downloads the models from Jinho Choi's bitbucket space. You will need to install wget 
if you don't already have it. Once the models download, you are ready to build and run the server. First, do:

	mvn package

which downloads the dependencies and builds a jar file :

	target/uber-clearpak-1.4.2-SNAPSHOT.jar

This contains the code, and all the statistical models necessary to run the
server. Next, run the server by invoking:

	java -Xmx3g -jar target/uber-clearpak-1.4.2-SNAPSHOT.jar

I do this in a screen session. It takes a while to load models, then sets
up a server waiting for calls from clients. Thrift handles the server client interaction nicely. 
It works across machines, since that is the point of Thrift. My colleague Daehee Lee, at Nuance,
has similar code for a large number of different parsers.


Once the server is running, the following call can be made.

```code
python demo.py
0	_R_	_R_	_R_	_	_	_	_
1	Please	please	UH	_	4	intj	4:AM-DIS
2	do	do	VB	p2=VBP	4	aux	_
3	not	not	RB	_	4	neg	4:AM-NEG
4	lean	lean	VB	p2=JJ|pb=lean.01	0	root	_
5	out	out	IN	_	4	prep	4:A2-LOC
6	of	of	IN	_	5	prep	_
7	the	the	DT	_	8	det	_
8	window	window	NN	_	6	pobj	_
9	when	when	WRB	_	12	advmod	12:R-AM-TMP
10	the	the	DT	_	11	det	_
11	train	train	NN	_	12	nsubj	12:A1-PPT
12	is	be	VBZ	pb=be.01	4	advcl	4:AM-TMP
13	in	in	IN	_	12	prep	12:A2-PRD
14	motion	motion	NN	_	13	pobj	_
15	.	.	.	_	4	punct	_

0	_R_	_R_	_R_	_	_	_	_
1	deoxyribonucleic	deoxyribonucleic	JJ	p2=NN	2	amod	_
2	acid	acid	NN	_	0	root	_
3	with	with	IN	_	2	prep	_
4	Chinese	chinese	JJ	_	5	amod	_
5	tendencies	tendency	NNS	_	3	pobj	_
6	.	.	.	_	2	punct	_

Lasix 40-mg p.o. q.d.
Lasix forty-milligrams po qdpropranolol 50-mg.
0	_R_	_R_	_R_	_	_	_	_
1	Lasix	lasix	VB	p2=RB|pb=lasix.01	0	root	_
2	forty	#crd#	CD	p2=NN	4	hmod	_
3	-	-	HYPH	_	4	hyph	_
4	milligrams	milligram	NNS	p2=CD	5	nn	_
5	po	po	NN	p2=VBP	6	nn	_
6	qdpropranolol	qdpropranolol	NN	p2=VBP	9	nsubj	_
7	50	0	CD	_	9	hmod	_
8	-	-	HYPH	p2=SYM	9	hyph	_
9	mg	mg	NN	p2=NNS	1	dobj	1:A1
10	.	.	.	_	1	punct	_

these doses of antihypertensives.
0	_R_	_R_	_R_	_	_	_	_
1	these	these	DT	_	2	det	_
2	doses	dose	NNS	_	0	root	_
3	of	of	IN	_	2	prep	_
4	antihypertensives	antihypertensive	NNS	_	3	pobj	_
5	.	.	.	_	2	punct	_

the beta blocker.
0	_R_	_R_	_R_	_	_	_	_
1	the	the	DT	_	3	det	_
2	beta	beta	NN	p2=JJ	3	nn	_
3	blocker	blocker	NN	_	0	root	_
4	.	.	.	_	3	punct	_

red belt and shirt and suspenders.
0	_R_	_R_	_R_	_	_	_	_
1	red	red	JJ	_	2	amod	_
2	belt	belt	NN	_	0	root	_
3	and	and	CC	_	2	cc	_
4	shirt	shirt	NN	_	2	conj	_
5	and	and	CC	_	4	cc	_
6	suspenders	suspender	NNS	p2=NN	4	conj	_
7	.	.	.	_	1	punct	_
```

There is also a client in the Java part of the code, and Thrift supports many other languages.
