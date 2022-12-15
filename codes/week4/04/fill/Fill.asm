// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input. 
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, the
// program clears the screen, i.e. writes "white" in every pixel.

@SCREEN
D = A 
@0
M = D 
@24575
D = A 
@1
M = D 

(LOOP)
    @KBD 
    D = M 
    @FILL
    D; JGT
    @CLEAR 
    0;JMP 
      

(FILL)
    @0
    D = M 
    @1 
    D = D - M 
    @LOOP 
    D; JGT

    @0 
    A = M
    M = -1
    @0
    M = M + 1
    @FILL
    0; JMP

(CLEAR)
    @0
    M = M - 1
    D = M 
    @SCREEN
    D = D - A
    @LOOP 
    D; JLT

    @0 
    A = M
    M = 0
    @CLERA
    0; JMP
