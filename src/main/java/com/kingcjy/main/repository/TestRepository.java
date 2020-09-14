package com.kingcjy.main.repository;

import org.springframework.stereotype.Repository;

@Repository
public class TestRepository {
    public String mockTest() {
        return "realValue";
    }
}
