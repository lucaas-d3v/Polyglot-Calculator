use std::{
    io::{self, BufRead, Write},
    str,
};

use serde::{Deserialize, Serialize};
use serde_json::Value;

#[derive(Deserialize)]
struct EvalRequest {
    op: String,
    id: u64,
    expr: Option<String>,
}

#[derive(Serialize)]
struct OkResponse {
    id: u64,
    ok: bool,
    value: String,
}

#[derive(Serialize)]
struct ErrorBody {
    code: String,
    message: String,
}

#[derive(Serialize)]
struct ErrResponse {
    id: u64,
    ok: bool,
    error: ErrorBody,
}

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

        let parsed: Result<Value, _> = serde_json::from_str(&line);
        if parsed.is_err() {
            continue;
        }

        let v = parsed.unwrap();
        let op = v["op"].as_str().unwrap_or("");
        let id = v["id"].as_u64().unwrap_or(0);

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
                let expr = v["expr"].as_str().unwrap_or("");
                let resp = OkResponse {
                    id,
                    ok: true,
                    value: format!("echo: {}", expr),
                };

                writeln!(stdout, "{}", serde_json::to_string(&resp).unwrap()).unwrap();
                stdout.flush().unwrap();
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
