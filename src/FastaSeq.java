import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class FastaSeq {

	private Map<Integer, SimpleEntry<String,String>> coupleMap;
	private int idCouple = 0;
	
	private static final String FASTA_BIN = "fasta36"; 
	private static String WORKING_DIR;
	Logger logger = Logger.getLogger(FastaSeq.class.getName());
	
	public FastaSeq(String inputDirPath){
		WORKING_DIR = System.getProperty("user.dir") +"/"+ inputDirPath;
		coupleMap = new HashMap<Integer, SimpleEntry<String,String>>();
		buildAllPairSequence();
	}
	
	public FastaSeq() {
		coupleMap = new HashMap<Integer, SimpleEntry<String,String>>();
	}
	
	public void buildAllPairSequence(){		
		File folder = new File(WORKING_DIR);
		File[] listOfFiles = folder.listFiles();
		
		String f1NameFile = null;
		
		logger.info("Start to build the Map with all couple...");
		
		for(int i=0; i<listOfFiles.length-1; i++)
		{
			f1NameFile = listOfFiles[i].getName();
			for(int j=i+1; j<listOfFiles.length; j++)
			{
				coupleMap.put(idCouple, new SimpleEntry<String, String>(f1NameFile, listOfFiles[j].getName()) );
				idCouple++;
			}
		}
		
		logger.info("End to build the Map...");

	}
	
	public String[] getCoupleAtRow(int row){
		return new String[]{
				coupleMap.get(row).getKey(),
				coupleMap.get(row).getValue()
		};
	}
	
	public void run(){
		String f1NameFile = null;
		String f2NameFile = null;
		
		String[] coupleToCompair = null;
		
		ProcessBuilder runner = null;
		List<String> arguments = new ArrayList<String>();
		
		logger.info("Start to execute Fasta...");
		
		for(int i = 0; i < coupleMap.size(); i++)
		{
			coupleToCompair = getCoupleAtRow(i);
			
			f1NameFile = coupleToCompair[0];
			f2NameFile = coupleToCompair[1];
			
			arguments.clear();
			arguments.add(WORKING_DIR + "/../" +FASTA_BIN);
			arguments.add("-q");
			arguments.add(f1NameFile);
			arguments.add(f2NameFile);
			
			runner = new ProcessBuilder(arguments);
			
				
			runner.redirectErrorStream(true);
			
			logger.info("Ready to run "+f1NameFile+" - "+f2NameFile+"...");
			
			Process p = null;
			PrintWriter out = null;
			

			
			try
			{
				runner.directory(new File(WORKING_DIR));
				p = runner.start();
			
				
				InputStream stdin = p.getInputStream();
				InputStreamReader isr = new InputStreamReader(stdin);
				BufferedReader buffer = new BufferedReader(isr);

				String line = null;

				String resultFile = f1NameFile+"###"+f2NameFile;

				out = new PrintWriter(new File(resultFile));
				
				/*
				 * File log = new File("log");
 				 * pb.redirectErrorStream(true);
 				 * pb.redirectOutput(Redirect.appendTo(log));
				 */

				while ((line = buffer.readLine()) != null) 
				{
					out.println(line);
				}

				p.waitFor(); // it retuns the exit value.. 
				
				out.close();
			}
			catch (Exception e)
			{
				logger.info(e.getMessage());
				e.printStackTrace();
			}

			logger.info("Finished to run "+f1NameFile+" - "+f2NameFile+"...");
			
			p = null;
			System.gc();



		}
	}
	
	public static void main(String[] args) {
		
		if(args.length < 1)
		{
			System.out.println("Give me the folder......");
			System.exit(1);
		}
					
		FastaSeq seq = new FastaSeq(args[0]);
		
		seq.run();
		
	}

	
}
