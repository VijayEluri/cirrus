// Copyright 2010 Joel Hockey (joel.hockey@gmail.com).  MIT Licence

package com.joelhockey.cirrus;

import java.io.IOException;
import java.sql.Connection;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.debugger.Main;

/**
 * Main servlet for cirrus. Manages ThreadLocal {@link CirrusScope}
 * and dispatches to /app/cirrus.js
 * @author Joel Hockey
 */
public class CirrusServlet extends HttpServlet {
    private static final long serialVersionUID = 0x26FAB6AD9ECB6BDCL;
    private static final Log log = LogFactory.getLog(CirrusServlet.class);

    private static boolean STATIC_INIT = false;
    private static boolean DEBUG_JS = false;
    private static DataSource DATA_SOURCE;
    static final RhinoJava WRAP_FACTORY = new RhinoJava();
    static CirrusScope SCOPE;

    /** Context Factory to set wrap factory and opt level. */
    static class CirrusContextFactory extends ContextFactory {
        @Override
        protected Context makeContext() {
            Context cx = super.makeContext();
            cx.setWrapFactory(WRAP_FACTORY);
            cx.setOptimizationLevel(DEBUG_JS ? -1 : 9);
            return cx;
        }
    }

    static {
        ContextFactory.initGlobal(new CirrusContextFactory());
    }

    @Override
    public void init() throws ServletException {
        staticInit();
    }

    /**
     * Perform init actions once per classloader.
     * Ensure DB is at correct version, if not run migrations.
     */
    private synchronized void staticInit() throws ServletException {
        if (STATIC_INIT) return;
        SCOPE = new CirrusScope(getServletConfig());

        // check if running in debug mode
        if (System.getProperty("debugjs") != null) {
            DEBUG_JS = true;
            // try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
            Main main = new Main("Cirrus Debug");
            main.setScope(SCOPE);
            main.attachTo(ContextFactory.getGlobal());
            main.pack();
            main.setSize(960, 720);
            main.setVisible(true);
        }

        // get datasource using 'dbname' servlet init-param
        try {
            InitialContext ic = new InitialContext();
            String dbname = getServletConfig().getInitParameter("dbname");
            log.info("Looking up datasource in jndi using 'dbname' servlet init-param: " + dbname);
            DATA_SOURCE = (DataSource) ic.lookup(dbname);
            // test
            Connection dbconn = DATA_SOURCE.getConnection();
            dbconn.close();
        } catch (Exception e) {
            log.error("Error getting dbconn", e);
            throw new ServletException("Error getting dbconn", e);
        }

        DB db = null;
        Cirrus cirrus = SCOPE.getCirrus();
        Timer timer = new Timer();
        try {
            db = new DB(DATA_SOURCE);
            cirrus.put("DB", cirrus, db);
            cirrus.load("/db/migrate.js");
            cirrus.delete("DB");
        } catch (Exception e) {
            log.error("Error migrating db", e);
            throw new ServletException("Error migrating db", e);
        } finally {
            if (db != null) {
                db.close();
            }
            timer.end("DB Migration");
        }
        STATIC_INIT = true;
    }

    /**
     * Forward requests to WEB-INF/app/cirrus.js.
     * Puts 'DB', {@link HttpServletRequest} as 'request', and
     * {@link HttpServletResponse} as 'response' into JS scope.
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            Cirrus cirrus = SCOPE.getCirrus();
            cirrus.load("/app/cirrus.js");

            Context cx = Context.enter();
            // var env = new cirrus.Env()
            Function f = (Function) cirrus.get("Env", cirrus);
            Scriptable env = f.construct(cx, SCOPE, ScriptRuntime.emptyArgs);

            // set up db, timer
            DB db = new DB(DATA_SOURCE);
            env.put("db", env, db);
            Timer timer = new Timer();
            env.put("timer", env, timer);

            // servlet request, response
            env.put("request", env, new NativeJavaObject(SCOPE, req, HttpServletRequest.class));
            env.put("response", env, new NativeJavaObject(SCOPE, res, HttpServletResponse.class));

            try {
                // 'cirrus.service(env)'
                Function service = (Function) cirrus.get("service", cirrus);
                service.call(cx, cirrus, cirrus, new Object[] {env});
            } finally {
                Context.exit();
                // close DB, record time
                db.close();
                timer.end(req.getMethod() + " " + req.getRequestURI());
            }
        } catch (Exception e) {
            log.error("Error running cirrus", e);
            throw new ServletException("Error running cirrus", e);
        }
    }
}