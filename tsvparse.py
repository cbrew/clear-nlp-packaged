import pandas
import sys



features = __import__(sys.argv[1])


features.oninit()

q = pandas.read_table(sys.argv[2])


q[sys.argv[1]] = q.text.map(features.featureize)
q.to_csv(sys.argv[3],sep='\t',index=False)
features.onexit()
