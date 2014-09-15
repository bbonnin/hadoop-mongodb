package hadoopmongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * 
 * Main class starting a minicluster with Hadoop and Hive components.
 *
 * @author Bruno Bonnin
 *
 */
public class MiniCluster {

    private static final Logger logger = LoggerFactory.getLogger(MiniCluster.class);

    /**
     * Main.
     * 
     * @param args Command line arguments
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {

        logger.info("Starting cluster...");

        AbstractApplicationContext ctx = null;
        
        try {
            ctx = new ClassPathXmlApplicationContext("hadoop-hive.xml");
            ctx.registerShutdownHook();
            
            logger.info("Cluster started !");
            
            // Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaargh ! Shame on me !!!
            while (true) { 
                Thread.currentThread().sleep(100000);
            }
        }
        finally {
            if (ctx != null) {
                ctx.close();
            }
            logger.info("Cluster stopped !");
        }
    }
    
}
