"""
Code to generate features from a module loaded
on the command line and map it to a column in the .tsv file.
"""

import pandas
import sys
import logging

logging.basicConfig(level=logging.INFO)


features = __import__(sys.argv[1])


features.oninit()

q = pandas.read_table(sys.argv[2])

def control_length(fgen,limit=50000):
    """
    Ensure text is not too long. 
    """
    text = "\n".join(fgen)
    return text[:limit]


q[sys.argv[1]] = q.text.map(lambda x: control_length(features.featureize(x)))
q.to_csv(sys.argv[3],sep='\t',index=False)
features.onexit()
