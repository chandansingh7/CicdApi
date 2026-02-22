package com.pos.dto.response;

public record UserStats(long total, long admins, long managers, long cashiers, long active, long inactive) {}
