package com.brewery.app.response;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GraphqlResponse<T> {
    private Data<T> data;

    private List<Object> errors = new ArrayList<>();

    @Getter
    public class Data<T> {
        private T data;
    }
}
