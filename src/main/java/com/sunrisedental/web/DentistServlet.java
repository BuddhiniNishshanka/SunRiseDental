package com.sunrisedental.web;

import com.sunrisedental.factory.DAOFactory;
import com.sunrisedental.model.Dentist;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "DentistServlet", urlPatterns = {"/api/dentists"})
public class DentistServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<Dentist> dentists = DAOFactory.getInstance().getDentistDAO().findAllAvailable();
        sendSuccess(resp, "Dentists retrieved", dentists);
    }
}
