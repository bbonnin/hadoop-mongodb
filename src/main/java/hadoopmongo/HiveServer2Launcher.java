package hadoopmongo;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hive.service.server.HiveServer2;

public class HiveServer2Launcher {

    public static void main(String[] args) {
        HiveConf hiveConf = new HiveConf();
        hiveConf.set("javax.jdo.option.ConnectionURL", "jdbc:derby:;databaseName=metastore_db;create=true");
        hiveConf.set("javax.jdo.option.ConnectionDriverName", "org.apache.derby.jdbc.EmbeddedDriver");
        hiveConf.set("hive.metastore.warehouse.dir", "file:///tmp");
        hiveConf.set("hive.server2.thrift.port", "11000");

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
