use std::fs::File;
use std::io::{self, BufWriter, Write};
use crate::cmd_type::CmdType;

pub(crate) struct CodeWriter {
    jump_counter: i32,
    out_printer: BufWriter<File>,
}

impl CodeWriter {
    pub fn new(file_out: &str) -> io::Result<Self> {
        let file = File::create(file_out)?;
        let out_printer = BufWriter::new(file);
        Ok(CodeWriter {
            jump_counter: 0,
            out_printer,
        })
    }

    pub fn write_assembly(&mut self, cmd_type: CmdType, arg1: Option<String>, arg2: Option<i32>) -> io::Result<()> {
        let asm_code = match cmd_type {
            CmdType::CArithmetic => self.write_arithmetic(&arg1.unwrap()),
            CmdType::CPush => self.write_push(&arg1.unwrap(), arg2.unwrap()),
            CmdType::CPop => self.write_pop(&arg1.unwrap(), arg2.unwrap()),
            _ => unreachable!(),
        };
        self.out_printer.write_all(asm_code.as_bytes())?;
        Ok(())
    }

    fn get_segment_and_direct(&self, segment: &str, index: i32) -> (String, bool) {
        match segment {
            "local" => ("LCL".to_string(), false),
            "argument" => ("ARG".to_string(), false),
            "this" => ("THIS".to_string(), false),
            "that" => ("THAT".to_string(), false),
            "pointer" => {
                let seg = if index == 0 { "THIS" } else { "THAT" };
                (seg.to_string(), true)
            }
            "static" => (format!("{}", 16 + index), true),
            _ => panic!("Invalid segment"),
        }
    }

    fn write_arithmetic(&mut self, command: &str) -> String {
        match command {
            "add" => format!("{}M=M+D\n", CodeWriter::arithmetic_template1()),
            "sub" => format!("{}M=M-D\n", CodeWriter::arithmetic_template1()),
            "and" => format!("{}M=M&D\n", CodeWriter::arithmetic_template1()),
            "or" => format!("{}M=M|D\n", CodeWriter::arithmetic_template1()),
            "gt" | "lt" | "eq" => {
                let jump_type = match command {
                    "gt" => "JLE",
                    "lt" => "JGE",
                    "eq" => "JNE",
                    _ => unreachable!(),
                };
                self.jump_counter += 1;
                format!("{}", self.arithmetic_template2(jump_type))
            },
            "not" => "@SP\nA=M-1\nM=!M\n".to_string(),
            "neg" => "D=0\n@SP\nA=M-1\nM=D-M\n".to_string(),
            _ => unreachable!(),
        }
    }

    fn write_push(&self, segment: &str, value: i32) -> String {
        match segment {
            "constant" => format!("@{}\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n", value),
            "temp" => format!("@R{}\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n",value + 5),
            _ => {
                let (seg, is_direct) = self.get_segment_and_direct(segment, value);
                self.push_template(&seg, value, is_direct)
            }
        }
    }

    fn write_pop(&self, segment: &str, value: i32) -> String {
        match segment {
            "temp" => format!("@SP\nAM=M-1\nD=M\n@R{}\nM=D\n", value + 5),
            _ => {
                let (seg, is_direct) = self.get_segment_and_direct(segment, value);
                self.pop_template(&seg, value, is_direct)
            }
        }
    }

    fn arithmetic_template1() -> String {
        "@SP\nAM=M-1\nD=M\nA=A-1\n".to_string()
    }

    fn arithmetic_template2(&self, jump_type: &str) -> String {
        format!(
            "@SP\n\
             AM=M-1\n\
             D=M\n\
             A=A-1\n\
             D=M-D\n\
             @FALSE{}\n\
             D;{}\n\
             @SP\n\
             A=M-1\n\
             M=-1\n\
             @CONTINUE{}\n\
             0;JMP\n\
             (FALSE{})\n\
             @SP\n\
             A=M-1\n\
             M=0\n\
             (CONTINUE{})\n",
            self.jump_counter, jump_type, self.jump_counter, self.jump_counter, self.jump_counter
        )
    }

    fn push_template(&self, segment: &str, index: i32, is_direct: bool) -> String {
        let no_pointer_code = if is_direct {
            "".to_string()
        } else {
            format!("@{}\nA=D+A\nD=M\n", index)
        };
        format!(
            "@{}\n\
             D=M\n\
             {}\
             @SP\n\
             A=M\n\
             M=D\n\
             @SP\n\
             M=M+1\n",
            segment, no_pointer_code
        )
    }

    fn pop_template(&self, segment: &str, index: i32, is_direct: bool) -> String {
        let no_pointer_code = if is_direct {
            "D=A\n".to_string()
        } else {
            format!("D=M\n@{}\nD=D+A\n", index)
        };
        format!(
            "@{}\n\
             {}\
             @R13\n\
             M=D\n\
             @SP\n\
             AM=M-1\n\
             D=M\n\
             @R13\n\
             A=M\n\
             M=D\n",
            segment, no_pointer_code
        )
    }
}
