package nextstep.jwp.model;

import static nextstep.jwp.exception.ExceptionType.INVALID_HTTP_LOGIN_EXCEPTION;
import static nextstep.jwp.exception.ExceptionType.INVALID_HTTP_REGISTER_EXCEPTION;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import nextstep.jwp.db.InMemoryUserRepository;
import nextstep.jwp.exception.InvalidHttpRequestException;
import org.apache.coyote.http11.common.SessionManager;
import org.apache.coyote.http11.request.HttpRequest;
import org.apache.coyote.http11.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

    private static final UserService INSTANCE = new UserService();

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final String SUCCEED_REDIRECT_URL = "/index.html";
    private static final String FAILED_REDIRECT_URL = "/401.html";
    private static final String ID = "account";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "email";
    private static Long autoIncrementCount = 1L;

    private UserService() {
    }

    public boolean findUserBySession(HttpRequest request) {
        return SessionManager.hasSession(request.getCookie());
    }

    public void login(final HttpRequest httpRequest, final HttpResponse httpResponse)
            throws IOException, URISyntaxException {

        final Map<String, String> params = httpRequest.getParams();

        validateLoginParams(params);
        final Optional<User> optionalUser = InMemoryUserRepository.findByAccount(params.get(ID));
        if (optionalUser.isPresent()) {
            final User user = findUser(params, optionalUser);
            httpResponse.setSessionAndCookieWithOkResponse(user, SUCCEED_REDIRECT_URL);
            return;
        }
        httpResponse.setFoundResponse(FAILED_REDIRECT_URL);
    }

    private void validateLoginParams(final Map<String, String> params) {
        if (params.isEmpty()) {
            throw new InvalidHttpRequestException(INVALID_HTTP_LOGIN_EXCEPTION);
        }
    }

    private User findUser(Map<String, String> params, Optional<User> optionalUser) {
        User user = optionalUser.get();
        if (user.checkPassword(params.get(PASSWORD))) {
            loggingInfoUser(params, user);
            return user;
        }
        return null;
    }

    public User register(Map<String, String> params) {
        validateResister(params);
        User user = getUser(params);
        InMemoryUserRepository.save(user);
        return user;
    }

    private void validateResister(final Map<String, String> params) {
        if (isValidRegister(params)) {
            throw new InvalidHttpRequestException(INVALID_HTTP_REGISTER_EXCEPTION);
        }
    }

    private boolean isValidRegister(final Map<String, String> params) {
        return params.isEmpty() || !params.containsKey(ID) || !params.containsKey(PASSWORD) || !params.containsKey(
                EMAIL);
    }

    private User getUser(Map<String, String> params) {
        return new User(++autoIncrementCount, params.get(ID), params.get(PASSWORD), params.get(EMAIL));
    }

    private void loggingInfoUser(Map<String, String> params, User user) {
        if (user.checkPassword(params.get(PASSWORD))) {
            log.info(" 로그인 성공! 아이디 : " + user.getAccount());
        }
    }

    public static UserService getInstance() {
        return INSTANCE;
    }
}