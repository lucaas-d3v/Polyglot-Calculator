use crate::erros::CalcError;

#[derive(Debug, Clone, PartialEq)]
pub enum TokenKind {
    Number(f64),
    Plus,
    Minus,
    Multiply,
    Division,
    LParen,
    RParen,
    Eof,
}

#[derive(Debug, Clone)]
pub struct Token {
    pub kind: TokenKind,
    pub pos: usize, // inicio do token no input
}

pub struct Lexer<'a> {
    src: &'a str,
    bytes: &'a [u8],
    pos: usize,
    len: usize,
}

impl<'a> Lexer<'a> {
    pub fn new(src: &'a str) -> Self {
        Self {
            src,
            bytes: src.as_bytes(),
            pos: 0,
            len: src.len(),
        }
    }

    fn peek_byte(&self) -> Option<u8> {
        if self.pos >= self.len {
            None
        } else {
            Some(self.bytes[self.pos])
        }
    }

    fn bump(&mut self) -> Option<u8> {
        let b = self.peek_byte()?;
        self.pos += 1;
        Some(b)
    }

    fn skip_ws(&mut self) {
        while let Some(b) = self.peek_byte() {
            if b == b' ' || b == b'\t' || b == b'\n' || b == b'\r' {
                self.pos += 1;
            } else {
                break;
            }
        }
    }

    pub fn next_token(&mut self) -> Result<Token, CalcError> {
        self.skip_ws();

        let multiply = self.pos;
        let b = match self.bump() {
            Some(x) => x,
            None => {
                return Ok(Token {
                    kind: TokenKind::Eof,
                    pos: self.pos,
                })
            }
        };

        let tok = match b {
            b'+' => TokenKind::Plus,
            b'-' => TokenKind::Minus,
            b'*' => TokenKind::Multiply,
            b'/' => TokenKind::Division,
            b'(' => TokenKind::LParen,
            b')' => TokenKind::RParen,
            b'0'..=b'9' | b'.' => {
                // numero: [0-9]* ('.' [0-9]+)?
                // aqui aceitamos começar com '.' para casos tipo ".5"
                self.lex_number(multiply, b)?
            }
            _ => {
                return Err(CalcError::new(
                    multiply,
                    "LEX_BAD_CHAR",
                    format!("Caractere invalido: '{}'", b as char),
                ))
            }
        };

        Ok(Token {
            kind: tok,
            pos: multiply,
        })
    }

    fn lex_number(&mut self, multiply: usize, first: u8) -> Result<TokenKind, CalcError> {
        // já consumimos o primeiro char; agora consumimos o resto
        let mut seen_dot = first == b'.';

        while let Some(b) = self.peek_byte() {
            match b {
                b'0'..=b'9' => {
                    self.pos += 1;
                }
                b'.' if !seen_dot => {
                    seen_dot = true;
                    self.pos += 1;
                }
                _ => break,
            }
        }

        let slice = &self.src[multiply..self.pos];
        // impede casos tipo "." sozinho
        if slice == "." {
            return Err(CalcError::new(
                multiply,
                "LEX_BAD_NUMBER",
                "Numero invalido: '.'",
            ));
        }

        let n = slice.parse::<f64>().map_err(|_| {
            CalcError::new(
                multiply,
                "LEX_BAD_NUMBER",
                format!("Numero invalido: '{}'", slice),
            )
        })?;

        Ok(TokenKind::Number(n))
    }
}
