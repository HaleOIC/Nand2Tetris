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


string dest_check( const string& );
string comp_check( const string& );
string jump_check( const string& );

auto isalph = []( char c ){ return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'); };


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

string comp_check( const string& comp ){
	if ( comp == "0" ) return "0101010";
	else if ( comp == "1" ) return "0111111";
	else if ( comp == "-1" ) return "0111010";
	else if ( comp == "D" ) return "0001100";
	else if ( comp == "A" ) return "0110000";
	else if ( comp == "M" ) return "1110000";
	else if ( comp == "!D" ) return "0001101";
	else if ( comp == "!A" ) return "0110001";
	else if ( comp == "!M" ) return "1110001";
	else if ( comp == "-D" ) return "0001111";
	else if ( comp == "-A" ) return "0110011";
	else if ( comp == "-M" ) return "1110011";
	else if ( comp == "D+1" ) return "0011111";
	else if ( comp == "A+1" ) return "0110111";
	else if ( comp == "M+1" ) return "1110111";
	else if ( comp == "D-1" ) return "0001110";
	else if ( comp == "A-1" ) return "0110010";
	else if ( comp == "M-1" ) return "1110010";
	else if ( comp == "D+A" ) return "0000010";
	else if ( comp == "D+M" ) return "1000010";
	else if ( comp == "D-A" ) return "0010011";
	else if ( comp == "D-M" ) return "1010011";
	else if ( comp == "A-D" ) return "0000111";
	else if ( comp == "M-D" ) return "1000111";
	else if ( comp == "D&A" ) return "0000000";
	else if ( comp == "D&M" ) return "1000000";
	else if ( comp == "D|A" ) return "0010101";
	return "1010101";
}


string dest_check( const string& tar ){
	if ( tar.length() == 0 ) return "000";
	vector<int> flags{0,0,0};
	string rv;
	if ( tar.find('M') != string::npos ) flags[0] = 1;
	if ( tar.find('D') != string::npos ) flags[1] = 1;
	if ( tar.find('A') != string::npos  ) flags[2] = 1;
	for ( int i = 2 ;  i >= 0; --i )  rv += flags[i] ? '1' : '0';
	return rv;
}

string jump_check( const string& tar ){
	if ( tar.length() == 0 ) return "000";
	else if ( tar == "JGT" ) return "001";
	else if ( tar == "JEQ" ) return "010";
	else if ( tar == "JGE" ) return "011";
	else if ( tar == "JLT" ) return "100";
	else if ( tar == "JNE" ) return "101";
	else if ( tar == "JLE" ) return "110";
	return "111";
}





string trim( const string& tar ){
	int posfront = 0, postail = tar.length() - 1 ;
	if ( tar.find("//") != string::npos )  postail = tar.find("//") - 1;
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
	vector<int> bits;
	string rv{"0"};
	int stoval = val;
	for ( int i = 0; i < 15; ++i ) {
		bits.push_back( stoval % 2 );
		stoval >>= 1;
	}
	for ( int i = 14; i >= 0; --i ) 	rv += bits[i] ? '1' : '0'; 
	return rv;
}

string transfer_C_instruction( const string& tar ){

	string dest,comp,jump,rv{"111"};
	auto equalpos = tar.find('='), semipos = tar.find(';');
	if ( equalpos != string::npos ) {
		dest = tar.substr( 0, equalpos );
		if ( semipos != string::npos ){
			auto pos = equalpos + 1;
			comp = tar.substr( pos, semipos - pos );
			pos = semipos + 1;
			jump = tar.substr( pos, tar.length() - pos ); 
		}else {
			auto pos = equalpos + 1;
			comp = tar.substr( pos, tar.length() - pos );
		}
	}else {
		if ( semipos != string::npos ) {
			comp = tar.substr( 0, semipos );
			auto pos = semipos + 1;
			jump = tar.substr( pos, tar.length() - pos );
		} else {
			comp = tar;
		}
	}
	rv += comp_check(comp) + dest_check(dest) +  jump_check(jump);
	return rv;
}




void transfer_hackfile( vector<string> vec, ofstream& outputfile ){
	vector<string> validlinevec;
	// first pass
	int cnt = 0;
	for ( auto& each : vec ){
		if ( each.length() == 0 ) continue;
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

	// for (auto each : validlinevec ) cout << each << endl;

	// second pass 
	int totReg = 16;																	// as the count flag for the reg
	for ( auto& each : validlinevec ){
		string instr;
		if ( each[0] == '@' ) {

			// solve the A instruction

			string label = each.substr( 1, each.length() - 1 );
			unsigned val;
			if ( label.length() == 0 ) continue;
			if ( isalph( label[0] ) ) {
				if ( symtab.find(label) == symtab.end() ) {

				// this label is a regester

					symtab.insert( { label, totReg } ); 
					++totReg;
				} 	
				val = symtab[label];
			} else {

				// this label is a integer.
				val = (unsigned)stoi( label );
			}

			instr = transfer_A_instruction( val );

		} else {

			// solve the C instruction
			instr = transfer_C_instruction( each );

		}
		// cout << instr << endl;
		outputfile << instr << endl;
	}
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