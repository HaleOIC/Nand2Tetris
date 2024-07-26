all:
	javac *.java -d ./out
	cd out && java VMTranslator ../../ProgramFlow/BasicLoop/BasicLoop.vm
	cd out && java VMTranslator ../../ProgramFlow/FibonacciSeries/FibonacciSeries.vm
	cd out && java VMTranslator ../../FunctionCalls/SimpleFunction/SimpleFunction.vm
	cd out && java VMTranslator ../../FunctionCalls/NestedCall
	cd out && java VMTranslator ../../FunctionCalls/FibonacciElement
	cd out && java VMTranslator ../../FunctionCalls/StaticsTest
	
clean:
	rm -rf ./*.class
	rm -rf out
