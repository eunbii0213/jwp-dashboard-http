package nextstep.jwp.http;

import nextstep.jwp.exception.NoMatchingControllerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestHandlerTest {
    private final String protocol = "HTTP/1.1";

    @DisplayName("index 페이지를 요청하면 index 페이지와 함께 올바른 형식으로 응답을 반환한다")
    @Test
    void index() throws IOException {
        // given
        final String httpRequest= String.join("\r\n",
                "GET /index.html HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");

        final MockSocket socket = new MockSocket(httpRequest);
        final RequestHandler requestHandler = new RequestHandler(socket);

        // when
        requestHandler.run();

        // then
        final URL resource = getClass().getClassLoader().getResource("static/index.html");
        final String responseBody = new String(Files.readAllBytes(new File(resource.getFile()).toPath()));
        final HttpResponse expected = new HttpResponse(
                protocol,
                HttpStatus.OK,
                ContentType.HTML,
                responseBody.getBytes().length,
                responseBody
        );

        assertThat(socket.output()).isEqualTo(expected.toResponseMessage());
    }

    @DisplayName("존재하지 않는 페이지를 요청하면 404 페이지를 반환한다")
    @Test
    void not_found() throws IOException {
        // given
        final String httpRequest= String.join("\r\n",
                "GET /sakjung HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");

        final MockSocket socket = new MockSocket(httpRequest);
        final RequestHandler requestHandler = new RequestHandler(socket);

        // when
        requestHandler.run();

        // then
        final URL resource = getClass().getClassLoader().getResource("static/404.html");
        final String responseBody = new String(Files.readAllBytes(new File(resource.getFile()).toPath()));
        final HttpResponse expected = new HttpResponse(
                protocol,
                HttpStatus.NOT_FOUND,
                ContentType.HTML,
                responseBody.getBytes().length,
                responseBody
        );

        assertThat(socket.output()).isEqualTo(expected.toResponseMessage());
    }

    @DisplayName("로직을 수행하다 에러가 나면 500 페이지를 반환한다")
    @Test
    void internal_server_error() throws IOException {
        // given
        final String httpRequest= String.join("\r\n",
                "POST /register HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "account=gugu&password=password&email=hkkang%40woowahan.com");

        final MockSocket socket = new MockSocket(httpRequest);
        final RequestHandler requestHandler = new RequestHandler(socket);

        // when
        requestHandler.run();

        // then
        final URL resource = getClass().getClassLoader().getResource("static/500.html");
        final String responseBody = new String(Files.readAllBytes(new File(resource.getFile()).toPath()));
        final HttpResponse expected = new HttpResponse(
                protocol,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ContentType.HTML,
                responseBody.getBytes().length,
                responseBody
        );

        assertThat(socket.output()).isEqualTo(expected.toResponseMessage());
    }

    @DisplayName("login post 요청이 왔을 때, 요청을 처리한 후 index.html페이지로 리다이렉트 한다")
    @Test
    void login_post() throws IOException {
        // given
        final String requestBody = "account=gugu&password=password";
        final String httpRequest= String.join("\r\n",
                "POST /login HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "Content-Length: " + requestBody.getBytes().length,
                "Content-Type: application/x-www-form-urlencoded",
                "Accept: */*",
                "",
                requestBody);

        final MockSocket socket = new MockSocket(httpRequest);
        final RequestHandler requestHandler = new RequestHandler(socket);

        // when
        requestHandler.run();

        // then
        final HttpResponse expected = new HttpResponse(
                protocol,
                HttpStatus.FOUND,
                "/index.html"
        );

        assertThat(socket.output()).isEqualTo(expected.toResponseMessage());
    }

    @DisplayName("register post 요청이 왔을 때, 요청을 처리한 후 index.html페이지로 리다이렉트 한다")
    @Test
    void register_post() throws IOException {
        // given
        final String requestBody = "account=sakjung&password=password&email=sakjung%40sakjung.com";
        final String httpRequest = String.join("\r\n",
                "POST /register HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "Content-Length: " + requestBody.getBytes().length,
                "Content-Type: application/x-www-form-urlencoded",
                "Accept: */*",
                "",
                requestBody);

        final MockSocket socket = new MockSocket(httpRequest);
        final RequestHandler requestHandler = new RequestHandler(socket);

        // when
        requestHandler.run();

        // then
        final HttpResponse expected = new HttpResponse(
                protocol,
                HttpStatus.SEE_OTHER,
                "/index.html"
        );

        assertThat(socket.output()).isEqualTo(expected.toResponseMessage());
    }

    @DisplayName("요청을 처리할 수 없는 handler가 없을 때, 에러를 반환한다")
    @Test
    void no_matching_controller_exception() {
        final String httpRequest= String.join("\r\n",
                "PUT /sakjung HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");

        final MockSocket socket = new MockSocket(httpRequest);
        final RequestHandler requestHandler = new RequestHandler(socket);

        assertThatThrownBy(requestHandler::run)
                .isInstanceOf(NoMatchingControllerException.class);
    }
}