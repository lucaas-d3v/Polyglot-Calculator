use std::io::{self, BufRead, Write};

use crate::protocols::{ErrResponse, ErrorBody, OkResponse};

mod erros;
mod lexer;
mod protocols;
mod shuting;

fn main() {
    let stdin = io::stdin();
    let mut stdout = io::stdout();

    for line in stdin.lock().lines() {
        let line = match line {
            Ok(l) => l,
            Err(_) => break,
        };

        if line.trim().is_empty() {
            continue;
        }

        let req: Result<protocols::EvalRequest, _> = serde_json::from_str(&line);
        let req = match req {
            Ok(r) => r,
            Err(_) => continue,
        };

        let id = req.id;
        let op = req.op.as_str();

        match op {
            "shutdown" => {
                let resp = OkResponse {
                    id,
                    ok: true,
                    value: "bye".to_string(),
                };

                writeln!(stdout, "{}", serde_json::to_string(&resp).unwrap()).unwrap();
                stdout.flush().unwrap();

                break;
            }

            "eval" => {
                let expr = req.expr.unwrap_or_default();
                match shuting::eval_expr(&expr) {
                    Ok(v) => {
                        // devolve como string
                        let resp = OkResponse {
                            id,
                            ok: true,
                            value: v.to_string(),
                        };

                        writeln!(stdout, "{}", serde_json::to_string(&resp).unwrap()).unwrap();
                        stdout.flush().unwrap();
                    }

                    Err(e) => {
                        let resp = ErrResponse {
                            id,
                            ok: false,
                            error: ErrorBody {
                                code: e.code.to_string(),
                                message: e.to_string(),
                            },
                        };

                        writeln!(stdout, "{}", serde_json::to_string(&resp).unwrap()).unwrap();
                        stdout.flush().unwrap();
                    }
                }
            }

            _ => {
                let resp = ErrResponse {
                    id,
                    ok: false,
                    error: ErrorBody {
                        code: "UNKNOW_OP".into(),
                        message: format!("Operação não suportada: {}", op),
                    },
                };

                writeln!(stdout, "{}", serde_json::to_string(&resp).unwrap()).unwrap();
                stdout.flush().unwrap();
            }
        }
    }
}
