use lazy_static::lazy_static;
use std::collections::HashMap;
use crate::cmd_type::CmdType;

// When at initialization stage, require resources
lazy_static!{
    static ref COMMAND_TABLE: HashMap<&'static str, CmdType> = {
        let mut m = HashMap::new();
        m.insert("push", CmdType::CPush);
        m.insert("pop", CmdType::CPop);
        m.insert("add", CmdType::CArithmetic);
        m.insert("sub", CmdType::CArithmetic);
        m.insert("neg", CmdType::CArithmetic);
        m.insert("eq", CmdType::CArithmetic);
        m.insert("lt", CmdType::CArithmetic);
        m.insert("and", CmdType::CArithmetic);
        m.insert("or", CmdType::CArithmetic);
        m.insert("not", CmdType::CArithmetic);
        m.insert("label", CmdType::CLabel);
        m.insert("goto", CmdType::CGoto);
        m.insert("function", CmdType::CFunction);
        m.insert("if-goto", CmdType::CIf);
        m.insert("call", CmdType::CCall);
        m.insert("return", CmdType::CReturn);
        m
    };
}

pub fn parse_command(command: &str) -> (CmdType, Option<String>, Option<i32>) {
    println!("{}", command);

    // split command with whitespace and explicitly type the collection
    let cmd_elements: Vec<&str> = command.split_whitespace().collect();

    // Parse command type, need to clone because we cannot return a reference
    let cmd_type = *COMMAND_TABLE.get(cmd_elements[0]).unwrap_or(&CmdType::CArithmetic);

    // Parse two arguments
    let arg1 = match cmd_type {
        CmdType::CArithmetic => cmd_elements.get(0).map(|s| s.to_string()),
        _ => cmd_elements.get(1).map(|s| s.to_string()),
    };

    let arg2 = if cmd_elements.len() == 3 {
        cmd_elements[2].parse().ok()
    } else {
        None
    };

    return (cmd_type, arg1, arg2);
}