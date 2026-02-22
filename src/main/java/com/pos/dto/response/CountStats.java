package com.pos.dto.response;

/** Lightweight stats record returned by resources that only expose a total count. */
public record CountStats(long total) {}
