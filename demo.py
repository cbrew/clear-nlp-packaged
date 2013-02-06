import parse

parse.oninit()
for result in parse.client.labelStringRaw('Please do not lean out of the window when the train is in motion.'):
    print result
parse.onexit()
