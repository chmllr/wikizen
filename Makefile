all:
	jsx --harmony src/ test/builds/
	nodeunit
