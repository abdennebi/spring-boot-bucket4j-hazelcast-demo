package com.abddennebi.demo.filter;

import io.github.bucket4j.*;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.ProxyManager;
import io.github.bucket4j.grid.jcache.JCache;
import org.springframework.web.filter.GenericFilterBean;

import javax.cache.Cache;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * This example is taken from : https://github.com/vladimir-bukhtoyarov/bucket4j/blob/master/doc-pages/basic-usage.md
 */
public class IpThrottlingFilter extends GenericFilterBean {

    // cache for storing token buckets, where IP is key.

    private final Cache<String, GridBucketState> cache;

    private ProxyManager<String> buckets;


    public IpThrottlingFilter(Cache<String, GridBucketState> cache) {
        this.cache = cache;
        // init bucket registry
        buckets = Bucket4j.extension(JCache.class).proxyManagerForCache(this.cache);
    }


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;


        BucketConfiguration bucketConfiguration = Bucket4j.configurationBuilder()
                .addLimit(Bandwidth.simple(10, Duration.ofMinutes(1)))
                .buildConfiguration();

        // prepare configuration supplier which will be called(on first interaction with proxy) iff bucket was not saved yet previously. 
        Supplier<BucketConfiguration> configurationLazySupplier = () -> bucketConfiguration;

        // acquire cheap proxy to bucket, the real  
        Bucket bucket = buckets.getProxy(httpRequest.getRemoteAddr(), configurationLazySupplier);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // the limit is not exceeded
            httpResponse.setHeader("X-Rate-Limit-Remaining", "" + probe.getRemainingTokens());
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            // limit is exceeded
            httpResponse.setContentType("text/plain");
            httpResponse.setStatus(429);
            httpResponse.getWriter().append("Too many requests");
        }
    }

    private Supplier<BucketConfiguration> getConfigSupplier() {
        return () -> Bucket4j.configurationBuilder()
                .addLimit(Bandwidth.simple(10, Duration.ofMinutes(1)))
                .buildConfiguration();
    }


}