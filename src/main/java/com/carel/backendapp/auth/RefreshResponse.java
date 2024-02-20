package com.carel.backendapp.auth;

import lombok.Builder;

@Builder
public record RefreshResponse(String accessToken) {
}
