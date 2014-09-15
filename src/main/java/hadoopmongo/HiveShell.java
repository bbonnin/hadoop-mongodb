package hadoopmongo;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.hadoop.hive.HiveScript;
import org.springframework.data.hadoop.hive.HiveTemplate;


/**
 * 
 * Main class starting the hive shell.
 *
 * @author Bruno Bonnin
 *
 */
public class HiveShell {

    private static final Logger logger = LoggerFactory.getLogger(HiveShell.class);

    /** Console for the interactive mode. */
    private ConsoleReader reader;
    
    /** Hive template for lauching queries. */
    @Inject
    private HiveTemplate hive;
    
    /**
     * Main.
     * 
     * @param args Command line arguments
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        
        final AbstractApplicationContext ctx = new ClassPathXmlApplicationContext("hive-shell.xml");
        ctx.registerShutdownHook();

        System.out.println("+---------------------------+");
        System.out.println("| Welcome to the Hive Shell |");
        System.out.println("+---------------------------+");

        try {
            final HiveShell shell = (HiveShell) ctx.getBean(HiveShell.class);
            
            if (args.length != 0) {
                shell.processBatch(args[0]);
            }
            
            shell.run();
        }
        finally {
            ctx.close();
        }
    }
    
    public HiveShell() {
    }

    /**
     * Process the content of a file.
     * 
     * @param fileName Name of the file
     */
    public void processBatch(String fileName) {
        
        try {
            final HiveScript script = new HiveScript(new FileSystemResource(fileName));
            final List<String> res = hive.executeScript(script);
            for (String line : res) {
                System.out.println(line);
            }
        }
        catch (Exception e) {
            //e.printStackTrace();
            logger.error("Process script", e);
        }
        
        
        
//        BufferedReader reader = null;
//        
//        try {
//            reader = new BufferedReader(new FileReader(fileName));
//            String line = null;
//            while ((line = reader.readLine()) != null) {
//                if (!StringUtils.isEmpty(line)) {
//                    System.out.println("Traitement de [" + line + "]");
//                    processCommand(line.trim());
//                }
//            }
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//        finally {
//            if (reader != null) {
//                try {
//                    reader.close();
//                }
//                catch (IOException e) {
//                    
//                }
//            }
//        }
    }
    
    
    
    /**
     * Loop for the interactive mode of the shell.
     */
    public void run() {
        
        try {
            initConsole();

            String line;
            while ((line = reader.readLine()) != null) {
                if (!StringUtils.isEmpty(line)) {
                    processCommand(line.trim());
                }
            }
        } 
        catch (IOException e) {
            logger.error("Read console", e);
            System.exit(1);
        }
    }
    
    /**
     * Process a command.
     * 
     * @param command Command
     * @return The result of the command
     */
    private List<String> processCommand(String command) {

        if ("exit".equalsIgnoreCase(command) || "quit".equalsIgnoreCase(command)) {
            System.exit(0);
        }
        else if ("examples".equalsIgnoreCase(command)) {
            showExamples();
        }
        else {
            try {
                final List<String> res = hive.query(command);
                for (String line : res) {
                    System.out.println(line);
                }
                return res;
            }
            catch (Exception e) {
                e.printStackTrace();
                logger.error("Process query", e);
            }
        }
        return null;
    }
    
    /**
     * Show some basic examples.
     */
    private void showExamples() {
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  show tables;");
        System.out.println("  drop table if exists jobs;");
        System.out.println();
        System.out.println("  create table jobs (id string, name string, job string) stored by 'com.mongodb.hadoop.hive.MongoStorageHandler' WITH SERDEPROPERTIES('mongo.columns.mapping'='{\"id\":\"_id\"}') TBLPROPERTIES('mongo.uri'='mongodb://localhost:27017/test.jobs');");
        System.out.println("  create table individuals (id struct<oid:string, bsontype:int>, name string, age int, address struct<zipcode:int, city:string>) stored by 'com.mongodb.hadoop.hive.MongoStorageHandler' WITH SERDEPROPERTIES('mongo.columns.mapping'='{\"id\":\"_id\"}') TBLPROPERTIES('mongo.uri'='mongodb://localhost:27017/test.individuals', 'columns.comments'='');");
        System.out.println("  describe extended individuals;");
        System.out.println();
        System.out.println("  create table src_individuals (name string, age int, zipcode int, city string) row format delimited fields terminated by ',';");
        System.out.println("  load data local inpath 'file:///tmp/data.csv' into table src_individuals;");
        System.out.println("  create temporary function newObjectId as 'hadoopmongo.UDFObjectId';");
        System.out.println("  insert into table individuals select newObjectId() as id, name, age, named_struct('zipcode', zipcode, 'city', city) as address from src_individuals;");
        System.out.println();
        System.out.println("  select * from individuals;");
        System.out.println("  select count(*) from individuals;");
        System.out.println();
        System.out.println("  select i.name, j.job from individuals i, jobs j where i.name=j.name;");
        System.out.println();
    }
    
    /**
     * Console initialisation.
     */
    private void initConsole() {

        try {
            reader = new ConsoleReader();
            final StringsCompleter strCompleter = new StringsCompleter(
                    "examples", "quit", "exit", "select", "insert", "update", "drop", "show" );

            reader.setPrompt(" > ");
            reader.addCompleter(strCompleter);

        }
        catch (IOException e) {
            logger.error("Init de la console", e);
            System.exit(1);
        }
    }

}
