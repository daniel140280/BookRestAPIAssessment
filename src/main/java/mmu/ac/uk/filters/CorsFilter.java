package mmu.ac.uk.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CORS filter that allows the Vite dev server (localhost:5173) to call
 * the REST API (localhost:8080) during local development.
 */

/**
 * The Cross-Origin Resource Sharing (CORS) servlet filter enables the front-end development server.
 * Put simply, this filter allows the Vite dev server running on http://localhost:5173 to access
 * the REST API running on http://localhost:8080. 
 * The browsers then enforce Same-Origin Policy that blocks cross-origin requests unless the server explicitly permits them.
 *
 * This filter is intended for development use only. In production, CORS rules should be restricted to trusted origins.
 */

@WebFilter("/*")
public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request   = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        response.setHeader("Access-Control-Allow-Origin",  "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept");
        response.setHeader("Access-Control-Max-Age",       "3600");

        // Browsers send a preflight OPTIONS request before the real request.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
    	
    }
}
