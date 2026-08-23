package com.sunrisedental.web;

import com.google.gson.JsonObject;
import com.sunrisedental.model.Bill;
import com.sunrisedental.service.BillingService;
import com.sunrisedental.service.strategy.BillingCalculationResult;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@WebServlet(name = "BillingServlet", urlPatterns = {"/api/bills/*"})
public class BillingServlet extends BaseServlet {

    private final BillingService billingService = new BillingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if ("/preview".equals(pathInfo)) {
            String appParam = req.getParameter("appointmentId");
            if (appParam == null || appParam.trim().isEmpty()) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "appointmentId is required.");
                return;
            }

            try {
                int appointmentId = Integer.parseInt(appParam.trim());
                BigDecimal discount = parseBigDecimalParam(req.getParameter("discountRate"), BigDecimal.ZERO);
                BigDecimal tax = parseBigDecimalParam(req.getParameter("taxRate"), BigDecimal.ZERO);

                StringBuilder error = new StringBuilder();
                BillingCalculationResult result = billingService.previewBillCalculation(appointmentId, discount, tax, error);
                if (result != null) {
                    sendSuccess(resp, "Calculation preview computed", result);
                } else {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, error.toString());
                }
            } catch (NumberFormatException e) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid appointment ID format.");
            }
            return;
        }

        if ("/search".equals(pathInfo)) {
            String billNo = req.getParameter("billNo");
            String appNo = req.getParameter("appNo");
            String appIdParam = req.getParameter("appointmentId");

            if (billNo != null && !billNo.trim().isEmpty()) {
                Optional<Bill> billOpt = billingService.getBillByBillNo(billNo.trim());
                if (billOpt.isPresent()) {
                    sendSuccess(resp, "Invoice found", billOpt.get());
                } else {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Invoice not found for Bill No: " + billNo);
                }
                return;
            }

            if (appIdParam != null && !appIdParam.trim().isEmpty()) {
                try {
                    int appId = Integer.parseInt(appIdParam.trim());
                    Optional<Bill> billOpt = billingService.getBillByAppointmentId(appId);
                    if (billOpt.isPresent()) {
                        sendSuccess(resp, "Invoice found", billOpt.get());
                    } else {
                        sendError(resp, HttpServletResponse.SC_NOT_FOUND, "No invoice exists for Appointment ID: " + appId);
                    }
                    return;
                } catch (NumberFormatException ignored) {}
            }

            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Please provide 'billNo' or 'appointmentId'.");
            return;
        }

        sendSuccess(resp, "All bills retrieved", billingService.getAllBills());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if ("/process".equals(pathInfo) || pathInfo == null || "/".equals(pathInfo)) {
            JsonObject json = parseRequestJsonObject(req);
            if (json == null || !json.has("appointmentId")) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "appointmentId is required in JSON payload.");
                return;
            }

            try {
                int appointmentId = json.get("appointmentId").getAsInt();
                BigDecimal discount = json.has("discountRate") ? json.get("discountRate").getAsBigDecimal() : BigDecimal.ZERO;
                BigDecimal tax = json.has("taxRate") ? json.get("taxRate").getAsBigDecimal() : BigDecimal.ZERO;
                String paymentMethod = json.has("paymentMethod") ? json.get("paymentMethod").getAsString() : "CASH";

                String username = getLoggedInUsername(req);
                StringBuilder error = new StringBuilder();

                Optional<Bill> billOpt = billingService.processPatientInvoice(
                        appointmentId, discount, tax, paymentMethod, username, error
                );

                if (billOpt.isPresent()) {
                    sendSuccess(resp, "Bill generated successfully! Invoice No: " + billOpt.get().getBillNo(), billOpt.get());
                } else {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, error.toString());
                }
            } catch (Exception e) {
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating bill: " + e.getMessage());
            }
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
        }
    }

    private BigDecimal parseBigDecimalParam(String param, BigDecimal fallback) {
        if (param == null || param.trim().isEmpty()) return fallback;
        try {
            return new BigDecimal(param.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private String getLoggedInUsername(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("username") != null) {
            return (String) session.getAttribute("username");
        }
        return "reception";
    }
}
