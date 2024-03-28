package com.communiverse.communiverse.controller;

import com.communiverse.communiverse.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@Slf4j
public class ApplicationErrorsController implements ErrorController {

    @Data
    @AllArgsConstructor
    private class RequestAttribute {
        private Object status;
        private Object uri;
        private Object message;
        private Object exception;
    }

    @RequestMapping("/error")
    public ResponseEntity<String> handleError(HttpServletRequest request) {

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object uri = request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        RequestAttribute requestAttribute = new RequestAttribute(status, uri, message, exception);
        if (isDismissibleError(requestAttribute)) {
            return ResponseEntity.ok().body("");
        }

        String error = "BAD REQUEST";

        if (status != null) {
            error += ", Status: " + status;
        }
        if (uri != null) {
            error += ", URI: " + uri;
        }
        if (message != null && !message.equals("")) {
            error += ", Error: " + message;
        }
        if (exception != null && !exception.equals("")) {
            error += ", Exception: " + exception;
        }

        log.error(error);
        return ResponseEntity.badRequest().body(JsonUtils.toJson(error));
    }

    /**
     * if error not important and can be ignored, just not to garbage log.
     * @param requestAttribute
     * @return true - if error can be dismissed, otherwise false.
     */
    private boolean isDismissibleError(RequestAttribute requestAttribute) {

        // Dismissing this error: BAD REQUEST, Status: 404, URI: /favicon.ico
        if (requestAttribute.uri != null && requestAttribute.uri.toString().equalsIgnoreCase("/favicon.ico")) {
            log.debug("Dismissing '/favicon.ico' error.");
            return true;
        }

        return false;
    }
}
