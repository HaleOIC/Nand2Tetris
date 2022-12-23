#include <fstream>
#include <string>
#include <vector>
#include <iostream>
#include <map>

using namespace std;

map<string, unsigned> symtab;

// related functions
void init_symbol_table();
void transfer_hackfile( vector<string>, ofstream& );
string transfer_A_instruction( const unsigned& );
string transfer_C_instruction( const string& );
string trim(const string&);

auto isalph = []( char c ){ return c >= 'a' && c <= 'z'; };


int main( int argc, char** argv)
{

	/**
	 * solve the related file operations
	 */

	if ( argc < 3 ) {
		cerr << "Please input the right address you want solve or store!!!" << endl;
	}
	ifstream infile( argv[1] );
	ofstream outfile( argv[2] );
	if ( !infile ) {
		cerr << "Please input the valid file !!" << endl;
	}

	/**
	 * transfer the contents in the infile into the program 
	 * solve each instruction line by line 
	 * and the output into the file.
	 */

	init_symbol_table();
	string line;
	vector<string> strvec;
	while ( getline( infile, line ) ) strvec.push_back( line );
	transfer_hackfile( strvec, outfile );
	return 0;

}


string trim( const string& tar ){
	int posfront = 0, postail = tar.length() - 1;
	int len = tar.length();
	while ( posfront < len ) {
		if ( tar[posfront] == ' ' ) ++posfront;
		else break;
	}
	while ( postail >= 0 ) {
		if ( tar[postail] == ' ' ) --postail;
		else break;
	}
	string rv = tar.substr( posfront, postail - posfront + 1 );
	return rv;
}


string transfer_A_instruction( const unsigned& val){
	return "hahaha";
}

string transfer_C_instruction( const string& tar ){
	return " ahahha ";
}




void transfer_hackfile( vector<string> vec, ofstream& outputfile ){
	vector<string> validlinevec;
	// first pass
	int cnt = 0;
	for ( auto& each : vec ){
		if ( each[0] == '/' && each[1] == '/' ) continue;
		if ( each[0] == '(' ) {
			auto pos = each.find_first_of( ')' );
			string label = each.substr( 1, pos - 1 );
			symtab.insert( { label, cnt });
			continue;
		}
		validlinevec.push_back( trim(each) );		
		++cnt;		
	}

	for ( auto each : validlinevec ) cout << each << endl;


	// second pass 
	// int cnt = 0; 																		// as the count flag for the program line
	// int totReg = 16;																	// as the count flag for the reg
	// for ( auto& each : vec ){
	// 	if ( each[0] == '/'  && each[0] == '/' ) continue;
	// 	else if ( each[0] == '@' ) {
	// 		// solve the A instruction
	// 		string label = each.substr( 1, each.length() - 1 ), instr;
	// 		if ( label.length() > 0 && isalpha( label[0] ) ) {
	// 			if ( symtab[label] )  s
	// 		}
	// 	}
	// 	else if ( each[0] == '(' ) {

	// 	}
	// 	else {

	// 	}
	// }

}



void init_symbol_table(){
	symtab.insert( {"R0", 0} );
	symtab.insert( {"R1", 1} );
	symtab.insert( {"R2", 2} );
	symtab.insert( {"R3", 3} );
	symtab.insert( {"R4", 4} );
	symtab.insert( {"R5", 5} );
	symtab.insert( {"R6", 6} );
	symtab.insert( {"R7", 7} );
	symtab.insert( {"R8", 8} );
	symtab.insert( {"R9", 9} );
	symtab.insert( {"R10", 10} );
	symtab.insert( {"R11", 11} );
	symtab.insert( {"R12", 12} );
	symtab.insert( {"R13", 13} );
	symtab.insert( {"R14", 14} );
	symtab.insert( {"R15", 15} );
	symtab.insert( {"SCREEN", 16384} );
	symtab.insert( {"KBD", 24576} );
	symtab.insert( {"SP", 0} );
	symtab.insert( {"LCL", 1} );
	symtab.insert( {"ARG", 2} );
	symtab.insert( {"THIS", 3} );
	symtab.insert( {"THAT", 4} );
}