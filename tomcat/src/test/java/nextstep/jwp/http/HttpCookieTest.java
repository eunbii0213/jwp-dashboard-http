package nextstep.jwp.http;

import nextstep.jwp.exception.InvalidException;
import org.apache.coyote.HttpCookie;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpCookieTest {

    @Test
    void 쿠키_정보가_들어있는_string을_map으로_변환한다() {
        // given
        final String rawCookie = "yummy_cookie=choco; tasty_cookie=strawberry; JSESSIONID=656cef62-e3c4-40bc-a8df-94732920ed46";
        final HttpCookie expected = HttpCookie.create();
        expected.put("yummy_cookie", "choco");
        expected.put("tasty_cookie", "strawberry");
        expected.put("JSESSIONID", "656cef62-e3c4-40bc-a8df-94732920ed46");
        // when
        final HttpCookie actual = HttpCookie.create(rawCookie);

        // then
        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void 쿠키_정보를_String으로_변환한다() {
        // given
        final String expected = "yummy_cookie=choco; tasty_cookie=strawberry; JSESSIONID=656cef62-e3c4-40bc-a8df-94732920ed46";

        final HttpCookie given = HttpCookie.create();
        given.put("yummy_cookie", "choco");
        given.put("tasty_cookie", "strawberry");
        given.put("JSESSIONID", "656cef62-e3c4-40bc-a8df-94732920ed46");

        // when, then
        assertThat(given.parse()).isEqualTo(expected);
    }

    @Test
    void 객체를_생성할_때_쿠키_정보가_올바르지_않으면_예외를_발생한다() {
        // given
        final String expected = "yummy_cookie=";

        // when, then
        assertThatThrownBy(() -> HttpCookie.create(expected))
                .isInstanceOf(InvalidException.class);
    }
}