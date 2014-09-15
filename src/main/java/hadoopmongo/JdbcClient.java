package hadoopmongo;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;


/**
 * 
 * Main class to test the hive jdbc client.
 *
 * @author Bruno Bonnin
 *
 */
public class JdbcClient {

    private static final Logger logger = LoggerFactory.getLogger(JdbcClient.class);
    
    /** JDBC template for lauching queries. */
    @Inject
    private JdbcTemplate hive;
    
    /**
     * Main.
     * 
     * @param args Command line arguments
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        
        final AbstractApplicationContext ctx = new ClassPathXmlApplicationContext("jdbc-client.xml");
        ctx.registerShutdownHook();

        try {
            final JdbcClient client = (JdbcClient) ctx.getBean(JdbcClient.class);
            
           client.startTests();
        }
        finally {
            ctx.close();
        }
    }
    
    public JdbcClient() {
    }

    /**
     * Process the query.
     * 
     * @param query Query to process
     */
    public void processQuery(String query) {
        
        try {
            System.out.println("===> Query : " + query);
            
            hive.query(query, new RowCallbackHandler() {

                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    final ResultSetMetaData metaData = rs.getMetaData();
                    for (int colIdx = 1; colIdx <= metaData.getColumnCount(); colIdx++) {
                        if (colIdx > 1) {
                            System.out.print(", ");
                        }
                        System.out.print(metaData.getColumnName(colIdx) + " => " + rs.getObject(colIdx));
                    }
                    System.out.println();
                }
                
            });
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Process query", e);
        }
    }
    
    /**
     * Show some basic examples.
     */
    public void startTests() {
        
        /*final List<String> tables = hive.queryForList("show tables", String.class);
        for (String table : tables) {
            System.out.println("Table : " + table);
        }
        
        long nbJobs = hive.queryForObject("select count(1) from jobs", Long.class);
        System.out.println("Count jobs : " + nbJobs);*/
        
        //processQuery("show tables");
        //processQuery("select count(1) as job_count from jobs");
        //processQuery("select * from jobs");
        processQuery("select i.name as individual_name, j.job as individual_job from individuals i, jobs j where i.name=j.name");
    }
}
