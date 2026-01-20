use crate::erros::CalcError;
use crate::lexer::{Lexer, TokenKind};

fn precedence(op: &TokenKind) -> i32 {
    match op {
        TokenKind::Plus | TokenKind::Minus => 1,
        TokenKind::Division | TokenKind::Multiply => 2,

        _ => 0,
    }
}

fn is_operator(t: &TokenKind) -> bool {
    return matches!(
        t,
        TokenKind::Plus | TokenKind::Minus | TokenKind::Multiply | TokenKind::Division
    );
}

fn apply_op(op: TokenKind, a: f64, b: f64, pos: usize) -> Result<f64, CalcError> {
    match op {
        TokenKind::Plus => Ok(a + b),
        TokenKind::Minus => Ok(a - b),
        TokenKind::Multiply => Ok(a * b),
        TokenKind::Division => {
            if b == 0.0 {
                Err(CalcError::new(pos, "EVAL_DIV_ZERO", "Divisão por 0"))
            } else {
                Ok(a / b)
            }
        }

        _ => Err(CalcError::new(pos, "EVAL_BAD_OP", "Operador Inválido")),
    }
}

pub fn eval_expr(src: &str) -> Result<f64, CalcError> {
    let mut lex = Lexer::new(src);

    let mut values: Vec<f64> = Vec::new();
    let mut ops: Vec<(TokenKind, usize)> = Vec::new();

    loop {
        let tok = lex.next_token()?;

        match tok.kind {
            TokenKind::Number(n) => {
                values.push(n);
            }

            TokenKind::LParen => {
                ops.push((TokenKind::LParen, tok.pos));
            }

            TokenKind::RParen => {
                let mut found = false;
                while let Some((top, top_pos)) = ops.pop() {
                    if top == TokenKind::LParen {
                        found = true;
                        break;
                    }
                    reduce_once(&mut values, top, top_pos)?;
                }
                if !found {
                    return Err(CalcError::new(
                        tok.pos,
                        "PARSE_MISMATCH_PAREN",
                        "Faltou '('",
                    ));
                }
            }

            TokenKind::Plus | TokenKind::Minus | TokenKind::Multiply | TokenKind::Division => {
                while let Some((top, top_pos)) = ops.last().cloned() {
                    if is_operator(&top) && precedence(&top) >= precedence(&tok.kind) {
                        ops.pop();

                        reduce_once(&mut values, top, top_pos)?;
                        continue;
                    }

                    break;
                }

                ops.push((tok.kind, tok.pos));
            }

            TokenKind::Eof => break,
        }
    }

    // reduz o restante
    while let Some((top, top_pos)) = ops.pop() {
        if top == TokenKind::LParen {
            return Err(CalcError::new(
                top_pos,
                "PARSE_MISMATCH_PAREN",
                "Faltou ')'",
            ));
        }

        reduce_once(&mut values, top, top_pos)?;
    }

    if values.len() != 1 {
        return Err(CalcError::new(0, "PARSE_BAD_EXPR", "Expressão incompleta"));
    }

    Ok(values[0])
}

fn reduce_once(values: &mut Vec<f64>, op: TokenKind, pos: usize) -> Result<(), CalcError> {
    if values.len() < 2 {
        return Err(CalcError::new(
            pos,
            "PARSE_MISSING_OPERAND",
            "Falrou operando",
        ));
    }

    let b = values.pop().unwrap();
    let a = values.pop().unwrap();
    let r = apply_op(op, a, b, pos)?;

    values.push(r);
    Ok(())
}
