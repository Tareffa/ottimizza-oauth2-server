package br.com.ottimizza.application.configuration.security.cookie;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

public class SSIDCookie extends Cookie {

    /**
     * Name of the cookie contaning the current user information.
     */
    public static final String NAME = "SSID";

    private static final String PATH = "/";
    private static final Pattern UID_PATTERN = Pattern.compile("ssid=([A-Za-z0-9(-?)]*)");
    private static final Pattern UID2_PATTERN = Pattern.compile("uid=([A-Za-z0-9]*)");
    private static final Pattern ROLES_PATTERN = Pattern.compile("roles=([A-Z0-9_|]*)");
    private static final Pattern COLOUR_PATTERN = Pattern.compile("colour=([A-Z]*)");
    private static final Pattern HMAC_PATTERN = Pattern.compile("hmac=([A-Za-z0-9+/=]*)");
    private static final String HMAC_SHA_512 = "HmacSHA512";

    private final Payload payload;
    private final String hmac;

    public SSIDCookie(String ssid, User userInfo, String cookieHmacKey) {
        super(NAME, "");
        this.payload = new Payload(ssid, userInfo.getUsername(),
                userInfo.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()),
                "");
        this.hmac = calculateHmac(this.payload, cookieHmacKey);
        this.setPath(PATH);
        this.setMaxAge((int) Duration.of(1, ChronoUnit.HOURS).toSeconds());
        this.setHttpOnly(true);
    }

    public SSIDCookie(User userInfo, String cookieHmacKey) {
        super(NAME, "");
        this.payload = new Payload(userInfo.getUsername(),
                userInfo.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()),
                "");
        this.hmac = calculateHmac(this.payload, cookieHmacKey);
        this.setPath(PATH);
        this.setMaxAge((int) Duration.of(1, ChronoUnit.HOURS).toSeconds());
        this.setHttpOnly(true);
    }

    public SSIDCookie(Cookie cookie, String cookieHmacKey) {
        super(NAME, "");

        if (!NAME.equals(cookie.getName()))
            throw new IllegalArgumentException("No " + NAME + " Cookie");

        this.hmac = parse(cookie.getValue(), HMAC_PATTERN).orElse(null);
        if (hmac == null)
            // throw new CookieVerificationFailedException("Cookie not signed (no HMAC)");
            throw new IllegalArgumentException("Cookie not signed (no HMAC)");

        String ssid = parse(cookie.getValue(), UID_PATTERN)
                .orElseThrow(() -> new IllegalArgumentException(NAME + " Cookie contains no UID"));
        // String username = parse(cookie.getValue(), UID2_PATTERN)
        //         .orElseThrow(() -> new IllegalArgumentException(NAME + " Cookie contains no UID"));
        // List<String> roles = parse(cookie.getValue(), ROLES_PATTERN).map(s -> List.of(s.split("\\|")))
        //         .orElse(List.of());
        String colour = parse(cookie.getValue(), COLOUR_PATTERN).orElse(null);
        this.payload = new Payload(ssid, "username", Arrays.asList(), "");

        System.out.println("cookieHmacKey" + cookieHmacKey);
        if (!hmac.equals(calculateHmac(payload, cookieHmacKey)))
            // throw new CookieVerificationFailedException("Cookie signature (HMAC)
            // invalid");
            throw new IllegalArgumentException("Cookie signature (HMAC) invalid");

        this.setPath(cookie.getPath());
        this.setMaxAge(cookie.getMaxAge());
        this.setHttpOnly(cookie.isHttpOnly());
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

    @Override
    public String getValue() {
        return payload.toString() + "&hmac=" + hmac;
    }

    public String getSSID() {
        return payload.getSSID();
    }

    public User getUserInfo() {
        return new User(payload.username, "",
                payload.roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet()));
    }

    private String calculateHmac(Payload payload, String secretKey) {
        byte[] secretKeyBytes = Objects.requireNonNull(secretKey).getBytes(StandardCharsets.UTF_8);
        byte[] valueBytes = Objects.requireNonNull(payload).toString().getBytes(StandardCharsets.UTF_8);

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

    private static class Payload {
        private final String ssid;
        private final String username;
        private final List<String> roles;
        private final String colour;

        private Payload(String username, List<String> roles, String colour) {
            this.username = username;
            this.roles = roles;
            this.colour = colour;
            this.ssid = "";
        }

        private Payload(String ssid, String username, List<String> roles, String colour) {
            this.username = username;
            this.roles = roles;
            this.colour = colour;
            this.ssid = ssid;
        }

        public String getSSID() {
            return this.ssid;
        }

        @Override
        public String toString() {
            return "ssid=" + ssid;
            // return "ssid=" + ssid + "&uid=" + username + "&roles=" + String.join("|",
            // roles)
            // + (colour != null ? "&colour=" + colour : "");
        }
    }
}