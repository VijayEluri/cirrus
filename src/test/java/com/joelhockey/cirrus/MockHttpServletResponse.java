// Copyright 2010 Joel Hockey (joel.hockey@gmail.com).  MIT Licence

package com.joelhockey.cirrus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class MockHttpServletResponse implements HttpServletResponse {
    public ByteArrayOutputStream baos = new ByteArrayOutputStream();
    public ServletOutputStream sos = new MockServletOutputStream(baos);
    public PrintWriter pw = new PrintWriter(baos);
    public Map<String, String> headers = new HashMap<String, String>();
    public int status = 200;
    public String redirect;
    private SimpleDateFormat dateHeaderFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'");
    {
    	dateHeaderFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    private String contentType;
    public int getStatus() { return status; }
    public String getResponse() {
        pw.flush();
        return new String(baos.toByteArray());
    }

    public void addCookie(Cookie arg0) { throw new UnsupportedOperationException(); }
    public void addDateHeader(String header, long value) { headers.put(header, dateHeaderFormat.format(new Date(value))); }
    public void addHeader(String header, String value) { headers.put(header, value); }
    public void addIntHeader(String header, int value) { headers.put(header, "" + value); }
    public boolean containsHeader(String header) { return headers.containsKey(header); }
    public String encodeRedirectURL(String path) { throw new UnsupportedOperationException(); }
    public String encodeRedirectUrl(String path) { throw new UnsupportedOperationException(); }
    public String encodeURL(String path) { throw new UnsupportedOperationException(); }
    public String encodeUrl(String path) { throw new UnsupportedOperationException(); }
    public void sendError(int sc) throws IOException { throw new UnsupportedOperationException(); }
    public void sendError(int sc, String msg) throws IOException { throw new UnsupportedOperationException(); }
    public void sendRedirect(String location) throws IOException {
        this.redirect = location;
        status = 302;
    }
    public void setDateHeader(String header, long value) {
    	headers.put(header, dateHeaderFormat.format(new Date(value)));
    }
    public void setHeader(String header, String value) { headers.put(header, value); }
    public void setIntHeader(String header, int value) { headers.put(header, "" + value); }
    public void setStatus(int status) { this.status = status; }
    public void setStatus(int status, String s) { this.status = status; }
    public void flushBuffer() throws IOException { throw new UnsupportedOperationException(); }
    public int getBufferSize() { return 0; }
    public String getCharacterEncoding() { return "UTF-8"; }
    public String getContentType() {
        return contentType;
    }
    public Locale getLocale() { return Locale.ENGLISH; }
    public ServletOutputStream getOutputStream() throws IOException { return sos; }
    public PrintWriter getWriter() throws IOException { return pw; }
    public boolean isCommitted() { return false; }
    public void reset() {
    	baos.reset();
    	headers.clear();
    	status = 200;
    }
    public void resetBuffer() { throw new UnsupportedOperationException(); }
    public void setBufferSize(int size) { throw new UnsupportedOperationException(); }
    public void setCharacterEncoding(String charset) { throw new UnsupportedOperationException(); }
    public void setContentLength(int len) { throw new UnsupportedOperationException(); }
    public void setContentType(String type) {
        contentType = type;
    }
    public void setLocale(Locale loc) { throw new UnsupportedOperationException(); }
}
