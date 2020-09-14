package com.kingcjy.main.controller;

import com.kingcjy.main.dto.ProductDto;
import com.kingcjy.main.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final TestService testService;

    @Autowired
    public ProductController(TestService testService){
        this.testService = testService;
    }

    @PostMapping("/mockTest")
    public ResponseEntity<?> mock() {
        String mockStr = testService.mockTest();
        return new ResponseEntity<>(mockStr,HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<?> postProduct(@RequestBody ProductDto productDto) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<?> getAllProducts(@RequestParam String page,
                                            @RequestParam String size) {
        ProductDto productDto = new ProductDto();
        productDto.setId(1);
        productDto.setName("Spring In Action");
        productDto.setDesc("Spring");
        productDto.setQuantity(10);

        List result = new ArrayList<>();
        result.add(productDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Integer id) {
        ProductDto productDto = new ProductDto();
        productDto.setId(id);
        productDto.setName("Spring In Action");
        productDto.setDesc("Spring");
        productDto.setQuantity(10);
        return new ResponseEntity<>(productDto, HttpStatus.OK);
    }

    @GetMapping("/test")
    public ResponseEntity<?> test(ProductDto productDto) {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}