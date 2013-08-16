import parse

parse.oninit()
for result in parse.client.labelStringRaw('Please do not lean out of the window when the train is in motion.'):
    print result
for result in parse.client.labelStringRaw('deoxyribonucleic acid with Chinese tendencies.'):
	print result

phrases = ['Lasix 40-mg p.o. q.d.', 
			'Lasix forty-milligrams po qd'
			'propranolol 50-mg',
			'these doses of antihypertensives',
			'the beta blocker', 
			]
for phrase in phrases:
	phrase = phrase.strip()
	if not phrase.endswith('.'):
		phrase += '.'

	print phrase
	for result in parse.client.labelStringRaw(phrase):
		print result

parse.onexit()
