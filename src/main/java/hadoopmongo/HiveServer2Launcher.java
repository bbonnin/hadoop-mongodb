package hadoopmongo;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.conf.HiveConf.ConfVars;
import org.apache.hive.service.auth.HiveAuthFactory.AuthTypes;
import org.apache.hive.service.server.HiveServer2;

public class HiveServer2Launcher {

    public static void main(String[] args) {
        HiveConf hiveConf = new HiveConf();
        hiveConf.set("javax.jdo.option.ConnectionURL", "jdbc:derby:;databaseName=metastore_db;create=true");
        hiveConf.set("javax.jdo.option.ConnectionDriverName", "org.apache.derby.jdbc.EmbeddedDriver");
        hiveConf.set("hive.metastore.warehouse.dir", "file:///tmp");
        //hiveConf.set("hive.server2.thrift.port", "11100");
        hiveConf.setBoolVar(ConfVars.HIVE_SERVER2_ENABLE_DOAS, false);
        hiveConf.setVar(ConfVars.HIVE_SERVER2_THRIFT_BIND_HOST, "localhost");
        hiveConf.setIntVar(ConfVars.HIVE_SERVER2_THRIFT_PORT, 11100);
        hiveConf.setVar(ConfVars.HIVE_SERVER2_AUTHENTICATION, AuthTypes.NONE.toString());
        hiveConf.setVar(ConfVars.HIVE_SERVER2_TRANSPORT_MODE, "binary");

        /*<!--hive.metastore.local=true
                mapreduce.framework.name=yarn
                hive.exec.submitviachild=false-->
                hive.debug.localtask=true
                hive.auto.convert.join.use.nonstaged=true*/
        HiveServer2 server = new HiveServer2();
        server.init(hiveConf);
        server.start();
    }
}
