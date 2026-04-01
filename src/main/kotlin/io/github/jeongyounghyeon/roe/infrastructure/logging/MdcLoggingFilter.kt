package io.github.jeongyounghyeon.roe.infrastructure.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class MdcLoggingFilter : OncePerRequestFilter() {

    companion object {
        const val REQUEST_ID_HEADER = "X-Request-Id"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestId = request.getHeader(REQUEST_ID_HEADER) ?: UUID.randomUUID().toString()
        try {
            MDC.put("requestId", requestId)
            MDC.put("httpMethod", request.method)
            MDC.put("httpUri", request.requestURI)
            response.setHeader(REQUEST_ID_HEADER, requestId)
            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}