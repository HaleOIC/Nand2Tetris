#[derive(Debug, PartialEq, Eq, Hash, Copy, Clone)]
pub enum CmdType {
    CPush,
    CPop,
    CArithmetic,
    CLabel,
    CGoto,
    CIf,
    CFunction,
    CCall,
    CReturn,
}