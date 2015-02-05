package hadoopmongo;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.conf.HiveConf.ConfVars;
import org.apache.hive.service.auth.HiveAuthFactory.AuthTypes;
import org.apache.hive.service.auth.PlainSaslHelper;
import org.apache.hive.service.cli.thrift.TCLIService;
import org.apache.hive.service.cli.thrift.TCLIService.Client;
import org.apache.hive.service.cli.thrift.TCloseSessionReq;
import org.apache.hive.service.cli.thrift.TExecuteStatementReq;
import org.apache.hive.service.cli.thrift.TExecuteStatementResp;
import org.apache.hive.service.cli.thrift.TOpenSessionReq;
import org.apache.hive.service.cli.thrift.TSessionHandle;
import org.apache.hive.service.server.HiveServer2;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class HiveServer2Launcher {

    private static final String HOST = "localhost";
    private static final int PORT = 11100;
    private static Client client;

    public static void main(String[] args) throws Exception {
        HiveConf hiveConf = new HiveConf();
        hiveConf.set("javax.jdo.option.ConnectionURL", "jdbc:derby:;databaseName=metastore_db;create=true");
        hiveConf.set("javax.jdo.option.ConnectionDriverName", "org.apache.derby.jdbc.EmbeddedDriver");
        hiveConf.set("hive.metastore.warehouse.dir", "file:///tmp");
        //hiveConf.set("hive.server2.thrift.port", "11100");
        hiveConf.setBoolVar(ConfVars.HIVE_SERVER2_ENABLE_DOAS, false);
        hiveConf.setVar(ConfVars.HIVE_SERVER2_THRIFT_BIND_HOST, HOST);
        hiveConf.setIntVar(ConfVars.HIVE_SERVER2_THRIFT_PORT, PORT);
        hiveConf.setVar(ConfVars.HIVE_SERVER2_AUTHENTICATION, AuthTypes.NOSASL.toString());
        hiveConf.setVar(ConfVars.HIVE_SERVER2_TRANSPORT_MODE, "binary");

        /*<!--hive.metastore.local=true
                mapreduce.framework.name=yarn
                hive.exec.submitviachild=false-->
                hive.debug.localtask=true
                hive.auto.convert.join.use.nonstaged=true*/
        HiveServer2 server = new HiveServer2();
        server.init(hiveConf);
        server.start();

        initClient(createBinaryTransport());
    }

    private static TTransport createBinaryTransport() throws Exception {
        return PlainSaslHelper.getPlainTransport("anonymous", "anonymous", new TSocket(HOST, PORT));
    }

    private static void initClient(TTransport transport) {
        TProtocol protocol = new TBinaryProtocol(transport);
        client = new TCLIService.Client(protocol);
    }

    private static TExecuteStatementResp executeQuerySync(String queryString, TSessionHandle sessHandle)
            throws Exception {
        TExecuteStatementReq execReq = new TExecuteStatementReq();
        execReq.setSessionHandle(sessHandle);
        execReq.setStatement(queryString);
        execReq.setRunAsync(false);
        TExecuteStatementResp execResp = client.ExecuteStatement(execReq);
        return execResp;
    }

    private static void createTable() throws Exception {
        TOpenSessionReq openReq = new TOpenSessionReq();
        TSessionHandle sessHandle = client.OpenSession(openReq).getSessionHandle();
        
        String queryString = "SET hive.lock.manager=org.apache.hadoop.hive.ql.lockmgr.EmbeddedLockManager";
        executeQuerySync(queryString, sessHandle);
        
        executeQuerySync("CREATE DATABASE IF NOT EXISTS es", sessHandle);
        executeQuerySync("USE es", sessHandle);
        
        queryString = "DROP TABLE IF EXISTS siebel_adresse";
        executeQuerySync(queryString, sessHandle);

        queryString = "CREATE EXTERNAL TABLE IF NOT EXISTS siebel_adresse (" +
                        "a_addr STRING, a_addr_line_2 STRING, a_addr_line_3 STRING, a_addr_line_4 STRING, " +
                        "a_addr_line_5 STRING, a_city STRING, a_country STRING, a_last_upd TIMESTAMP, " +
                        "a_principal_flg STRING, a_province STRING, a_row_id STRING, a_x_uge_flg STRING, " +
                        "a_zipcode STRING, b_active_flg STRING, d_asset_num STRING) " +
                        "STORED BY 'org.elasticsearch.hadoop.hive.EsStorageHandler' " +
                        "TBLPROPERTIES('es.resource'='siebel/adresse', 'es.index.auto.create'='false', 'es.nodes'='192.168.1.45')";
        executeQuerySync(queryString, sessHandle);
        
        TCloseSessionReq closeReq = new TCloseSessionReq(sessHandle);
        client.CloseSession(closeReq);
    }
}
