package com.kingcjy.main.controller;

import com.kingcjy.main.repository.TestRepository;
import com.kingcjy.main.service.TestService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

//@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
public class MockTest {

    @Mock
    private TestRepository testRepository;

    //@InjectMocks  //@Mock들을 주입
    //private TestService testService;

    @Test
    public  void mockTest() throws  Exception{
        //given
        TestService testService = new TestService(testRepository);
        when(testRepository.mockTest()).thenReturn("mockValue");

        //when
        String value = testService.mockTest();

        //then
        assertEquals("mockValue",value);
        System.out.println(value);

    }

    @Nested
    class Child {
        @BeforeEach
        void beforeEach() {
            System.out.println("Child beforeEach");
        }
        @AfterEach
        void afterEach() {
            System.out.println("Child afterEach");
        }
        @Test void test() {
            System.out.println("Child Test");
        }
    }

}
