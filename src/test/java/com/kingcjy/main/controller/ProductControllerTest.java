package com.kingcjy.main.controller;

import com.google.gson.GsonBuilder;
import com.kingcjy.main.dto.ProductDto;
import com.kingcjy.main.repository.TestRepository;
import com.kingcjy.main.service.TestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.hypermedia.LinksSnippet;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
@SpringBootTest(classes = {
        TestRepository.class,
        TestService.class
    })
*/
@SpringBootTest
public class ProductControllerTest {
    //생성자에 String 형식으로 output directory를 지정할 수 있습니다. (기본값은 target/generated-snippets)
    @RegisterExtension
    final RestDocumentationExtension restDocumentation = new RestDocumentationExtension ("build/generated-snippets");

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private TestRepository testRepository;
    @Autowired
    private TestService testService;

    private MockMvc mockMvc;
    private RestDocumentationResultHandler document;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        //스니펫 경로를 {class-name}/{method-name}
        this.document = document(
                "{class-name}/{method-name}" //{className} no-formate  {step} 현재 테스트에서 서비스에 대한 호출 수
        );

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(restDocumentation)
                        .uris().withScheme("http").withHost("localhost").withPort(8080)//스니펫 파일에서 나오는 호스트를 변조해줍니다.
                        .and().snippets().withEncoding("UTF-8")//snippet encoding
                        .and().operationPreprocessors().withResponseDefaults(prettyPrint())//json 정렬
                        .and().operationPreprocessors().withRequestDefaults(prettyPrint()))//json 정렬
                //.and().snippets().withTemplateFormat(TemplateFormats.markdown()) 마크다운 지원
                .alwaysDo(document)
                .build();
    }

    @Test
    public void mockbeanTest() throws Exception {

        when(testRepository.mockTest()).thenReturn("mockbeanValue");

        String value = testService.mockTest();

        assertEquals("mockbeanValue",value);
        System.out.println(value);
    }

    @Test
    public void regist() throws Exception {

        ProductDto productDto = new ProductDto();
        productDto.setName("갤럭시 폴드");
        productDto.setDesc("삼성의 폴더블 스마트폰");
        productDto.setQuantity(10);

        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(productDto);

        mockMvc.perform(
                post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(//기본(6개) + @ 생성
                        requestFields(
                                fieldWithPath("name").description("상품 이름"),
                                fieldWithPath("desc").description("상품 설명"),
                                fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("상품 수량")
                        )
                ));
    }

    @Test
    public void search() throws Exception {
        FieldDescriptor[] testField = new FieldDescriptor[] {
                fieldWithPath("id").description("상품 아이디"),
                fieldWithPath("name").description("상품 이름"),
                fieldWithPath("desc").description("상품 설명"),
                fieldWithPath("quantity").type(Integer.class).description("상품 수량")
        };

        mockMvc.perform(
                get("/api/products/{id}", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("id").description("상품 id")
                        ),
                        responseFields(
                                //subsectionWithPath("contact").description("The user's contact details") 하위 섹션 문서화
                               /* fieldWithPath("id").description("상품 아이디"),
                                fieldWithPath("name").description("상품 이름"),
                                fieldWithPath("desc").description("상품 설명"),
                                fieldWithPath("quantity").type(Integer.class).description("상품 수량")*/
                                //beneathPath("0020.XX60",fieldWithPath("1").description("a"),fieldWithPath("2").description("b"))   부분표현
                                testField
                        ))
                )
                .andExpect(jsonPath("id", is(notNullValue())))
                .andExpect(jsonPath("name", is(notNullValue())))
                .andExpect(jsonPath("desc", is(notNullValue())))
                .andExpect(jsonPath("quantity", is(notNullValue())));
    }
    @Test
    public void searchAll() throws Exception {
        mockMvc.perform(
                get("/api/products")//(get("/api/products?page=1&size=10") 이것도 가능
                        .param("page", "1")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        requestParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지 사이즈")
                        ),
                        responseFields(
                                fieldWithPath("[].id").description("상품 아이디"),
                                fieldWithPath("[].name").type(JsonFieldType.STRING).description("상품 이름"),
                                fieldWithPath("[].desc").type(JsonFieldType.STRING).description("상품 설명"),
                                fieldWithPath("[].quantity").type(Integer.class).description("상품 수량")
                        )
                ))
                .andExpect(jsonPath("[0].id", is(notNullValue())))
                .andExpect(jsonPath("[0].name", is(notNullValue())))
                .andExpect(jsonPath("[0].desc", is(notNullValue())))
                .andExpect(jsonPath("[0].quantity", is(notNullValue())));

    }

    //@Test
    public void linksTest(LinkDescriptor description, LinkDescriptor linkDescriptor) throws Exception {
        LinksSnippet pagingLinks = links(
                linkWithRel("first").optional().description("The first page of results"),
                linkWithRel("last").optional().description("The last page of results"),
                linkWithRel("next").optional().description("The next page of results"),
                linkWithRel("prev").optional().description("The previous page of results"));

        this.mockMvc.perform(get("/").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("example", pagingLinks.and(
                        linkWithRel("alpha").description("Link to the alpha resource"),
                        linkWithRel("bravo").description("Link to the bravo resource"))));
    }

    //@Test
    public void upload1() throws Exception {
        this.mockMvc.perform(multipart("/upload").file("file", "example".getBytes()))
                .andExpect(status().isOk())
                .andDo(document.document(
                        requestParts(
                                partWithName("file").description("The file to upload")
                        )
                ));
    }

    //@Test
    public void upload2() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "image.png", "image/png",
                "<<png data>>".getBytes());
        MockMultipartFile metadata = new MockMultipartFile("metadata", "",
                "application/json", "{ \"version\": \"1.0\"}".getBytes());

        this.mockMvc.perform(fileUpload("/images").file(image).file(metadata)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document.document(
                        requestPartFields("metadata",
                                fieldWithPath("version").description("The version of the image"))));
    }

    //@Test
    public void header() throws Exception {
        this.mockMvc
                .perform(get("/people").header("Authorization", "Basic dXNlcjpzZWNyZXQ="))
                .andExpect(status().isOk())
                .andDo(document("headers",
                        requestHeaders(
                                headerWithName("Authorization").description(
                                        "Basic auth credentials")),
                        responseHeaders(
                                headerWithName("X-RateLimit-Limit").description(
                                        "The total number of requests permitted per period"),
                                headerWithName("X-RateLimit-Remaining").description(
                                        "Remaining requests permitted in current period"),
                                headerWithName("X-RateLimit-Reset").description(
                                        "Time at which the rate limit period will reset"))));
    }

    private static class ConstrainedFields {

        private final ConstraintDescriptions constraintDescriptions;

        ConstrainedFields(Class<?> input) {
            this.constraintDescriptions = new ConstraintDescriptions(input);
        }

        private FieldDescriptor withPath(String path) {
            return fieldWithPath(path).attributes(key("constraints").value(StringUtils
                    .collectionToDelimitedString(this.constraintDescriptions
                            .descriptionsForProperty(path), ". ")));
        }
    }
}