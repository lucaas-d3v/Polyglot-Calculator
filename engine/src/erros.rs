use std::fmt;

#[derive(Debug, Clone)]
pub struct CalcError {
    pub pos: usize,
    pub code: &'static str,
    pub message: String,
}

impl CalcError {
    pub fn new(pos: usize, code: &'static str, message: impl Into<String>) -> Self {
        Self {
            pos,
            code,
            message: message.into(),
        }
    }
}

impl fmt::Display for CalcError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        // formato curto
        write!(f, "{} at {}: {}", self.code, self.pos, self.message)
    }
}

impl std::error::Error for CalcError {}
