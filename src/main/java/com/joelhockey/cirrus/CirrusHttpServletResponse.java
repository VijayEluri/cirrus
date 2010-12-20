// Copyright 2010 Joel Hockey (joel.hockey@gmail.com).  MIT Licence

package com.joelhockey.cirrus;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


/**
 * Wraps servlet response and makes status accessible.
 * @author Joel Hockey
 */
public class CirrusHttpServletResponse extends HttpServletResponseWrapper {

    public CirrusHttpServletResponse(HttpServletResponse response) {
        super(response);
    }

    private int status = 200;
    private boolean statusSet = false;

    @Override
    public void sendError(int sc) throws IOException {
        status = sc;
        statusSet = true;
        super.sendError(sc);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        status = sc;
        statusSet = true;
        super.sendError(sc, msg);
    }

    @Override
    public void setStatus(int sc) {
        status = sc;
        statusSet = true;
        super.setStatus(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        status = 302;
        statusSet = true;
        super.sendRedirect(location);
    }

    public int getStatus() { return status; }
    public boolean isStatusSet() { return statusSet; }
}
