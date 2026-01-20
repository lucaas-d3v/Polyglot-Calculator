use std::io::{self, BufRead, Write};

use serde::{Deserialize, Serialize};
use serde_json::Value;

#[derive(Deserialize)]
struct EvalRequest {
    op: String,
    id: u64,
    expr: Option<String>,
}

fn main() {
    println!("Hello, world!");
}
