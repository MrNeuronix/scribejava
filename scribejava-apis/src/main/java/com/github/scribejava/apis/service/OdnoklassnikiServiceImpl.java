package com.github.scribejava.apis.service;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthConfig;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Parameter;
import com.github.scribejava.core.model.ParameterList;
import com.github.scribejava.core.oauth.OAuth20Service;

import org.apache.commons.codec.CharEncoding;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

public class OdnoklassnikiServiceImpl extends OAuth20Service {

    public OdnoklassnikiServiceImpl(DefaultApi20 api, OAuthConfig config) {
        super(api, config);
    }

    @Override
    public void signRequest(OAuth2AccessToken accessToken, OAuthRequest request) {
        //sig = lower(md5( sorted_request_params_composed_string + md5(access_token + application_secret_key)))
        try {
            final String tokenDigest = md5Hex(accessToken.getAccessToken() + getConfig().getApiSecret());

            ParameterList queryParams = request.getQueryStringParams();
            ParameterList bodyParams = request.getBodyParams();
            queryParams.addAll(bodyParams);
            Collections.sort(queryParams.getParams());

            List<String> params = new ArrayList<>();
            for(Parameter param : queryParams.getParams()) {
                params.add(param.getKey().concat("=").concat(param.getValue()));
            }

            final StringBuilder builder = new StringBuilder();
            for (String param : params) {
                builder.append(param);
            }

            final String sigSource = URLDecoder.decode(builder.toString(), CharEncoding.UTF_8) + tokenDigest;
            request.addQuerystringParameter("sig", md5Hex(sigSource).toLowerCase());

            super.signRequest(accessToken, request);
        } catch (UnsupportedEncodingException unex) {
            throw new IllegalStateException(unex);
        }
    }
}
