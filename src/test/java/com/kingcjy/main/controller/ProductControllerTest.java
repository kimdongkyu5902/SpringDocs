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
    //???????????? String ???????????? output directory??? ????????? ??? ????????????. (???????????? target/generated-snippets)
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
        //????????? ????????? {class-name}/{method-name}
        this.document = document(
                "{class-name}/{method-name}" //{className} no-formate  {step} ?????? ??????????????? ???????????? ?????? ?????? ???
        );

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(restDocumentation)
                        .uris().withScheme("http").withHost("localhost").withPort(8080)//????????? ???????????? ????????? ???????????? ??????????????????.
                        .and().snippets().withEncoding("UTF-8")//snippet encoding
                        .and().operationPreprocessors().withResponseDefaults(prettyPrint())//json ??????
                        .and().operationPreprocessors().withRequestDefaults(prettyPrint()))//json ??????
                //.and().snippets().withTemplateFormat(TemplateFormats.markdown()) ???????????? ??????
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
        productDto.setName("????????? ??????");
        productDto.setDesc("????????? ????????? ????????????");
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
                .andDo(document.document(//??????(6???) + @ ??????
                        requestFields(
                                fieldWithPath("name").description("?????? ??????"),
                                fieldWithPath("desc").description("?????? ??????"),
                                fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("?????? ??????")
                        )
                ));
    }

    @Test
    public void search() throws Exception {
        FieldDescriptor[] testField = new FieldDescriptor[] {
                fieldWithPath("id").description("?????? ?????????"),
                fieldWithPath("name").description("?????? ??????"),
                fieldWithPath("desc").description("?????? ??????"),
                fieldWithPath("quantity").type(Integer.class).description("?????? ??????")
        };

        mockMvc.perform(
                get("/api/products/{id}", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("id").description("?????? id")
                        ),
                        responseFields(
                                //subsectionWithPath("contact").description("The user's contact details") ?????? ?????? ?????????
                               /* fieldWithPath("id").description("?????? ?????????"),
                                fieldWithPath("name").description("?????? ??????"),
                                fieldWithPath("desc").description("?????? ??????"),
                                fieldWithPath("quantity").type(Integer.class).description("?????? ??????")*/
                                //beneathPath("0020.XX60",fieldWithPath("1").description("a"),fieldWithPath("2").description("b"))   ????????????
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
                get("/api/products")//(get("/api/products?page=1&size=10") ????????? ??????
                        .param("page", "1")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        requestParameters(
                                parameterWithName("page").description("????????? ??????"),
                                parameterWithName("size").description("????????? ?????????")
                        ),
                        responseFields(
                                fieldWithPath("[].id").description("?????? ?????????"),
                                fieldWithPath("[].name").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("[].desc").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("[].quantity").type(Integer.class).description("?????? ??????")
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