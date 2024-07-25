mod parser;
mod code_writer;
mod cmd_type;

use std::{env, io};
use std::fs::File;
use std::io::{BufRead, BufReader};
use crate::parser::parse_command;

fn main() -> Result<(), io::Error>{
    let args: Vec<String> = env::args().collect();

    // error message
    if args.len() <= 1 {
        eprintln!("Usage: cargo run <vm_file>");
        return Err(io::Error::new(io::ErrorKind::InvalidInput, "No input file provided"));
    }


    // check whether the arguments end with '.vm'
    for file in &args[1..] {
        if file.ends_with(".vm") {
            let output_path = format!("{}.asm", &file[..file.len() - 3]);

            // Open the input file
            let input_file = File::open(file)?;
            let reader = BufReader::new(input_file);

            // Create or overwrite the output file
            let mut code_writer = code_writer::CodeWriter::new(&output_path)?;

            // Read line by line
            for line in reader.lines() {
                let line = line?;
                let line = line.trim();
                // filter comment
                if line.is_empty() || line.starts_with("//") {
                    continue;
                }

                // parse given command
                let (cmd_type, arg1, arg2) = parse_command(&line);

                // according to type, write into file
                let write_rv = code_writer.write_assembly(cmd_type, arg1, arg2);
                if write_rv.is_err() {
                    return Err(io::Error::new(io::ErrorKind::InvalidData, "Failed to write into file"));
                }
            }
        } else {
            eprintln!("Error: The file '{}' does not have a .vm extension", file);
            return Err(io::Error::new(io::ErrorKind::InvalidInput, "Invalid file extension"));
        }
    }

    return Ok(());
}
