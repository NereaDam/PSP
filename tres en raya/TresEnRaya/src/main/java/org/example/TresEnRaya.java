package org.example;

import config.ConstantesTresEnRaya;
import config.ThymeleafConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;

@WebServlet("/tres")
public class TresEnRaya extends HttpServlet {


protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    var webExchange = JakartaServletWebApplication.buildApplication(getServletContext())
            .buildExchange(req, resp);
    WebContext ctx = new WebContext(webExchange);
    resp.setContentType(ThymeleafConstants.CONTENT_TYPE);
    ((TemplateEngine)getServletContext().getAttribute(ThymeleafConstants.TEMPLATE_ENGINE_ATTR))
            .process(ConstantesTresEnRaya.TEMPLATE_TRES_EN_RAYA, ctx, resp.getWriter());
}

    }
