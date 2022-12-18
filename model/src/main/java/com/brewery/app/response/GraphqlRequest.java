package com.brewery.app.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GraphqlRequest {
    private String query;
    private Object variables;
}
