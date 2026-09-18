package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.user.dto.NewUserDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UserClientTest {

    private UserClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        this.client = new UserClient("http://localhost:9090", builder);
        RestTemplate rest = (RestTemplate) org.springframework.test.util.ReflectionTestUtils
                .getField(this.client, "rest");
        this.server = MockRestServiceServer.createServer(rest);
    }

    @Test
    void create_sendsPostWithBody() {
        this.server.expect(requestTo("http://localhost:9090/users"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"name\":\"Иван\",\"email\":\"ivan@example.com\"}"))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        NewUserDto request = new NewUserDto();
        request.setName("Иван");
        request.setEmail("ivan@example.com");

        assertThat(this.client.create(request).getStatusCode().is2xxSuccessful()).isTrue();
        this.server.verify();
    }

    @Test
    void getById_sendsGetToUserPath() {
        this.server.expect(requestTo("http://localhost:9090/users/7"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":7}", MediaType.APPLICATION_JSON));

        assertThat(this.client.getById(7L).getStatusCode().is2xxSuccessful()).isTrue();
        this.server.verify();
    }
}
