package com.sunrisedental.web;

import com.sunrisedental.factory.DAOFactory;
import com.sunrisedental.model.Treatment;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "TreatmentServlet", urlPatterns = {"/api/treatments"})
public class TreatmentServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<Treatment> list = DAOFactory.getInstance().getTreatmentDAO().findAll();
        sendSuccess(resp, "Treatments retrieved", list);
    }
}
