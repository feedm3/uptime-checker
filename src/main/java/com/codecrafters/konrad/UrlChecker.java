package com.codecrafters.konrad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used the check if a URL is up or down.
 *
 * @author Fabian Dietenberger
 */
@Component
public class UrlChecker {

    // to  make external requests
    private final RestTemplate restTemplate;

    // properties from the .properties file
    private final KonradProperties properties;

    @Autowired
    public UrlChecker(final RestTemplate restTemplate, final KonradProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    /**
     * Load urls from the .properties file, check them and return the result.
     *
     * @return map with url and status mapping. if status is true the url is ok
     */
    public Map<String, Boolean> checkUrlsFromProperties() {
        final Map<String, Boolean> urlOkResults = new HashMap<>();
        for (final String urlToCheck : properties.getUrls()) {
            boolean urlOk = isUrlOk(urlToCheck);
            urlOkResults.put(urlToCheck, urlOk);
        }
        return urlOkResults;
    }

    /**
     * Check a url if it is ok. The url is ok when it's reachable.
     *
     * @param url the url to check
     * @return true if the url is ok (reachable)
     */
    public boolean isUrlOk(final String url) {
        boolean urlOk = false;
        if (!StringUtils.isEmpty(url)) {
            final ResponseEntity responseEntity = requestUrl(url);
            if (isResponseOk(responseEntity)) {
                urlOk = true;
            }
        }
        return urlOk;
    }

    private ResponseEntity requestUrl(final String url) {
        ResponseEntity responseEntity = null;
        try {
            responseEntity = restTemplate.getForEntity(URI.create(url), null);
        } catch (Exception ignored) {
            // RestTemplate will throw an error if the request didn't work.
            // We don't need the information from this error, all information
            // is in the ResponseEntity object.
        }
        return responseEntity;
    }

    private boolean isResponseOk(final ResponseEntity responseEntity) {
        if (responseEntity == null) {
            return false;
        }
        final HttpStatus.Series series = responseEntity.getStatusCode().series();
        return !HttpStatus.Series.CLIENT_ERROR.equals(series)
                && !HttpStatus.Series.SERVER_ERROR.equals(series);
    }
}
