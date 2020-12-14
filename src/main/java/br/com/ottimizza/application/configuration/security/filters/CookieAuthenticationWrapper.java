package br.com.ottimizza.application.configuration.security.filters;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import br.com.ottimizza.application.model.httpsession.HttpSessionDetails;
import br.com.ottimizza.application.repositories.httpsession.HttpSessionRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
public class CookieAuthenticationWrapper {

    /**
     * Name of the cookie contaning the current user information.
     */
    public static final String NAME = "SSID";

    private static final String HMAC_SHA_512 = "HmacSHA512";

    /**
     * 
     */
    private static final String PATH = "/";
    private static final Pattern UID_PATTERN = Pattern.compile("ssid=([A-Za-z0-9(-?)]*)");
    private static final Pattern UID2_PATTERN = Pattern.compile("uid=([A-Za-z0-9]*)");
    private static final Pattern ROLES_PATTERN = Pattern.compile("roles=([A-Z0-9_|]*)");
    private static final Pattern COLOUR_PATTERN = Pattern.compile("colour=([A-Z]*)");
    private static final Pattern HMAC_PATTERN = Pattern.compile("hmac=([A-Za-z0-9+/=]*)");

    @Value("${auth.cookie.hmac-key}")
    private String cookieHmacKey;

    @Autowired
    HttpSessionRepository httpSessionDetailsRepository;

    public boolean isCookiePresent(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return false;
        }

        Optional<Cookie> maybeCookie = Stream.of(request.getCookies()).filter(c -> NAME.equals(c.getName()))
                .findFirst();

        return maybeCookie.isPresent();
    }

    public String getSSIDFromRequest(HttpServletRequest request) {
        Cookie cookie = Stream.of(request.getCookies()).filter(c -> NAME.equals(c.getName())).findFirst().orElse(null);

        if (cookie == null) {
            return null;
        }

        SSIDCookieParser ssidCookieParser = new SSIDCookieParser(cookie, cookieHmacKey);
        return ssidCookieParser.parse().getSsid();
    }

    public User getUserDetailsFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        Cookie cookie = Stream.of(cookies).filter(c -> NAME.equals(c.getName())).findFirst().orElse(null);

        if (cookie == null) {
            return null;
        }

        return this.getUserDetailsFromCookie(cookie);
    }

    public User getUserDetailsFromCookie(Cookie cookie) {
        SSIDCookieParser ssidCookieParser = new SSIDCookieParser(cookie, cookieHmacKey);
        SSIDCookieDetails ssidCookieDetails = ssidCookieParser.parse();

        UUID ssid = UUID.fromString(ssidCookieDetails.getSsid());

        HttpSessionDetails httpSessionDetails = httpSessionDetailsRepository.findById(ssid).orElse(null);

        if (httpSessionDetails == null) {
            return null;
        }

        return new User(httpSessionDetails.getPrincipal(), "", Arrays.asList());
    }

    public Optional<UsernamePasswordAuthenticationToken> readAuthenticationFromRequest(HttpServletRequest request) {
        User userinfo = this.getUserDetailsFromRequest(request);

        UsernamePasswordAuthenticationToken authentication = null;

        if (userinfo != null) {
            authentication = new UsernamePasswordAuthenticationToken(userinfo, "EMPTY_CREDENTIALS",
                    userinfo.getAuthorities());
        }
        return Optional.ofNullable(authentication);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    protected static class SSIDCookieDetails {

        private String ssid;

        @Override
        public String toString() {
            return "ssid=" + ssid;
        }

    }

    /**
     * 
     */
    protected static class SSIDCookieParser {

        private Cookie cookie;

        private String cookieHmacKey;

        private String hmac;

        public SSIDCookieParser(Cookie cookie, String cookieHmacKey) {
            this.cookie = cookie;
            this.cookieHmacKey = cookieHmacKey;
        }

        public SSIDCookieDetails parse() throws IllegalArgumentException {
            if (!NAME.equals(cookie.getName()))
                throw new IllegalArgumentException("No " + NAME + " Cookie");

            this.hmac = parse(cookie.getValue(), HMAC_PATTERN).orElse(null);

            String ssid = parse(cookie.getValue(), UID_PATTERN)
                    .orElseThrow(() -> new IllegalArgumentException(NAME + " Cookie contains no SSID"));

            SSIDCookieDetails ssidCookieDetails = new SSIDCookieDetails(ssid);

            if (!hmac.equals(calculateHmac(ssidCookieDetails, cookieHmacKey)))
                throw new IllegalArgumentException("Cookie signature (HMAC) invalid");

            return ssidCookieDetails;
        }

        private static Optional<String> parse(String value, Pattern pattern) {
            Matcher matcher = pattern.matcher(value);
            if (!matcher.find())
                return Optional.empty();

            if (matcher.groupCount() < 1)
                return Optional.empty();

            String match = matcher.group(1);
            if (match == null || match.trim().isEmpty())
                return Optional.empty();

            return Optional.of(match);
        }

        private String calculateHmac(SSIDCookieDetails ssidCookieDetails, String secretKey) {
            byte[] secretKeyBytes = Objects.requireNonNull(secretKey).getBytes(StandardCharsets.UTF_8);
            byte[] valueBytes = Objects.requireNonNull(ssidCookieDetails).toString().getBytes(StandardCharsets.UTF_8);

            try {
                Mac mac = Mac.getInstance(HMAC_SHA_512);
                SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyBytes, HMAC_SHA_512);
                mac.init(secretKeySpec);
                byte[] hmacBytes = mac.doFinal(valueBytes);
                return Base64.getEncoder().encodeToString(hmacBytes);

            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 
     */
    protected static class SSIDCookieSigner {

        public SSIDCookieSigner(SSIDCookieDetails cookieDetails, String cookieHmacKey) {

        }

    }

}
