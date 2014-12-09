package com.sfpay.openapi.service;

import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.stereotype.Service;

@Service("clientDetailsService")
public class SfpayClientDetailsService extends InMemoryClientDetailsServiceBuilder{
}
