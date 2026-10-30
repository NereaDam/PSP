package org.example;

import config.ThymeleafConstants;
import config.UrlConstants;
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

        String marca = req.getParameter("marca");
        String coordenada = req.getParameter("coordenada");

        if (marca==null || coordenada==null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"ASver si te entreras cabron, parametro requertido");
            return;
        }

        String[] Tablero = (String[]) req.getSession().getAttribute("Tablero");
        if (Tablero==null) {
            Tablero = new String[]{"-", "-", "-", "-", "-", "-", "-", "-", "-"};
            req.getSession().setAttribute("Tablero",Tablero);
        }

    String error = null;
    int posicion = 0;

    try {
        posicion = Integer.parseInt(coordenada);
    } catch (NumberFormatException e) {
        error = "La coordenada tiene que ser un número, no una letra";
    }

    if (error == null && (posicion < 1 || posicion > 9)) {
        error = "La coordenada está fuera del tablero (del 1 al 9)";
    }
    if (error == null && !Tablero[posicion-1].equals("-")) {
        error = "Esa posición ya está ocupada";
    }
    if (error == null && !(marca.equals("X") || marca.equals("O"))) {
        error = "La marca tiene que ser X o O";
    }
    if (error == null && marca.equals(req.getSession().getAttribute("ultimaMarca"))) {
        error = "Esa marca ya ha jugado, le toca a la otra";
    }

    if (error == null) {
        Tablero[posicion-1]=marca;
        req.getSession().setAttribute("Tablero",Tablero);
        req.getSession().setAttribute("ultimaMarca",marca);
    }

        String fila1 = Tablero[0]+ " " + Tablero[1]+ " " + Tablero[2];
        String fila2 = Tablero[3]+ " " + Tablero[4]+ " " + Tablero[5];
        String fila3 = Tablero[6]+ " " + Tablero[7] + " " + Tablero[8];

        var webExchange = JakartaServletWebApplication.buildApplication(getServletContext())
                .buildExchange(req, resp);
        WebContext ctx = new WebContext(webExchange);
        ctx.setVariable("fila1", fila1);
        ctx.setVariable("fila2", fila2);
        ctx.setVariable("fila3", fila3);
        ctx.setVariable("error", error);
        resp.setContentType(ThymeleafConstants.CONTENT_TYPE);
        ((TemplateEngine)getServletContext().getAttribute(ThymeleafConstants.TEMPLATE_ENGINE_ATTR))
                .process(UrlConstants.TEMPLATE_TRES_EN_RAYA, ctx, resp.getWriter());

    }

    }
