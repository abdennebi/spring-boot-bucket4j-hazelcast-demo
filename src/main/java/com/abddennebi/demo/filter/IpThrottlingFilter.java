package com.abddennebi.demo.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.ProxyManager;
import io.github.bucket4j.grid.jcache.JCache;
import org.springframework.beans.factory.annotation.Autowired;
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


        BucketConfiguration bucketConfiguration = Bucket4j.configurationBuilder()
                .addLimit(Bandwidth.simple(10, Duration.ofMinutes(1)))
                .buildConfiguration();

        // prepare configuration supplier which will be called(on first interaction with proxy) iff bucket was not saved yet previously. 
        Supplier<BucketConfiguration> configurationLazySupplier = () -> bucketConfiguration;

        // acquire cheap proxy to bucket, the real  
        Bucket bucket = buckets.getProxy(httpRequest.getRemoteAddr(), configurationLazySupplier);

        // tryConsume returns false immediately if no tokens available with the bucket
        if (bucket.tryConsume(1)) {
            // the limit is not exceeded
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            // limit is exceeded
            HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
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