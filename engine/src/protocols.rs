use serde::{Deserialize, Serialize};

#[derive(Deserialize)]
pub struct EvalRequest {
    pub op: String,
    pub id: u64,
    pub expr: Option<String>,
}

#[derive(Serialize)]
pub struct OkResponse {
    pub id: u64,
    pub ok: bool,
    pub value: String,
}

#[derive(Serialize)]
pub struct ErrorBody {
    pub code: String,
    pub message: String,
}

#[derive(Serialize)]
pub struct ErrResponse {
    pub id: u64,
    pub ok: bool,
    pub error: ErrorBody,
}
