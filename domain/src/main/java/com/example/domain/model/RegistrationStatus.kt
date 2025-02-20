package com.example.domain.model

enum class RegistrationStatus(id: Int) {
    None(0),
    Progress(2),
    Ok(3),
    Cleared(4),
    Failed(5),
    Refreshing(6)
}