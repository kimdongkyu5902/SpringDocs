package com.kingcjy.main.service;

import com.kingcjy.main.repository.TestRepository;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    private final TestRepository testRepository;

    public TestService(TestRepository testRepository){
        this.testRepository = testRepository;
    }

    public String mockTest() {
        return testRepository.mockTest();
    }
}
