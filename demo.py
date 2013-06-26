import parse

parse.oninit()
for result in parse.client.labelStringRaw('Please do not lean out of the window when the train is in motion.'):
    print result


for result in parse.client.labelStringRaw('Ed slept .'):
    print result

for result in parse.client.labelStringRaw('Did Ed sleep ?'):
    print result


parse.onexit()
